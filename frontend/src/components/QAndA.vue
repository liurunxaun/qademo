<template>
  <div class="container">
    <!-- 左侧：聊天界面 -->
    <div class="chat-container">
      <div class="chat-header">
        <h2>NLP科研知识问答小助手</h2>
      </div>
      <div class="chat-body">
        <div v-for="(message, index) in messages" :key="index" class="message">
          <div :class="message.sender === 'user' ? 'user-message' : 'bot-message'" v-html="message.text"></div></div>
      </div>
      <div class="chat-footer">
        <input v-model="userInput" @keyup.enter="sendMessage" placeholder="请输入您的问题..." />
        <button @click="sendMessage">Send</button>
      </div>
    </div>

    <!-- 右侧：知识图谱展示 -->
    <div class="knowledge-graph">
      <h2>Knowledge Graph</h2>
      <div id="graph"></div> <!-- 用于渲染知识图谱的区域 -->
    </div>
  </div>
</template>

<script>
import axios from "axios";
import * as echarts from 'echarts';

export default {
  data() {
    return {
      messages: [],
      userInput: '',
      question: '',
      answers: [],
      answer1: '',
      answer2: [],
      answer3: {},
    };
  },
  mounted() {
    window.addEventListener('resize', this.resizeChart);
  },
  methods: {
    async sendMessage() {
      if (this.userInput.trim()) {
        this.question = this.userInput;
        this.userInput = '';
        // 用户消息
        this.messages.push({ sender: 'user', text: this.question });

        try {
          const response = await axios.get("http://localhost:8080/api/qa", {
            params: { question : this.question }
          });

          this.answers = response.data;
          console.log(this.answers)
          this.answer1 = this.answers[0];
          this.answer2 = this.answers[1];
          this.answer3 = this.answers[2];

          // 模拟AI的回复
          this.temp_answer = ""
          if (this.answer1 != null) {
            this.temp_answer += this.answer1
          }
          if (this.answer2 != null) {
            this.temp_answer += "<br><br>" + this.answer2
          }
          if (this.answer3 != null) {
            // 渲染知识图谱
            this.renderKnowledgeGraph();
          }
          setTimeout(() => {
            this.messages.push({ sender: 'bot', text: this.temp_answer });
          }, 1000);
        }catch (error) {
          console.error("Error fetching answer:", error);
          // 模拟AI的回复
          setTimeout(() => {
            this.messages.push({ sender: 'bot', text: '人家没听明白，请换个说法吧'});
          }, 1000);
        }
      }
    },

    resizeChart() {
      const chartDom = document.getElementById('graph');
      const myChart = echarts.getInstanceByDom(chartDom);
      if (myChart) {
        myChart.resize();
      }
    },

    renderKnowledgeGraph() {
      const graphData = {
        nodes: [],
        edges: []
      };

      // 假设你在 sendMessage 中已准备好 this.answer3（子图数据）
      const subgraph = this.answer3;

      if (subgraph && subgraph.nodes && subgraph.edges) {
        graphData.nodes = subgraph.nodes.map(node => ({
          name: node.label,
          id: node.id,
          category: node.type
        }));

        graphData.edges = subgraph.edges.map(edge => ({
          source: edge.source,
          target: edge.target,
          relationship: edge.relationship
        }));
      }

      // ECharts 配置
      const chartDom = document.getElementById('graph');
      const myChart = echarts.init(chartDom);

      const option = {
        tooltip: {},
        animation: false,
        series: [
          {
            type: 'graph',
            layout: 'force',
            data: graphData.nodes,
            links: graphData.edges,
            categories: [
              { name: 'Title', itemStyle: { color: '#ffcc00' } },
              { name: 'Neighbor', itemStyle: { color: '#00ccff' } }
            ],
            force: {
              repulsion: 100,
              edgeLength: 50
            },
            label: {
              show: true,
              position: 'inside',
              formatter: '{b}'
            }
          }
        ]
      };

      myChart.setOption(option);
    }
  }
};
</script>

<style>
/* 全局样式重置 */
.container {
  display: flex;
  height: 100vh; /* 确保 container 占据整个视口高度 */
}

.chat-container {
  width: 60%;
  border-right: 1px solid #ccc;
  display: flex;
  flex-direction: column;
}

.knowledge-graph {
  width: 40%;
  padding: 10px;
}

.chat-header {
  background-color: #007bff;
  color: white;
  padding: 10px;
  text-align: center;
}
.chat-body {
  flex: 1;
  padding: 10px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
}
.message {
  margin-bottom: 10px;
  display: flex; /* 添加这一行 */
}
.user-message {
  text-align: left;
  background: #e0f7fa; /* 用户消息背景色 */
  border-radius: 10px; /* 圆角 */
  padding: 10px; /* 内边距 */
  padding-left: 20px; /* 内边距 */
  margin-left: auto; /* 自动左边距使消息靠右 */
  max-width: 75%; /* 限制最大宽度 */
}

.bot-message {
  text-align: left;
  background: #f1f8e9; /* AI消息背景色 */
  border-radius: 10px; /* 圆角 */
  padding: 10px; /* 内边距 */
  margin-right: auto; /* 自动右边距使消息靠左 */
  max-width: 75%; /* 限制最大宽度 */
}

.chat-footer {
  display: flex;
  padding: 10px;
}
.chat-footer input {
  flex: 1;
  padding: 10px;
  border: 1px solid #ccc;
  border-radius: 4px;
}
.chat-footer button {
  padding: 10px;
  margin-left: 10px;
  background-color: #007bff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}
#graph {
  width: 100%;
  height: 80vh;
}
</style>

<template>
  <div class="container">
    <!-- 左侧：聊天界面 -->
    <div class="chat-container">
      <div class="chat-header">
        <h2>NLP科研知识问答小助手</h2>
      </div>
      <div class="chat-body">
        <div v-for="(message, index) in messages" :key="index" class="message">
          <div :class="message.sender === 'user' ? 'user-message' : 'bot-message'" v-html="message.text"></div>
        </div>
      </div>
      <div class="chat-footer">
        <input v-model="userInput" @keyup.enter="sendMessage" placeholder="请输入您的问题..." />
        <button @click="sendMessage">Send</button>
      </div>
    </div>

    <!-- 右侧：知识图谱展示 -->
    <div class="knowledge-graph">
      <h2>Knowledge Graph</h2>
      <div ref="chart" id="graph"></div> <!-- 用于渲染知识图谱的区域 -->
    </div>
  </div>
</template>

<script>
import axios from "axios";
import * as echarts from 'echarts';
import { marked } from 'marked';

export default {
  data() {
    return {
      messages: [],
      userInput: '',
      question: '',
      answers: [],
      answer1: '',
      answer2: [],
      answer3: {}
    };
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
            this.temp_answer += marked(this.answer1)
          }

          if (this.answer2.length != 0) {
            // TODO:后面还需要处理answer2，他是[[实体：论文],[],...]格式的，考虑怎么展示给用户
            this.temp_answer += "<br><div class='answer2-message'>";
            this.answer2.forEach(item => {
              this.temp_answer += "<b>" + "问题关键词：" + "</b>" + "<br>"
              this.temp_answer += item[0] + "<br>"
              this.temp_answer += "<b>" + "知识图谱匹配实体：" + "</b>" + "<br>"
              this.temp_answer += item[1] + "<br>"
              this.temp_answer += "<b>" + "参考文献：" + "</b>" + "<br>"
              this.temp_answer += item[2] + "<br><br>"
            })
            this.temp_answer += "</div>";
          } else {
            this.temp_answer += "<br><br>" + "没有匹配到相关论文"
          }

          setTimeout(() => {
            this.messages.push({ sender: 'bot', text: this.temp_answer });
          }, 1000);

          if (this.answer3.length != 0) {
            console.log('Actual Graph Data:', this.answer3);
            // 渲染知识图谱
            this.renderGraph(this.answer3);
          }

        }catch (error) {
          console.error("Error fetching answer:", error);
          // 模拟AI的回复
          setTimeout(() => {
            this.messages.push({ sender: 'bot', text: error});
          }, 1000);
        }
      }
    },

    renderGraph(graphs) {
      const graphData = graphs[0]; // 假设只可视化第一个子图

      const graphContainer = this.$refs.chart;

      // 检查 graphContainer 是否有效
      if (!graphContainer) {
        console.error('Graph container is not found');
        return;
      }

      // 确保 graphData 有效
      if (!graphData || !graphData.nodes || !graphData.edges) {
        console.error('Invalid graph data:', graphData);
        return;
      }

      const seenIds = new Set();
      const uniqueNodes = [];

      // 过滤重复节点
      graphData.nodes.forEach(node => {
        if (!seenIds.has(node.id)) {
          seenIds.add(node.id);
          uniqueNodes.push({
            id: node.id,
            name: node.label,
            category: node.type === '标题' ? 0 : 1
          });
        } else {
          console.warn(`Duplicate node found: ${node.id}`);
        }
      });

      const edges = graphData.edges.map(edge => ({
        source: edge.source,
        target: edge.target
      }));

      const option = {
        tooltip: {},
        animation: false,
        series: [
          {
            type: 'graph',
            layout: 'circular',
            data: uniqueNodes,
            links: edges,
            categories: [{ name: '标题' }, { name: 'Neighbor' }],
            roam: true,
            label: {
              show: true
            }
          }
        ]
      };

      const chart = echarts.init(graphContainer);
      chart.setOption(option);
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
  min-height: 80vh; /* 确保有足够的空间 */
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

.answer2-message {
  line-height: 2; /* 设置answer2的行间距 */
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

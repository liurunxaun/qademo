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
      answer2: '',
      answer3: '',
      graphData: {
        nodes: [
          { id: '患者', name: '患者'},
          { id: '声音嘶哑', name: '声音嘶哑'},
          { id: '咽喉炎', name: '咽喉炎'},
          { id: '对喉咙进行物理检查', name: '对喉咙进行物理检查'},
          { id: '喉镜检查', name: '喉镜检查'},
          { id: '消炎药和类固醇', name: '消炎药和类固醇'},
          { id: '休息嗓子，避免刺激', name: '休息嗓子，避免刺激'},
        ],
        links: [
          { source: '患者', target: '声音嘶哑', label: { show: true, formatter: '一直有' } },
          { source: '声音嘶哑', target: '咽喉炎', label: { show: true, formatter: '可能是由于' } },
          { source: '咽喉炎', target: '对喉咙进行物理检查', label: { show: true, formatter: '需要' } },
          { source: '咽喉炎', target: '喉镜检查', label: { show: true, formatter: '可包括' } },
          { source: '咽喉炎', target: '消炎药和类固醇', label: { show: true, formatter: '可以用' } },
          { source: '咽喉炎', target: '休息嗓子，避免刺激', label: { show: true, formatter: '应同时服用' } },
        ],
      }
    };
  },
  mounted() {
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
            this.temp_answer += "<br><br>" + this.answer3
          }
          setTimeout(() => {
            this.messages.push({ sender: 'bot', text: this.temp_answer });
          }, 1000);

          // 渲染知识图谱
          this.renderKnowledgeGraph();

        }catch (error) {
          console.error("Error fetching answer:", error);
          // 模拟AI的回复
          setTimeout(() => {
            this.messages.push({ sender: 'bot', text: '人家没听明白，请换个说法吧'});
          }, 1000);
        }
      }
    },

    renderKnowledgeGraph (){
      this.chartInstance = echarts.init(document.getElementById('graph'));
      this.chartInstance.setOption({
        tooltip: {},
        series: [
          {
            type: 'graph',
            layout: 'force',
            data: this.graphData.nodes.map(node => ({
              ...node,
              label: {
                show: true,
                fontSize: 16,
              },
              symbolSize: 0
            })),
            links: this.graphData.links.map(link => ({
              ...link,
              lineStyle: {
                // curveness: 0.2, // 可以设置边的弯曲程度
                width: 2,
                color: '#000',
                type: 'solid',
                // 设置箭头样式
                arrow: true,
              },
            })),
            emphasis: {
              focus: 'adjacency',
              lineStyle: {
                width: 10,
                color: '#ff0000',
              },
            },
            lineStyle: {
              color: '#000',
              width: 2,
            },
            label: {
              show: true,
            },
            itemStyle: {
              borderWidth: 2,
              borderColor: '#fff',
            },
            force: {
              repulsion: 600, // 节点之间的斥力
              edgeLength: [50, 200], // 边的长度范围
            },
          },
        ],
      });
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

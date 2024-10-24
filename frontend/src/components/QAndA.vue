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
import * as d3 from 'd3';
import axios from "axios";

export default {
  data() {
    return {
      messages: [],
      userInput: '',
      question: '',
      answers: [],
      answer1: '',
      answer2: '',
      answer3: ''
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
          setTimeout(() => {
            this.messages.push({ sender: 'bot', text: this.answer1 + '<br><br>' + this.answer2 + '<br><br>' + this.answer3 });
          }, 1000);

          // 使用 D3.js 渲染知识图谱
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

    renderKnowledgeGraph() {
      const data = this.parseAnswerToGraph(this.answer3);

      const svg = d3.select("#graph").append("svg")
          .attr("width", 400)
          .attr("height", 400);

      const simulation = d3.forceSimulation(data.nodes)
          .force("link", d3.forceLink().id(d => d.id))
          .force("charge", d3.forceManyBody())
          .force("center", d3.forceCenter(200, 200));

      const link = svg.append("g")
          .selectAll("line")
          .data(data.links)
          .enter().append("line")
          .style("stroke", "#aaa");

      const node = svg.append("g")
          .selectAll("circle")
          .data(data.nodes)
          .enter().append("circle")
          .attr("r", 10)
          .style("fill", "#69b3a2");

      simulation.on("tick", () => {
        link.attr("x1", d => d.source.x)
            .attr("y1", d => d.source.y)
            .attr("x2", d => d.target.x)
            .attr("y2", d => d.target.y);

        node.attr("cx", d => d.x)
            .attr("cy", d => d.y);
      });
    },

    parseAnswerToGraph() {
      // 假设您将 answer3 解析为树形结构
      const nodes = [];
      const links = [];

      const addNode = (parent, child) => {
        const parentId = parent.id || parent;
        const childId = child.id || child;

        if (!nodes.some(n => n.id === parentId)) {
          nodes.push({ id: parentId });
        }
        if (!nodes.some(n => n.id === childId)) {
          nodes.push({ id: childId });
        }
        links.push({ source: parentId, target: childId });
      };

      // 模拟解析逻辑（需要根据具体格式调整）
      // 这里只是一个示例，实际解析需要根据您的 answer3 内容
      const treeData = {
        "Patient": ["has been experiencing", "could be caused by"],
        "has been experiencing": ["hoarse voice"],
        "could be caused by": ["laryngitis"],
        "laryngitis": ["requires", "can be treated with", "should be accompanied by"],
        "requires": ["physical examination of the throat"],
        "can be treated with": ["anti-inflammatory drugs and Steroids"],
        "should be accompanied by": ["resting the voice and avoiding irritants"],
        "physical examination of the throat": ["may include"],
        "may include": ["laryngoscopy"]
      };

      for (const [parent, children] of Object.entries(treeData)) {
        for (const child of children) {
          addNode(parent, child);
        }
      }

      return { nodes, links };
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
  height: 400px;
}
</style>

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
      answer3: {},
    };
  },

  mounted() {
    this.setupWebSocket();
  },

  methods: {
    async sendMessage() {
      if (this.userInput.trim()) {
        this.question = this.userInput;
        this.userInput = '';

        // 创建一个新消息对象，临时用于合并流式数据
        const messageId = Date.now(); // 使用时间戳作为唯一ID
        const newMessage = { id: messageId, sender: 'bot', text: '' };

        // 用户消息
        this.messages.push({ sender: 'user', text: this.question })
        this.messages.push(newMessage); // 添加临时空的 bot 消息框

        try {
          const response = await axios.post("http://localhost:8080/api/question", {
            question: this.question,
          });
          console.log("Request sent to Java:", response.data);
        }catch (error) {
          console.error("Error fetching answer:", error);
          this.updateMessageContent(messageId, "Error: " + error.message);
        }
      }
    },

    updateMessageContent(messageId, newContent) {
      const targetMessage = this.messages.find(msg => msg.id === messageId);
      if (targetMessage) {
        targetMessage.text += newContent; // 追加新内容
      }
    },

    setupWebSocket() {
      const socket = new WebSocket("ws://localhost:8080/ws");

      socket.onmessage = (event) => {
        this.enevnt_data = event.data
        console.log("Received from Java:", event.data);

        // 获取最后一个 bot 消息框的 ID
        const lastMessage = this.messages.find(msg => msg.sender === 'bot');

        if (lastMessage) {
          try {
            if (this.enevnt_data.includes("answer1_start")) {
              this.status = 1;
            }
            else if (this.enevnt_data.includes("answer2_start")) {
              this.status = 2;
            }
            else if (this.enevnt_data.includes("answer3_start")) {
              this.status = 3;
            }
            else {
              if (this.status == 1) {
                lastMessage.text += marked(this.enevnt_data);
              }
              else if (this.status == 2) {
                this.answer2 = this.enevnt_data;
                if (this.answer2.length != 0) {
                  this.process_answer2()
                }
                setTimeout(() => {
                  this.messages.push({ sender: 'bot', text: this.temp_answer });
                }, 1000);
              }
              else if (this.status == 3) {
                this.answer3 = this.enevnt_data;
                if (this.answer3.length != 0) {
                  console.log('Actual Graph Data:', this.answer3);
                  // 渲染知识图谱
                  this.renderGraph(this.answer3);
                }
              }
            }
          }catch (error) {
            console.error("Error fetching answer:", error);
            // 模拟AI的回复
            setTimeout(() => {
              this.messages.push({ sender: 'bot', text: error});
            }, 1000);
          }
        }
        else {
          // 如果没有现有的消息框，创建一个新的（备用）
          this.messages.push({ sender: 'bot', text: `<div>${marked(this.eventData)}</div>` });
        }
      };

      socket.onopen = () => {
        console.log("WebSocket connected to Java backend");
      };

      socket.onclose = () => {
        console.log("WebSocket connection closed");
      };
    },

    process_answer2(){
      this.temp_answer += "<div class='answer2-message'>";
      this.temp_answer += "<b>" + "参考文献：" + "</b>" + "<br>"

      this.title_list = []

      for(this.item of this.answer2){
        this.flag = 0
        for(this.title of this.title_list){
          if(this.item[2] == this.title){
            this.flag = 1
            break
          }
        }
        if(this.flag == 0){
          this.title_list.push(this.item[2])
        }
      }

      this.count = 1
      this.title_list.forEach(item=>{
        this.temp_answer += `${this.count}. ` + item + "<br>"
        this.count += 1
      })

      this.temp_answer += "</div>";
    },

    // process_answer2(){
    //   this.temp_answer += "<br><div class='answer2-message'>";
    //   this.entity_list = []
    //
    //   for(this.item of this.answer2){
    //     this.flag = 0
    //     for(this.entity of this.entity_list){
    //       if (this.item[0] == this.entity[0]) {
    //         this.entity[1] += this.item[1] + "<br>"
    //         this.entity[2] += this.item[2] + "<br>"
    //         this.flag = 1
    //         break
    //       }
    //     }
    //     if(this.flag == 0) {
    //       this.entity_list.push([this.item[0], this.item[1], this.item[2]+"<br>"])
    //     }
    //   }
    //
    //   this.entity_list.forEach(items => {
    //     this.temp_answer += "<b>" + "问题关键词：" + "</b>" + "<br>"
    //     this.temp_answer += items[0] + "<br>"
    //     this.temp_answer += "<b>" + "知识图谱匹配实体：" + "</b>" + "<br>"
    //     this.temp_answer += items[1] + "<br>"
    //     this.temp_answer += "<b>" + "参考文献：" + "</b>" + "<br>"
    //     this.temp_answer += items[2] + "<br><br>"
    //   })
    //
    //   this.temp_answer += "</div>";
    // },

    renderGraph(graphs) {
      const graphData = graphs[0]; // 假设只可视化第一个子图
      console.log(graphData)

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
          if (node.type == '标题') {
            uniqueNodes.push({
              id: node.id,
              name: node.label,
              value: node.type,
              draggable: true,
              label: {
                show: true,
                color: '#2ecc71',  // 设置字体颜色
                fontSize: 16,      // 设置字体大小
                fontFamily: 'Arial'  // 设置字体类型
              }
            });
          } else {
            uniqueNodes.push({
              id: node.id,
              name: node.type,
              value: node.label,
              draggable: true
            });
          }
        }
      });

      const edges = graphData.edges.map(edge => ({
        source: edge.source,
        target: edge.target,
        label: {
          show: false,
          formatter: edge.relationship
        }
      }));

      const option = {
        tooltip: {},
        animation: false,
        series: [
          {
            type: 'graph',
            layout: 'force',
            force: {
              repulsion: 500
            },
            data: uniqueNodes,
            links: edges,
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
  line-height: 1.8; /* 设置answer2的行间距 */
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

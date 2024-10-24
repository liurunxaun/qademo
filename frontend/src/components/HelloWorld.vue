<template>
  <div>
    <h1>{{ message }}</h1>
    <button @click="getMessageFromBackend">从后端获取数据</button>
  </div>
</template>

<script>
import axios from "axios"

export default {
  data() {
    return {
      message: "Hello from Vue.js"
    };
  },
  methods: {
    async getMessageFromBackend() {
      try {
        const response = await axios.get("http://localhost:8080/api/hello");
        this.message = response.data;
      } catch (error) {
        console.error("Error fetching data from backend:", error);
        this.message = "sad"
      }
    }
  }
};
</script>

<style scoped>
h1 {
  color: #42b983;
}
</style>

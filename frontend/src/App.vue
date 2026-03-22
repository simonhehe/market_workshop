<template>
  <div class="app-root">
    <div v-if="loading" class="loading">
      <p>加载中...</p>
    </div>
    <router-view v-else />
  </div>
</template>

<script>
export default {
  name: 'App',
  data() {
    return {
      loading: true
    }
  },
  mounted() {
    console.log('App component mounted')
    // 短暂延迟以确保路由已初始化
    setTimeout(() => {
      this.loading = false
    }, 100)
  }
}
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  background-color: #f5f5f5;
}

.app-root {
  min-height: 100vh;
}

button {
  cursor: pointer;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  font-size: 14px;
  transition: all 0.3s;
}

button:hover {
  opacity: 0.8;
}

button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

input, textarea, select {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

input:focus, textarea:focus, select:focus {
  outline: none;
  border-color: #409eff;
}

.loading {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  font-size: 18px;
  color: #666;
}
</style>

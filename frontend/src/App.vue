<template>
  <div class="app">
    <header class="header">
      <h1>企业内部福利商城 - Demo测试</h1>
      <p>JDK 21 + Spring Boot 3.X + Vue 3 运行环境测试</p>
    </header>
    
    <main class="main">
      <div class="test-section">
        <h2>前后端连通性测试</h2>
        
        <div class="test-buttons">
          <button @click="testHealth" :disabled="loading">
            {{ loading ? '测试中...' : '测试健康检查接口' }}
          </button>
          <button @click="testApi" :disabled="loading">
            {{ loading ? '测试中...' : '测试API接口' }}
          </button>
        </div>
        
        <div v-if="result" class="result">
          <h3>测试结果：</h3>
          <div class="result-item">
            <strong>状态：</strong>
            <span :class="result.success ? 'success' : 'error'">
              {{ result.success ? '✓ 成功' : '✗ 失败' }}
            </span>
          </div>
          <div class="result-item">
            <strong>消息：</strong>
            <span>{{ result.message }}</span>
          </div>
          <div v-if="result.data" class="result-item">
            <strong>数据：</strong>
            <pre>{{ JSON.stringify(result.data, null, 2) }}</pre>
          </div>
          <div v-if="result.error" class="result-item error">
            <strong>错误：</strong>
            <pre>{{ JSON.stringify(result.error, null, 2) }}</pre>
          </div>
        </div>
        
        <div v-if="error" class="error-message">
          <h3>错误信息：</h3>
          <pre>{{ error }}</pre>
        </div>
      </div>
      
      <div class="info-section">
        <h2>环境信息</h2>
        <ul>
          <li><strong>前端框架：</strong>Vue 3</li>
          <li><strong>构建工具：</strong>Vite</li>
          <li><strong>后端框架：</strong>Spring Boot 3.2.0</li>
          <li><strong>Java版本：</strong>JDK 21</li>
          <li><strong>数据库：</strong>SQLite</li>
          <li><strong>前端端口：</strong>3000</li>
          <li><strong>后端端口：</strong>8080</li>
        </ul>
      </div>
    </main>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'App',
  data() {
    return {
      loading: false,
      result: null,
      error: null
    }
  },
  methods: {
    async testHealth() {
      this.loading = true
      this.result = null
      this.error = null
      
      try {
        const response = await axios.get('/api/health')
        this.result = {
          success: response.data.success,
          message: response.data.message,
          data: response.data.data,
          error: response.data.error
        }
      } catch (err) {
        this.error = err.message
        this.result = {
          success: false,
          message: '请求失败',
          error: {
            code: err.response?.status || 'NETWORK_ERROR',
            message: err.message,
            details: err.response?.data || '无法连接到后端服务'
          }
        }
      } finally {
        this.loading = false
      }
    },
    
    async testApi() {
      this.loading = true
      this.result = null
      this.error = null
      
      try {
        const response = await axios.get('/api/health/test')
        this.result = {
          success: response.data.success,
          message: response.data.message,
          data: response.data.data,
          error: response.data.error
        }
      } catch (err) {
        this.error = err.message
        this.result = {
          success: false,
          message: '请求失败',
          error: {
            code: err.response?.status || 'NETWORK_ERROR',
            message: err.message,
            details: err.response?.data || '无法连接到后端服务'
          }
        }
      } finally {
        this.loading = false
      }
    }
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
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  min-height: 100vh;
}

.app {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.header {
  text-align: center;
  color: white;
  margin-bottom: 40px;
}

.header h1 {
  font-size: 2.5em;
  margin-bottom: 10px;
}

.header p {
  font-size: 1.2em;
  opacity: 0.9;
}

.main {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 30px;
}

.test-section, .info-section {
  background: white;
  border-radius: 10px;
  padding: 30px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
}

.test-section h2, .info-section h2 {
  margin-bottom: 20px;
  color: #333;
  border-bottom: 2px solid #667eea;
  padding-bottom: 10px;
}

.test-buttons {
  display: flex;
  gap: 15px;
  margin-bottom: 30px;
}

button {
  padding: 12px 24px;
  background: #667eea;
  color: white;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  font-size: 16px;
  transition: all 0.3s;
}

button:hover:not(:disabled) {
  background: #5568d3;
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(102, 126, 234, 0.4);
}

button:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.result {
  margin-top: 20px;
  padding: 20px;
  background: #f5f5f5;
  border-radius: 5px;
}

.result-item {
  margin-bottom: 15px;
}

.result-item strong {
  color: #333;
  margin-right: 10px;
}

.success {
  color: #28a745;
  font-weight: bold;
}

.error {
  color: #dc3545;
  font-weight: bold;
}

pre {
  background: #fff;
  padding: 15px;
  border-radius: 5px;
  overflow-x: auto;
  margin-top: 10px;
  border: 1px solid #ddd;
}

.error-message {
  margin-top: 20px;
  padding: 20px;
  background: #fee;
  border-radius: 5px;
  border: 1px solid #fcc;
}

.error-message h3 {
  color: #dc3545;
  margin-bottom: 10px;
}

.info-section ul {
  list-style: none;
}

.info-section li {
  padding: 10px 0;
  border-bottom: 1px solid #eee;
}

.info-section li:last-child {
  border-bottom: none;
}

.info-section li strong {
  color: #667eea;
  margin-right: 10px;
}

@media (max-width: 768px) {
  .main {
    grid-template-columns: 1fr;
  }
  
  .header h1 {
    font-size: 1.8em;
  }
  
  .test-buttons {
    flex-direction: column;
  }
}
</style>

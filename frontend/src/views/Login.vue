<template>
  <div class="login-container">
    <div class="login-box">
      <h1>企业内部福利商城</h1>
      <h2>用户登录</h2>
      
      <div class="test-accounts">
        <h3>测试账号（点击快速填充）</h3>
        <div class="account-list">
          <div class="account-item" @click="fillAccount('user1', 'password1')">
            <strong>用户1：</strong>user1 / password1（初始积分100）
          </div>
          <div class="account-item" @click="fillAccount('user2', 'password2')">
            <strong>用户2：</strong>user2 / password2（初始积分200）
          </div>
          <div class="account-item" @click="fillAccount('user3', 'password3')">
            <strong>用户3：</strong>user3 / password3（初始积分150）
          </div>
          <div class="account-item admin" @click="fillAccount('admin', 'admin123')">
            <strong>管理员：</strong>admin / admin123
          </div>
        </div>
      </div>
      
      <form @submit.prevent="handleLogin" class="login-form">
        <div class="form-group">
          <label>用户名</label>
          <input 
            v-model="form.username" 
            type="text" 
            placeholder="请输入用户名"
            required
            @keyup.enter="handleLogin"
          />
        </div>
        
        <div class="form-group">
          <label>密码</label>
          <input 
            v-model="form.password" 
            type="password" 
            placeholder="请输入密码"
            required
            @keyup.enter="handleLogin"
          />
        </div>
        
        <div v-if="error" class="error-message">
          {{ error }}
        </div>
        
        <button type="submit" :disabled="loading" class="login-btn">
          {{ loading ? '登录中...' : '登录' }}
        </button>
      </form>
    </div>
  </div>
</template>

<script>
import { authApi } from '../api/auth'

export default {
  name: 'Login',
  data() {
    return {
      form: {
        username: '',
        password: ''
      },
      loading: false,
      error: ''
    }
  },
  methods: {
    fillAccount(username, password) {
      this.form.username = username
      this.form.password = password
    },
    async handleLogin() {
      if (!this.form.username || !this.form.password) {
        this.error = '请输入用户名和密码'
        return
      }
      
      this.loading = true
      this.error = ''
      
      try {
        const response = await authApi.login(this.form.username, this.form.password)
        
        if (response.success) {
          localStorage.setItem('token', response.data.token)
          localStorage.setItem('userInfo', JSON.stringify({
            userId: response.data.userId,
            username: response.data.username,
            role: response.data.userType
          }))
          
          // 根据角色跳转
          if (response.data.userType === 'admin') {
            this.$router.push('/admin')
          } else {
            this.$router.push('/')
          }
        } else {
          this.error = response.message || '登录失败'
        }
      } catch (err) {
        this.error = err.response?.data?.message || err.message || '登录失败，请检查网络连接'
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.login-box {
  background: white;
  border-radius: 12px;
  padding: 40px;
  width: 100%;
  max-width: 500px;
  box-shadow: 0 10px 40px rgba(0,0,0,0.1);
}

.login-box h1 {
  text-align: center;
  color: #333;
  margin-bottom: 10px;
}

.login-box h2 {
  text-align: center;
  color: #666;
  font-size: 18px;
  font-weight: normal;
  margin-bottom: 30px;
}

.test-accounts {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 30px;
}

.test-accounts h3 {
  font-size: 14px;
  color: #666;
  margin-bottom: 12px;
}

.account-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.account-item {
  padding: 8px 12px;
  background: white;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
  font-size: 13px;
}

.account-item:hover {
  background: #e3f2fd;
  transform: translateX(4px);
}

.account-item.admin {
  border-left: 3px solid #f44336;
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-group label {
  font-size: 14px;
  color: #333;
  font-weight: 500;
}

.form-group input {
  width: 100%;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
}

.form-group input:focus {
  border-color: #667eea;
  outline: none;
}

.error-message {
  color: #f44336;
  font-size: 14px;
  padding: 8px;
  background: #ffebee;
  border-radius: 4px;
}

.login-btn {
  width: 100%;
  padding: 12px;
  background: #667eea;
  color: white;
  border-radius: 6px;
  font-size: 16px;
  font-weight: 500;
  margin-top: 10px;
}

.login-btn:hover {
  background: #5568d3;
}
</style>

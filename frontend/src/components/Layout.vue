<template>
  <div class="layout">
    <header class="header">
      <div class="header-content">
        <h1 class="logo">企业内部福利商城</h1>
        <nav class="nav">
          <router-link to="/">首页</router-link>
          <router-link to="/products">商品</router-link>
          <router-link to="/cart">购物车</router-link>
          <router-link to="/orders">订单</router-link>
          <router-link to="/favorites">收藏</router-link>
          <router-link to="/profile">个人中心</router-link>
          <template v-if="userInfo.role === 'admin'">
            <router-link to="/admin">管理后台</router-link>
          </template>
          <a href="#" @click.prevent="handleLogout" class="logout">退出</a>
        </nav>
        <div class="user-info" v-if="userInfo.username">
          <span>欢迎，{{ userInfo.username }}</span>
          <span class="points">积分：{{ userPoints }}</span>
        </div>
      </div>
    </header>
    
    <main class="main-content">
      <slot />
    </main>
  </div>
</template>

<script>
import { userApi } from '../api/user'

export default {
  name: 'Layout',
  data() {
    return {
      userInfo: {},
      userPoints: 0
    }
  },
  mounted() {
    this.loadUserInfo()
  },
  methods: {
    async loadUserInfo() {
      const userInfoStr = localStorage.getItem('userInfo')
      if (userInfoStr) {
        this.userInfo = JSON.parse(userInfoStr)
      }
      
      try {
        const response = await userApi.getUserInfo()
        if (response.success) {
          this.userPoints = response.data.points || 0
        }
      } catch (err) {
        console.error('获取用户信息失败', err)
      }
    },
    handleLogout() {
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      this.$router.push('/login')
    }
  }
}
</script>

<style scoped>
.layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.header {
  background: white;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 60px;
}

.logo {
  font-size: 20px;
  color: #333;
  font-weight: 600;
}

.nav {
  display: flex;
  gap: 20px;
  align-items: center;
}

.nav a {
  text-decoration: none;
  color: #666;
  font-size: 14px;
  padding: 8px 12px;
  border-radius: 4px;
  transition: all 0.3s;
}

.nav a:hover,
.nav a.router-link-active {
  color: #667eea;
  background: #f0f4ff;
}

.logout {
  color: #f44336 !important;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 16px;
  font-size: 14px;
  color: #666;
}

.points {
  color: #ff9800;
  font-weight: 600;
}

.main-content {
  flex: 1;
  max-width: 1200px;
  width: 100%;
  margin: 0 auto;
  padding: 20px;
}
</style>

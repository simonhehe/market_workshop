<template>
  <Layout>
    <div class="profile-page">
      <h2>个人中心</h2>

      <div v-if="loading" class="state-card">
        用户信息加载中...
      </div>

      <div v-else-if="errorMessage" class="state-card error-state">
        {{ errorMessage }}
      </div>

      <div class="profile-card" v-else-if="userInfo">
        <div class="info-item">
          <label>用户名：</label>
          <span>{{ userInfo.username }}</span>
        </div>
        <div class="info-item">
          <label>邮箱：</label>
          <span>{{ userInfo.email || '未设置' }}</span>
        </div>
        <div class="info-item">
          <label>手机：</label>
          <span>{{ userInfo.phone || '未设置' }}</span>
        </div>
        <div class="info-item">
          <label>积分：</label>
          <span class="points">{{ userInfo.points || 0 }}</span>
        </div>
        <div class="info-item">
          <label>部门：</label>
          <span>{{ userInfo.department || '未设置' }}</span>
        </div>
        <div class="info-item">
          <label>职位：</label>
          <span>{{ userInfo.position || '未设置' }}</span>
        </div>
      </div>
    </div>
  </Layout>
</template>

<script>
import Layout from '../components/Layout.vue'
import { userApi } from '../api/exports'

export default {
  name: 'Profile',
  components: { Layout },
  data() {
    return {
      userInfo: null,
      loading: true,
      errorMessage: ''
    }
  },
  async mounted() {
    await this.loadUserInfo()
  },
  methods: {
    async loadUserInfo() {
      this.loading = true
      this.errorMessage = ''

      try {
        const response = await userApi.getUserInfo()
        if (response.success) {
          this.userInfo = response.data
        } else {
          this.errorMessage = '用户信息暂时不可用'
        }
      } catch (err) {
        console.error('加载用户信息失败', err)
        this.errorMessage = err.response?.data?.message || '加载用户信息失败，请稍后重试'
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style scoped>
.profile-page {
  padding: 20px 0;
}

.profile-page h2 {
  font-size: 24px;
  margin-bottom: 24px;
}

.state-card {
  background: white;
  border-radius: 8px;
  padding: 48px 24px;
  text-align: center;
  color: #666;
  font-size: 16px;
}

.error-state {
  color: #f44336;
}

.profile-card {
  background: white;
  border-radius: 8px;
  padding: 24px;
}

.info-item {
  display: flex;
  padding: 16px 0;
  border-bottom: 1px solid #f5f5f5;
}

.info-item:last-child {
  border-bottom: none;
}

.info-item label {
  width: 120px;
  color: #666;
  font-weight: 500;
}

.info-item span {
  color: #333;
}

.points {
  color: #ff9800;
  font-weight: 600;
  font-size: 18px;
}
</style>

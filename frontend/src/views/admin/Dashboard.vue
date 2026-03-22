<template>
  <Layout>
    <div class="admin-dashboard">
      <h2>管理后台</h2>
      <div class="dashboard-stats">
        <div class="stat-card">
          <h3>商品总数</h3>
          <p class="stat-value">{{ stats.products || 0 }}</p>
        </div>
        <div class="stat-card">
          <h3>订单总数</h3>
          <p class="stat-value">{{ stats.orders || 0 }}</p>
        </div>
        <div class="stat-card">
          <h3>用户总数</h3>
          <p class="stat-value">{{ stats.users || 0 }}</p>
        </div>
      </div>
      <div class="admin-nav">
        <router-link to="/admin/products" class="nav-card">商品管理</router-link>
        <router-link to="/admin/orders" class="nav-card">订单管理</router-link>
        <router-link to="/admin/users" class="nav-card">用户管理</router-link>
      </div>
    </div>
  </Layout>
</template>

<script>
import Layout from '../../components/Layout.vue'

export default {
  name: 'AdminDashboard',
  components: { Layout },
  data() {
    return {
      stats: {
        products: 0,
        orders: 0,
        users: 0
      }
    }
  }
}
</script>

<style scoped>
.admin-dashboard {
  padding: 20px 0;
}

.dashboard-stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  margin-bottom: 40px;
}

.stat-card {
  background: white;
  border-radius: 8px;
  padding: 24px;
  text-align: center;
}

.stat-value {
  font-size: 32px;
  color: #667eea;
  font-weight: 600;
  margin-top: 12px;
}

.admin-nav {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.nav-card {
  background: white;
  border-radius: 8px;
  padding: 40px;
  text-align: center;
  text-decoration: none;
  color: #333;
  font-size: 18px;
  transition: all 0.3s;
}

.nav-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 16px rgba(0,0,0,0.15);
  color: #667eea;
}
</style>

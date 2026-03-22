<template>
  <Layout>
    <div class="admin-users">
      <h2>用户管理</h2>
      <div class="users-table">
        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>用户名</th>
              <th>邮箱</th>
              <th>积分</th>
              <th>角色</th>
              <th>状态</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="user in users" :key="user.id">
              <td>{{ user.id }}</td>
              <td>{{ user.username }}</td>
              <td>{{ user.email }}</td>
              <td>{{ user.points }}</td>
              <td>{{ user.role }}</td>
              <td>{{ user.status }}</td>
              <td>
                <button @click="adjustPoints(user.id)">调整积分</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </Layout>
</template>

<script>
import Layout from '../../components/Layout.vue'
import api from '../../api/index'

export default {
  name: 'AdminUsers',
  components: { Layout },
  data() {
    return {
      users: []
    }
  },
  async mounted() {
    await this.loadUsers()
  },
  methods: {
    async loadUsers() {
      try {
        const response = await api.get('/admin/users')
        if (response.success) {
          this.users = response.data
        }
      } catch (err) {
        console.error('加载用户失败', err)
      }
    },
    async adjustPoints(userId) {
      const points = prompt('请输入要调整的积分（正数为增加，负数为减少）：')
      if (points === null) return
      
      try {
        const response = await api.put(`/admin/users/${userId}/points`, { points: parseInt(points) })
        if (response.success) {
          await this.loadUsers()
        }
      } catch (err) {
        alert(err.response?.data?.message || '调整失败')
      }
    }
  }
}
</script>

<style scoped>
.admin-users {
  padding: 20px 0;
}

.users-table {
  background: white;
  border-radius: 8px;
  overflow: hidden;
}

table {
  width: 100%;
  border-collapse: collapse;
}

th, td {
  padding: 12px;
  text-align: left;
  border-bottom: 1px solid #eee;
}

th {
  background: #f8f9fa;
  font-weight: 600;
}
</style>

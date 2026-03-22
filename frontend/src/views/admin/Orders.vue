<template>
  <Layout>
    <div class="admin-orders">
      <h2>订单管理</h2>
      <div class="orders-table">
        <table>
          <thead>
            <tr>
              <th>订单号</th>
              <th>用户ID</th>
              <th>积分</th>
              <th>状态</th>
              <th>时间</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="order in orders" :key="order.id">
              <td>{{ order.orderNo }}</td>
              <td>{{ order.userId }}</td>
              <td>{{ order.totalPoints }}</td>
              <td>{{ order.status }}</td>
              <td>{{ formatDate(order.createdAt) }}</td>
              <td>
                <select @change="updateStatus(order.id, $event.target.value)">
                  <option value="pending" :selected="order.status === 'pending'">待处理</option>
                  <option value="processing" :selected="order.status === 'processing'">处理中</option>
                  <option value="shipped" :selected="order.status === 'shipped'">已发货</option>
                  <option value="completed" :selected="order.status === 'completed'">已完成</option>
                </select>
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
  name: 'AdminOrders',
  components: { Layout },
  data() {
    return {
      orders: []
    }
  },
  async mounted() {
    await this.loadOrders()
  },
  methods: {
    async loadOrders() {
      try {
        const response = await api.get('/admin/orders')
        if (response.success) {
          this.orders = response.data
        }
      } catch (err) {
        console.error('加载订单失败', err)
      }
    },
    formatDate(dateStr) {
      if (!dateStr) return ''
      return new Date(dateStr).toLocaleString('zh-CN')
    },
    async updateStatus(id, status) {
      try {
        const response = await api.put(`/admin/orders/${id}/status`, { status })
        if (response.success) {
          await this.loadOrders()
        }
      } catch (err) {
        alert(err.response?.data?.message || '更新失败')
      }
    }
  }
}
</script>

<style scoped>
.admin-orders {
  padding: 20px 0;
}

.orders-table {
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

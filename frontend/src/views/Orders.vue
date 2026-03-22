<template>
  <Layout>
    <div class="orders-page">
      <h2>我的订单</h2>

      <div class="filter-row">
        <label for="order-status-filter">状态筛选</label>
        <select id="order-status-filter" v-model="filterStatus" class="status-filter" @change="loadOrders">
          <option value="all">全部</option>
          <option value="pending">待处理</option>
          <option value="processing">处理中</option>
          <option value="shipped">已发货</option>
          <option value="completed">已完成</option>
          <option value="cancelled">已取消</option>
        </select>
      </div>
      
      <div v-if="orders.length === 0" class="empty">
        暂无订单
      </div>
      
      <div v-else class="orders-list">
        <div v-for="order in orders" :key="order.id" class="order-card">
          <div class="order-header">
            <span class="order-no">订单号：{{ order.orderNo }}</span>
            <span :class="['order-status', getStatusClass(order.status)]">
              {{ getStatusText(order.status) }}
            </span>
          </div>
          <div class="order-info">
            <p>积分：{{ order.totalPoints }}</p>
            <p>时间：{{ formatDate(order.createdAt) }}</p>
          </div>
          <div class="order-actions">
            <button @click="viewDetail(order.id)">查看详情</button>
            <button 
              v-if="order.status === 'pending'" 
              @click="cancelOrder(order.id)"
              class="cancel-btn"
            >
              取消订单
            </button>
          </div>
        </div>
      </div>
    </div>
  </Layout>
</template>

<script>
import Layout from '../components/Layout.vue'
import { orderApi } from '../api/exports'

export default {
  name: 'Orders',
  components: { Layout },
  data() {
    return {
      orders: [],
      filterStatus: 'all'
    }
  },
  async mounted() {
    await this.loadOrders()
  },
  methods: {
    async loadOrders() {
      try {
        const params =
          this.filterStatus === 'all' ? {} : { status: this.filterStatus }
        const response = await orderApi.getOrders(params)
        if (response.success) {
          this.orders = response.data || []
        }
      } catch (err) {
        console.error('加载订单失败', err)
      }
    },
    getStatusText(status) {
      const statusMap = {
        'pending': '待处理',
        'processing': '处理中',
        'shipped': '已发货',
        'completed': '已完成',
        'cancelled': '已取消'
      }
      return statusMap[status] || status
    },
    getStatusClass(status) {
      const classMap = {
        'pending': 'status-pending',
        'processing': 'status-processing',
        'shipped': 'status-shipped',
        'completed': 'status-completed',
        'cancelled': 'status-cancelled'
      }
      return classMap[status] || ''
    },
    formatDate(dateStr) {
      if (!dateStr) return ''
      return new Date(dateStr).toLocaleString('zh-CN')
    },
    viewDetail(id) {
      this.$router.push(`/orders/${id}`)
    },
    async cancelOrder(id) {
      if (!confirm('确定要取消这个订单吗？')) return
      
      try {
        const response = await orderApi.cancelOrder(id)
        if (response.success) {
          alert('订单已取消')
          await this.loadOrders()
        }
      } catch (err) {
        alert(err.response?.data?.message || '取消失败')
      }
    }
  }
}
</script>

<style scoped>
.orders-page {
  padding: 20px 0;
}

.orders-page h2 {
  font-size: 24px;
  margin-bottom: 24px;
}

.filter-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
}

.filter-row label {
  font-size: 14px;
  color: #666;
}

.status-filter {
  padding: 8px 12px;
  border-radius: 4px;
  border: 1px solid #ccc;
  min-width: 160px;
}

.empty {
  text-align: center;
  padding: 60px 20px;
  color: #999;
  font-size: 16px;
}

.orders-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.order-card {
  background: white;
  border-radius: 8px;
  padding: 20px;
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 16px;
  border-bottom: 1px solid #eee;
}

.order-no {
  font-size: 14px;
  color: #666;
}

.order-status {
  padding: 4px 12px;
  border-radius: 4px;
  font-size: 14px;
}

.status-pending {
  background: #fff3cd;
  color: #856404;
}

.status-processing {
  background: #d1ecf1;
  color: #0c5460;
}

.status-shipped {
  background: #d4edda;
  color: #155724;
}

.status-completed {
  background: #d1ecf1;
  color: #0c5460;
}

.status-cancelled {
  background: #f8d7da;
  color: #721c24;
}

.order-info {
  margin-bottom: 16px;
  color: #666;
  font-size: 14px;
}

.order-actions {
  display: flex;
  gap: 12px;
}

.order-actions button {
  padding: 8px 16px;
  background: #667eea;
  color: white;
  border-radius: 4px;
}

.cancel-btn {
  background: #f44336 !important;
}
</style>

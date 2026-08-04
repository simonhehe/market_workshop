<template>
  <Layout>
    <div class="order-detail">
      <h2>订单详情</h2>

      <div v-if="loading" class="state-card">
        订单加载中...
      </div>

      <div v-else-if="errorMessage" class="state-card error-state">
        {{ errorMessage }}
      </div>

      <div v-else-if="order" class="detail-card">
        <div class="order-header">
          <span>订单号：{{ order.orderNo }}</span>
          <span :class="['status', getStatusClass(order.status)]">
            {{ getStatusText(order.status) }}
          </span>
        </div>
        
        <div class="order-items">
          <h3>商品列表</h3>
          <div v-for="item in orderItems" :key="item.id" class="order-item">
            <span>{{ item.productName }}</span>
            <span>{{ item.pointsRequired }} 积分 × {{ item.quantity }}</span>
            <span>{{ item.subtotalPoints }} 积分</span>
          </div>
        </div>
        
        <div class="order-summary">
          <p>总计：{{ order.totalPoints }} 积分</p>
          <p>收货地址：{{ order.receiverAddress }}</p>
          <p>收货人：{{ order.receiver }} {{ order.receiverPhone }}</p>
          <p v-if="order.remark">备注：{{ order.remark }}</p>
          <p v-else>备注：无</p>
        </div>
      </div>
    </div>
  </Layout>
</template>

<script>
import Layout from '../components/Layout.vue'
import { orderApi } from '../api/exports'

export default {
  name: 'OrderDetail',
  components: { Layout },
  data() {
    return {
      order: null,
      orderItems: [],
      loading: true,
      errorMessage: ''
    }
  },
  async mounted() {
    await this.loadOrder()
  },
  methods: {
    async loadOrder() {
      this.loading = true
      this.errorMessage = ''

      try {
        const id = this.$route.params.id
        const response = await orderApi.getOrderById(id)
        if (response.success) {
          this.order = response.data.order
          this.orderItems = response.data.orderItems || []
        } else {
          this.errorMessage = '订单不存在或暂时无法访问'
        }
      } catch (err) {
        console.error('加载订单失败', err)
        this.errorMessage = err.response?.data?.message || '订单加载失败，请稍后重试'
      } finally {
        this.loading = false
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
      return `status-${status}`
    }
  }
}
</script>

<style scoped>
.order-detail {
  padding: 20px 0;
}

.order-detail h2 {
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

.detail-card {
  background: white;
  border-radius: 8px;
  padding: 24px;
}

.order-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #eee;
}

.order-items {
  margin-bottom: 24px;
}

.order-items h3 {
  font-size: 18px;
  margin-bottom: 16px;
}

.order-item {
  display: flex;
  justify-content: space-between;
  padding: 12px 0;
  border-bottom: 1px solid #f5f5f5;
}

.order-summary {
  padding-top: 16px;
  border-top: 1px solid #eee;
}

.order-summary p {
  margin-bottom: 8px;
  color: #666;
}
</style>

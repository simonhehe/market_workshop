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
        </div>

        <div class="order-actions">
          <button
            v-if="order.status === 'pending'"
            type="button"
            class="cancel-btn"
            @click="cancelOrder"
          >
            取消订单
          </button>
          <button
            v-if="order.status === 'shipped'"
            type="button"
            class="confirm-btn"
            @click="confirmReceipt"
          >
            确认收货
          </button>
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
        const raw = this.$route.params.id
        if (raw === 'create' || !/^\d+$/.test(String(raw))) {
          this.errorMessage =
            raw === 'create'
              ? '这是下单页路径，请使用「确认下单」页面完成结算。'
              : '无效的订单编号'
          return
        }
        const id = raw
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
    },
    async cancelOrder() {
      if (!this.order || !confirm('确定要取消这个订单吗？')) return
      try {
        const response = await orderApi.cancelOrder(this.order.id)
        if (response.success) {
          alert('订单已取消')
          await this.loadOrder()
        }
      } catch (err) {
        alert(err.response?.data?.message || '取消失败')
      }
    },
    async confirmReceipt() {
      if (!this.order || !confirm('确认已收到商品？')) return
      try {
        const response = await orderApi.confirmReceipt(this.order.id)
        if (response.success) {
          alert('确认收货成功')
          await this.loadOrder()
        }
      } catch (err) {
        alert(err.response?.data?.message || '确认收货失败')
      }
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

.order-actions {
  margin-top: 24px;
  padding-top: 16px;
  border-top: 1px solid #eee;
  display: flex;
  gap: 12px;
}

.order-actions button {
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  color: white;
  cursor: pointer;
}

.order-actions .cancel-btn {
  background: #f44336;
}

.order-actions .confirm-btn {
  background: #4caf50;
}
</style>

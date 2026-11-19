<template>
  <Layout>
    <div class="checkout-page">
      <h2>确认订单</h2>

      <div v-if="loading" class="loading-state">结算信息加载中...</div>

      <div v-else-if="createdOrder" class="success-card">
        <h3>下单成功</h3>
        <p>订单号：<span class="success-order-no">{{ createdOrder.orderNo }}</span></p>
        <p>实付积分：<span class="success-points">{{ createdOrder.totalPoints }}</span></p>
        <p>订单状态：<span class="success-status">{{ statusText(createdOrder.status) }}</span></p>
        <div class="success-actions">
          <button class="detail-btn" @click="goToOrder">查看订单</button>
          <button class="continue-btn" @click="continueShopping">继续购物</button>
        </div>
      </div>

      <div v-else-if="redirected" class="redirect-notice">正在返回购物车...</div>

      <div v-else class="checkout-form">
        <div v-if="errorMessage" class="error-banner">{{ errorMessage }}</div>

        <section class="checkout-section address-section">
          <h3>收货地址</h3>
          <p v-if="addresses.length === 0" class="empty-address">暂无收货地址，请先添加</p>
          <div
            v-for="address in addresses"
            :key="address.id"
            class="address-card"
            :class="{ selected: selectedAddressId === address.id }"
            @click="selectAddress(address.id)"
          >
            <p class="address-receiver">{{ address.receiver }} {{ address.phone }}</p>
            <p class="address-detail">
              {{ address.province }} {{ address.city }} {{ address.district }} {{ address.detailAddress }}
            </p>
          </div>
        </section>

        <section class="checkout-section items-section">
          <h3>商品清单</h3>
          <div
            v-for="item in items"
            :key="item.cartId"
            class="checkout-item"
            :class="{ 'stock-warning': item.quantity > item.stock || errorCode === 'STOCK_INSUFFICIENT' }"
          >
            <div class="item-name">{{ item.name }}</div>
            <div class="item-points">{{ item.pointsRequired }} 积分</div>
            <div class="item-quantity">
              <button class="decrease-btn" :disabled="item.quantity <= 1" @click="decreaseQuantity(item)">-</button>
              <span class="quantity-value">{{ item.quantity }}</span>
              <button class="increase-btn" :disabled="item.quantity >= item.stock" @click="increaseQuantity(item)">+</button>
            </div>
            <div class="item-subtotal">{{ item.pointsRequired * item.quantity }} 积分</div>
            <div v-if="item.quantity > item.stock" class="item-stock-error">
              库存不足：仅剩 {{ item.stock }} 件
            </div>
          </div>
        </section>

        <section class="checkout-section remark-section">
          <label for="order-remark">订单备注</label>
          <textarea
            id="order-remark"
            class="remark-input"
            v-model="remark"
            maxlength="100"
            placeholder="选填，100 字以内"
          ></textarea>
        </section>

        <section class="checkout-section summary-section">
          <p>共 {{ totalQuantity }} 件商品</p>
          <p>商品合计：{{ goodsPoints }} 积分</p>
          <p>运费：0 积分</p>
          <p class="payable-points">应付积分：{{ payablePoints }}</p>
          <p>当前积分：{{ currentPoints }}</p>
          <p class="balance-after">下单后余额：{{ balanceAfter }}</p>
        </section>

        <div class="submit-bar">
          <div v-if="selectedAddress" class="shipping-info">
            寄送至：{{ selectedAddress.province }} {{ selectedAddress.city }}
            {{ selectedAddress.district }} {{ selectedAddress.detailAddress }}
            （{{ selectedAddress.receiver }} {{ selectedAddress.phone }}）
          </div>
          <div v-else class="shipping-info">请选择收货地址</div>
          <button class="submit-btn" :disabled="submitting" @click="submitOrder">
            {{ submitting ? '提交中...' : '提交订单' }}
          </button>
        </div>
      </div>
    </div>
  </Layout>
</template>

<script>
import Layout from '../components/Layout.vue'
import { cartApi, addressApi, productApi, orderApi, userApi } from '../api/exports'

export default {
  name: 'Checkout',
  components: { Layout },
  data() {
    return {
      loading: true,
      redirected: false,
      submitting: false,
      errorMessage: '',
      errorCode: '',
      addresses: [],
      selectedAddressId: null,
      items: [],
      remark: '',
      currentPoints: 0,
      createdOrder: null
    }
  },
  computed: {
    selectedAddress() {
      return this.addresses.find(address => address.id === this.selectedAddressId) || null
    },
    totalQuantity() {
      return this.items.reduce((total, item) => total + item.quantity, 0)
    },
    goodsPoints() {
      return this.items.reduce((total, item) => total + item.pointsRequired * item.quantity, 0)
    },
    payablePoints() {
      return this.goodsPoints
    },
    balanceAfter() {
      return this.currentPoints - this.payablePoints
    }
  },
  async mounted() {
    await this.loadCheckout()
  },
  methods: {
    async loadCheckout() {
      try {
        const [cartResponse, addressResponse, userResponse] = await Promise.all([
          cartApi.getCart(),
          addressApi.getAddresses(),
          userApi.getUserInfo()
        ])

        if (!cartResponse.success) {
          this.errorMessage = cartResponse.message || '购物车加载失败'
          return
        }

        const selectedItems = (cartResponse.data.cartItems || [])
          .filter(item => item.isSelected === 1)

        if (selectedItems.length === 0) {
          this.redirected = true
          this.$router.replace({ path: '/cart', query: { notice: 'empty' } })
          return
        }

        this.addresses = addressResponse.success ? (addressResponse.data || []) : []
        this.currentPoints = userResponse.success ? (userResponse.data.points || 0) : 0

        const loadedItems = []
        for (const cartItem of selectedItems) {
          const productResponse = await productApi.getProductById(cartItem.productId)
          if (productResponse.success) {
            const product = productResponse.data
            loadedItems.push({
              cartId: cartItem.id,
              productId: product.id,
              name: product.name,
              pointsRequired: product.pointsRequired,
              stock: product.stock,
              quantity: cartItem.quantity
            })
          }
        }
        this.items = loadedItems
      } catch (err) {
        this.errorMessage = err.response?.data?.message || '结算信息加载失败，请稍后重试'
      } finally {
        this.loading = false
      }
    },
    selectAddress(id) {
      this.selectedAddressId = id
    },
    increaseQuantity(item) {
      if (item.quantity < item.stock) {
        item.quantity += 1
      }
    },
    decreaseQuantity(item) {
      if (item.quantity > 1) {
        item.quantity -= 1
      }
    },
    validateBeforeSubmit() {
      this.errorCode = ''
      if (!this.selectedAddressId) {
        this.errorCode = 'ADDR_REQUIRED'
        this.errorMessage = '请选择收货地址后再提交订单'
        return false
      }

      const stockProblem = this.items.find(item => item.quantity > item.stock)
      if (stockProblem) {
        this.errorCode = 'STOCK_INSUFFICIENT'
        this.errorMessage = `库存不足：商品「${stockProblem.name}」仅剩 ${stockProblem.stock} 件，需要 ${stockProblem.quantity} 件`
        return false
      }

      if (this.currentPoints < this.payablePoints) {
        this.errorCode = 'POINTS_INSUFFICIENT'
        this.errorMessage = `积分不足：当前 ${this.currentPoints}，需支付 ${this.payablePoints}，还差 ${this.payablePoints - this.currentPoints}`
        return false
      }

      return true
    },
    async submitOrder() {
      if (this.submitting) {
        return
      }
      this.errorMessage = ''
      if (!this.validateBeforeSubmit()) {
        return
      }

      this.submitting = true
      try {
        const response = await orderApi.createOrder({
          addressId: this.selectedAddressId,
          remark: this.remark
        })
        if (response.success) {
          this.createdOrder = response.data
        } else {
          this.applyServerError(response.error, response.message)
        }
      } catch (err) {
        const data = err.response?.data
        this.applyServerError(data?.error, data?.message)
      } finally {
        this.submitting = false
      }
    },
    applyServerError(error, fallbackMessage) {
      this.errorCode = error?.code || ''
      this.errorMessage = error?.message || fallbackMessage || '下单失败，请稍后重试'
    },
    statusText(status) {
      const statusMap = {
        pending: '待处理',
        processing: '处理中',
        shipped: '已发货',
        completed: '已完成',
        cancelled: '已取消'
      }
      return statusMap[status] || status
    },
    goToOrder() {
      this.$router.push(`/orders/${this.createdOrder.id}`)
    },
    continueShopping() {
      this.$router.push('/products')
    }
  }
}
</script>

<style scoped>
.checkout-page {
  padding: 20px 0;
}

.checkout-page h2 {
  font-size: 24px;
  margin-bottom: 20px;
}

.loading-state,
.redirect-notice {
  background: white;
  border-radius: 8px;
  padding: 48px 24px;
  text-align: center;
  color: #666;
}

.error-banner {
  background: #fff1f0;
  border: 1px solid #ffa39e;
  color: #cf1322;
  border-radius: 6px;
  padding: 12px 16px;
  margin-bottom: 16px;
}

.checkout-section {
  background: white;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 16px;
}

.checkout-section h3 {
  font-size: 18px;
  margin-bottom: 16px;
}

.address-card {
  border: 1px solid #e5e5e5;
  border-radius: 6px;
  padding: 14px 16px;
  margin-bottom: 12px;
  cursor: pointer;
}

.address-card:last-child {
  margin-bottom: 0;
}

.address-card.selected {
  border-color: #e1251b;
  background: #fff5f5;
}

.address-receiver {
  font-weight: 600;
  margin-bottom: 6px;
}

.address-detail {
  color: #666;
  font-size: 14px;
}

.checkout-item {
  display: grid;
  grid-template-columns: 2fr 1fr 1.2fr 1fr;
  gap: 12px;
  align-items: center;
  padding: 14px 0;
  border-bottom: 1px solid #f0f0f0;
}

.checkout-item:last-child {
  border-bottom: none;
}

.checkout-item.stock-warning {
  background: #fff1f0;
  border-radius: 6px;
  padding-left: 12px;
  padding-right: 12px;
}

.item-name {
  font-weight: 600;
}

.item-quantity {
  display: flex;
  align-items: center;
  gap: 8px;
}

.item-quantity button {
  width: 32px;
  height: 32px;
  border: 1px solid #ddd;
  border-radius: 4px;
  background: #fafafa;
}

.item-quantity button:disabled {
  color: #bbb;
  cursor: not-allowed;
}

.item-subtotal {
  color: #e1251b;
  font-weight: 600;
  text-align: right;
}

.item-stock-error {
  grid-column: 1 / -1;
  color: #cf1322;
  font-size: 13px;
}

.remark-input {
  width: 100%;
  min-height: 88px;
  border: 1px solid #ddd;
  border-radius: 6px;
  padding: 10px 12px;
  resize: vertical;
  font: inherit;
}

.summary-section p {
  margin-bottom: 8px;
  color: #666;
}

.payable-points {
  color: #e1251b;
  font-size: 20px;
  font-weight: 700;
}

.balance-after {
  color: #333;
  font-weight: 600;
}

.submit-bar {
  position: sticky;
  bottom: 0;
  background: white;
  border-radius: 8px;
  padding: 16px 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 -2px 12px rgba(0, 0, 0, 0.06);
}

.shipping-info {
  color: #666;
  font-size: 14px;
}

.submit-btn {
  min-width: 160px;
  padding: 12px 32px;
  background: #e1251b;
  color: white;
  border-radius: 6px;
  font-size: 16px;
}

.submit-btn:disabled {
  background: #f5a5a0;
  cursor: not-allowed;
}

.success-card {
  background: white;
  border-radius: 8px;
  padding: 32px;
  text-align: center;
}

.success-card h3 {
  color: #2e7d32;
  font-size: 22px;
  margin-bottom: 20px;
}

.success-card p {
  margin-bottom: 10px;
  color: #666;
}

.success-order-no,
.success-points,
.success-status {
  color: #333;
  font-weight: 600;
}

.success-actions {
  display: flex;
  justify-content: center;
  gap: 12px;
  margin-top: 24px;
}

.success-actions button {
  padding: 10px 24px;
  border-radius: 6px;
}

.detail-btn {
  background: #e1251b;
  color: white;
}

.continue-btn {
  background: #f5f5f5;
  color: #333;
}
</style>

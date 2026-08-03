<template>
  <Layout>
    <div class="order-create-page">
      <h2>下单结算</h2>

      <div v-if="loading" class="state-card">
        加载中...
      </div>

      <div v-else-if="selectedItems.length === 0" class="state-card">
        购物车中没有选中的商品
        <p><router-link to="/cart">返回购物车</router-link></p>
      </div>

      <div v-else>
        <section class="section-card">
          <h3>收货地址</h3>
          <label for="address-select">选择地址（必选）</label>
          <select id="address-select" v-model="selectedAddressId">
            <option value="">请选择收货地址</option>
            <option
              v-for="addr in addresses"
              :key="addr.id"
              :value="addr.id"
            >
              {{ formatAddress(addr) }}
            </option>
          </select>
          <p v-if="addressError" class="error-message">{{ addressError }}</p>
          <p v-if="addresses.length === 0" class="hint">
            暂无收货地址，请先在个人中心添加
          </p>
        </section>

        <section class="section-card">
          <h3>商品摘要</h3>
          <div
            v-for="item in selectedItems"
            :key="item.id"
            class="summary-item"
          >
            <span>{{ getProductName(item.productId) }} × {{ item.quantity }}</span>
            <span>{{ getItemPoints(item) }} 积分</span>
          </div>
        </section>

        <section class="section-card">
          <h3>积分合计</h3>
          <p class="total-points">应付：{{ totalPoints }} 积分</p>
          <p class="balance">账户余额：{{ userPoints }} 积分</p>
        </section>

        <section class="section-card">
          <h3>提交</h3>
          <label for="remark">备注（可选）</label>
          <textarea
            id="remark"
            v-model="remark"
            rows="2"
            placeholder="给仓管的备注"
          />
          <p v-if="submitError" class="error-message">{{ submitError }}</p>
          <button
            class="submit-btn"
            :disabled="submitting"
            @click="submitOrder"
          >
            {{ submitting ? '提交中...' : '确认下单' }}
          </button>
        </section>
      </div>
    </div>
  </Layout>
</template>

<script>
import Layout from '../components/Layout.vue'
import { addressApi, cartApi, orderApi, productApi, userApi } from '../api/exports'

export default {
  name: 'OrderCreate',
  components: { Layout },
  data() {
    return {
      loading: true,
      submitting: false,
      addresses: [],
      selectedAddressId: '',
      cartItems: [],
      products: {},
      userPoints: 0,
      remark: '',
      addressError: '',
      submitError: ''
    }
  },
  computed: {
    selectedItems() {
      return this.cartItems.filter(item => item.isSelected === 1)
    },
    totalPoints() {
      return this.selectedItems.reduce((sum, item) => sum + this.getItemPoints(item), 0)
    }
  },
  async mounted() {
    await this.loadData()
  },
  methods: {
    async loadData() {
      this.loading = true
      this.submitError = ''

      try {
        const [addressRes, cartRes, userRes] = await Promise.all([
          addressApi.getAddresses(),
          cartApi.getCart(),
          userApi.getUserInfo()
        ])

        if (addressRes.success) {
          this.addresses = addressRes.data || []
          const defaultAddr = this.addresses.find(a => a.isDefault === 1)
          if (defaultAddr) {
            this.selectedAddressId = defaultAddr.id
          }
        }

        if (cartRes.success) {
          this.cartItems = cartRes.data.cartItems || []
          for (const item of this.cartItems) {
            if (!this.products[item.productId]) {
              try {
                const productRes = await productApi.getProductById(item.productId)
                if (productRes.success) {
                  this.products[item.productId] = productRes.data
                }
              } catch (err) {
                console.error('加载商品信息失败', err)
              }
            }
          }
        }

        if (userRes.success) {
          this.userPoints = userRes.data.points || 0
        }
      } catch (err) {
        console.error('加载结算页数据失败', err)
        this.submitError = err.response?.data?.message || '加载失败，请稍后重试'
      } finally {
        this.loading = false
      }
    },
    formatAddress(addr) {
      const phone = addr.phone || ''
      const maskedPhone = phone.length >= 7
        ? phone.slice(0, 3) + '****' + phone.slice(-4)
        : phone
      const detail = [addr.province, addr.city, addr.district, addr.detailAddress]
        .filter(Boolean)
        .join(' ')
      return `${addr.receiver} · ${maskedPhone} · ${detail}`
    },
    getProductName(productId) {
      return this.products[productId]?.name || '商品已下架'
    },
    getItemPoints(item) {
      const points = this.products[item.productId]?.pointsRequired || 0
      return points * item.quantity
    },
    async submitOrder() {
      this.addressError = ''
      this.submitError = ''

      if (!this.selectedAddressId) {
        this.addressError = '请先选择收货地址'
        return
      }

      this.submitting = true
      try {
        const response = await orderApi.createOrder({
          addressId: Number(this.selectedAddressId),
          remark: this.remark || null
        })
        if (response.success) {
          const orderId = response.data?.id
          if (orderId) {
            this.$router.push(`/orders/${orderId}`)
          } else {
            this.$router.push('/orders')
          }
        } else {
          this.submitError = response.message || '下单失败'
        }
      } catch (err) {
        this.submitError = err.response?.data?.message || '下单失败，请稍后重试'
      } finally {
        this.submitting = false
      }
    }
  }
}
</script>

<style scoped>
.order-create-page {
  padding: 20px 0;
}

.order-create-page h2 {
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

.section-card {
  background: white;
  border-radius: 8px;
  padding: 24px;
  margin-bottom: 16px;
}

.section-card h3 {
  font-size: 18px;
  margin: 0 0 16px;
}

.section-card label {
  display: block;
  margin-bottom: 8px;
  color: #666;
  font-size: 14px;
}

.section-card select,
.section-card textarea {
  width: 100%;
  box-sizing: border-box;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font: inherit;
}

.summary-item {
  display: flex;
  justify-content: space-between;
  padding: 12px 0;
  border-bottom: 1px solid #f5f5f5;
}

.summary-item:last-child {
  border-bottom: none;
}

.total-points {
  font-size: 20px;
  color: #ff9800;
  font-weight: 600;
  margin-bottom: 8px;
}

.balance {
  color: #666;
}

.hint {
  margin-top: 8px;
  color: #999;
  font-size: 14px;
}

.error-message {
  color: #f44336;
  margin-top: 8px;
}

.submit-btn {
  margin-top: 16px;
  padding: 12px 48px;
  background: #667eea;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 16px;
  cursor: pointer;
}

.submit-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
</style>

<template>
  <Layout>
    <div class="checkout-page">
      <h2>确认订单</h2>

      <div v-if="loading" class="state-card">加载中...</div>

      <div v-else-if="!canCheckout" class="state-card error-state">
        没有选中的商品，请返回购物车选择。
        <p><button type="button" @click="$router.push('/cart')">去购物车</button></p>
      </div>

      <div v-else class="checkout-card">
        <section class="section">
          <h3>结算商品</h3>
          <div v-for="item in selectedItems" :key="item.id" class="line">
            <span>{{ productName(item.productId) }}</span>
            <span>{{ pointsLine(item) }}</span>
          </div>
          <p class="total">共 {{ summary.selectedCount }} 件，合计 <strong>{{ summary.totalPoints }}</strong> 积分</p>
          <p v-if="userPoints !== null" class="points-hint">当前积分：{{ userPoints }}</p>
        </section>

        <section class="section">
          <h3>收货地址（可选）</h3>
          <select v-model="addressId" class="addr-select">
            <option :value="null">不填写地址</option>
            <option v-for="a in addresses" :key="a.id" :value="a.id">
              {{ a.receiver }} {{ a.phone }} {{ a.province }}{{ a.city }}{{ a.district }}{{ a.detailAddress }}
            </option>
          </select>
        </section>

        <section class="section">
          <h3>备注</h3>
          <textarea v-model="remark" rows="3" placeholder="选填" class="remark"></textarea>
        </section>

        <p v-if="errorMessage" class="error-msg">{{ errorMessage }}</p>

        <div class="actions">
          <button type="button" class="btn-secondary" @click="$router.push('/cart')">返回购物车</button>
          <button
            type="button"
            class="btn-primary"
            :disabled="submitting || (userPoints !== null && summary.totalPoints > userPoints)"
            @click="submitOrder"
          >
            {{ submitting ? '提交中...' : '提交订单' }}
          </button>
        </div>
      </div>
    </div>
  </Layout>
</template>

<script>
import Layout from '../components/Layout.vue'
import { cartApi, orderApi, addressApi, userApi, productApi } from '../api/exports'

export default {
  name: 'Checkout',
  components: { Layout },
  data() {
    return {
      loading: true,
      cartItems: [],
      summary: { selectedCount: 0, totalPoints: 0 },
      products: {},
      addresses: [],
      addressId: null,
      remark: '',
      userPoints: null,
      errorMessage: '',
      submitting: false
    }
  },
  computed: {
    selectedItems() {
      return this.cartItems.filter((i) => this.isRowSelected(i))
    },
    canCheckout() {
      return this.selectedItems.length > 0
    }
  },
  async mounted() {
    await this.bootstrap()
  },
  methods: {
    isRowSelected(item) {
      const v = item?.isSelected
      return v === 1 || v === true || v === '1'
    },
    async bootstrap() {
      this.loading = true
      try {
        const [cartRes, userRes, addrRes] = await Promise.all([
          cartApi.getCart(),
          userApi.getUserInfo().catch(() => ({ success: false })),
          addressApi.getAddresses().catch(() => ({ success: false }))
        ])
        if (cartRes.success) {
          this.cartItems = cartRes.data.cartItems || []
          this.summary = cartRes.data.summary || {}
        }
        if (userRes.success && userRes.data) {
          this.userPoints = userRes.data.points ?? null
        }
        if (addrRes.success) {
          this.addresses = addrRes.data || []
        }
        for (const item of this.selectedItems) {
          if (!this.products[item.productId]) {
            const pr = await productApi.getProductById(item.productId).catch(() => null)
            if (pr && pr.success) this.products[item.productId] = pr.data
          }
        }
      } finally {
        this.loading = false
      }
    },
    productName(pid) {
      return this.products[pid]?.name || '商品'
    },
    pointsLine(item) {
      const p = this.products[item.productId]
      const unit = p?.pointsRequired ?? 0
      return `${unit} 积分 × ${item.quantity}`
    },
    async submitOrder() {
      this.errorMessage = ''
      if (!this.canCheckout) {
        this.errorMessage = '没有可结算的商品'
        return
      }
      if (this.userPoints !== null && this.summary.totalPoints > this.userPoints) {
        this.errorMessage = '积分不足'
        return
      }
      this.submitting = true
      try {
        const body = {}
        if (this.addressId != null) body.addressId = this.addressId
        if (this.remark) body.remark = this.remark
        const res = await orderApi.createOrder(body)
        if (res.success && res.data?.id) {
          this.$router.replace(`/orders/${res.data.id}`)
        } else {
          this.errorMessage = res.message || '下单失败'
        }
      } catch (err) {
        const msg = err.response?.data?.message || err.response?.data?.error?.message || err.message
        this.errorMessage = msg || '下单失败'
      } finally {
        this.submitting = false
      }
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
  margin-bottom: 24px;
}
.state-card {
  background: white;
  border-radius: 8px;
  padding: 32px;
  text-align: center;
}
.error-state {
  color: #c62828;
}
.checkout-card {
  background: white;
  border-radius: 8px;
  padding: 24px;
  max-width: 640px;
}
.section {
  margin-bottom: 24px;
}
.section h3 {
  font-size: 16px;
  margin-bottom: 12px;
}
.line {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;
}
.total {
  margin-top: 12px;
  font-size: 15px;
}
.points-hint {
  color: #666;
  font-size: 14px;
  margin-top: 8px;
}
.addr-select {
  width: 100%;
  padding: 8px;
  border-radius: 4px;
  border: 1px solid #ccc;
}
.remark {
  width: 100%;
  padding: 8px;
  border-radius: 4px;
  border: 1px solid #ccc;
  box-sizing: border-box;
}
.error-msg {
  color: #c62828;
  margin-bottom: 12px;
}
.actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}
.btn-primary {
  padding: 10px 24px;
  background: #1976d2;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}
.btn-primary:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
.btn-secondary {
  padding: 10px 24px;
  background: #eee;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}
</style>

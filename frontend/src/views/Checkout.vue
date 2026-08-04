<template>
  <Layout>
    <div class="checkout-page">
      <div class="checkout-top">
        <div class="checkout-title">
          <span class="title-main">确认订单</span>
        </div>
        <div class="user-points">可用积分：<strong>{{ userPoints }}</strong></div>
      </div>

      <div class="steps-bar">
        <div class="steps">
          <router-link to="/cart" class="step done step-link">① 我的购物车</router-link>
          <span class="arrow">›</span>
          <span class="step active">② 填写并核对订单信息</span>
          <span class="arrow">›</span>
          <span class="step">③ 成功提交订单</span>
        </div>
      </div>

      <div v-if="errorBanner" class="error-banner show">
        <div class="error-inner">{{ errorBanner }}</div>
      </div>

      <div v-if="loading" class="state">加载中...</div>
      <div v-else-if="emptyHint" class="state empty-state">
        <p>{{ emptyHint }}</p>
        <router-link to="/cart" class="back-btn">返回购物车</router-link>
      </div>

      <main v-else class="page">
        <div class="main-col">
          <section ref="addressSection" class="card" id="addressSection">
            <div class="card-head">
              <span class="icon">收</span>
              收货人信息
            </div>
            <div class="card-body">
              <div v-if="addresses.length === 0" class="address-empty">
                <div>暂无收货地址</div>
                <div class="hint">请先添加收货地址后再结算</div>
              </div>
              <div v-else class="address-list">
                <div
                  v-for="addr in addresses"
                  :key="addr.id"
                  class="address-item"
                  :class="{ selected: selectedAddressId === addr.id }"
                  role="button"
                  tabindex="0"
                  @click="selectAddress(addr.id)"
                  @keydown.enter="selectAddress(addr.id)"
                >
                  <input
                    type="radio"
                    name="address"
                    class="radio"
                    :checked="selectedAddressId === addr.id"
                    @click.stop="selectAddress(addr.id)"
                  />
                  <div class="address-content">
                    <div class="address-row1">
                      <span class="name">{{ addr.receiver }}</span>
                      <span class="phone">{{ addr.phone }}</span>
                      <span v-if="addr.isDefault === 1" class="tag-default">默认</span>
                    </div>
                    <div class="address-detail">{{ formatAddress(addr) }}</div>
                  </div>
                </div>
              </div>
            </div>
          </section>

          <section class="card shop-block">
            <div class="card-head">
              <span class="icon">商</span>
              商品清单
            </div>
            <div class="card-body">
              <div
                v-for="item in selectedItems"
                :key="item.id"
                class="product-row"
              >
                <div class="product-img">
                  <img
                    v-if="productImageUrl(item.productId)"
                    :src="productImageUrl(item.productId)"
                    :alt="products[item.productId]?.name"
                    class="product-img-el"
                  />
                  <span v-else>{{ productThumb(item.productId) }}</span>
                </div>
                <div class="product-info">
                  <div class="title">{{ products[item.productId]?.name || '商品' }}</div>
                  <div class="spec">福利兑换 · 积分支付</div>
                  <div v-if="stockWarn(item)" class="stock-warn show">
                    库存不足，当前仅剩 {{ products[item.productId]?.stock ?? 0 }}件
                  </div>
                </div>
                <div class="product-price">
                  <div class="points">{{ products[item.productId]?.pointsRequired || 0 }}积分</div>
                  <div class="qty">× {{ item.quantity }}</div>
                </div>
              </div>
            </div>
          </section>

          <section class="card">
            <div class="card-head">订单备注</div>
            <div class="card-body">
              <textarea
                v-model="remark"
                class="remark-input"
                maxlength="100"
                placeholder="选填：对本次兑换的特殊说明（最多 100 字）"
                rows="3"
              />
            </div>
          </section>
        </div>

        <aside class="sidebar">
          <div class="card summary-card">
            <div class="card-head">应付积分</div>
            <div class="card-body">
              <div class="summary-row">
                <span>商品件数</span>
                <span>{{ itemCount }}件</span>
              </div>
              <div class="summary-row">
                <span>商品积分</span>
                <span>{{ subtotalPoints }}积分</span>
              </div>
              <div class="summary-row">
                <span>当前可用积分</span>
                <span>{{ userPoints }}积分</span>
              </div>
              <div class="summary-row total">
                <span>合计</span>
                <span class="amount"><small>积分 </small>{{ subtotalPoints }}</span>
              </div>
              <div v-if="pointsGap > 0" class="points-gap show">
                积分不足：还需 <strong>{{ pointsGap }}</strong>积分
              </div>
              <button class="btn-submit" :disabled="submitting" @click="submitOrder">
                {{ submitting ? '提交中...' : '提交订单' }}
              </button>
            </div>
          </div>
        </aside>
      </main>

      <div v-if="showModal" class="overlay show" @click.self="showModal = false">
        <div class="modal">
          <div class="modal-icon error">×</div>
          <h3>提交失败</h3>
          <p>{{ modalMessage }}</p>
          <div class="modal-actions">
            <button class="btn-primary" @click="showModal = false">知道了</button>
          </div>
        </div>
      </div>
      <div class="mock-bar">
        <span class="mock-tag">Mock</span>
        <label for="addressMockSelect">地址场景</label>
        <select
          id="addressMockSelect"
          v-model="addressMockScenario"
          @change="onAddressScenarioChange"
        >
          <option
            v-for="opt in mockAddressScenarioOptions"
            :key="opt.value"
            :value="opt.value"
          >
            {{ opt.label }}
          </option>
        </select>
      </div>
    </div>
  </Layout>
</template>

<script>
import Layout from '../components/Layout.vue'
import {
  MOCK_CHECKOUT_ADDRESSES,
  MOCK_ADDRESS_SCENARIO_EMPTY,
  MOCK_ADDRESS_SCENARIO_NORMAL,
  MOCK_ADDRESS_SCENARIO_OPTIONS,
  getMockAddressesForScenario
} from '../mocks/checkoutAddresses'
import {
  pickDefaultAddressId,
  buildBackendAddressIdMap,
  resolveAddressId as resolveBackendAddressId,
  formatFullAddress,
  needsAddressSeed
} from '../checkout/addressMockSync'
import {
  computeItemCount,
  computeSubtotalPoints,
  computePointsGap
} from '../checkout/cartSummary'
import {
  parseProductImageUrl,
  productThumbChar,
  isStockWarn
} from '../checkout/productDisplay'
import { cartApi, addressApi, productApi, userApi, orderApi } from '../api/exports'

export default {
  name: 'Checkout',
  components: { Layout },
  data() {
    return {
      loading: true,
      emptyHint: '',
      userPoints: 0,
      addresses: [],
      selectedAddressId: null,
      selectedItems: [],
      products: {},
      remark: '',
      errorBanner: '',
      showModal: false,
      modalMessage: '',
      submitting: false,
      backendAddressIds: {},
      addressMockScenario: MOCK_ADDRESS_SCENARIO_NORMAL
    }
  },
  computed: {
    mockAddressScenarioOptions() {
      return MOCK_ADDRESS_SCENARIO_OPTIONS
    },
    itemCount() {
      return computeItemCount(this.selectedItems)
    },
    subtotalPoints() {
      return computeSubtotalPoints(this.selectedItems, this.products)
    },
    pointsGap() {
      return computePointsGap(this.subtotalPoints, this.userPoints)
    }
  },
  async mounted() {
    const q = this.$route.query.mockAddress
    if (q === MOCK_ADDRESS_SCENARIO_EMPTY) {
      this.addressMockScenario = MOCK_ADDRESS_SCENARIO_EMPTY
    }
    await this.loadData()
  },
  methods: {
    formatAddress(addr) {
      return formatFullAddress(addr)
    },
    productImageUrl(productId) {
      return parseProductImageUrl(this.products[productId])
    },
    productThumb(productId) {
      return productThumbChar(this.products[productId])
    },
    stockWarn(item) {
      return isStockWarn(this.products[item.productId], item.quantity)
    },
    selectAddress(id) {
      this.selectedAddressId = id
      this.errorBanner = ''
    },
    async loadData() {
      this.loading = true
      this.emptyHint = ''
      try {
        const [userRes, cartRes] = await Promise.all([
          userApi.getUserInfo(),
          cartApi.getCart()
        ])
        if (userRes.success) {
          this.userPoints = userRes.data.points || 0
        }
        const cartItems = cartRes.success ? (cartRes.data.cartItems || []) : []
        this.selectedItems = cartItems.filter(i => i.isSelected === 1)
        if (cartItems.length === 0) {
          this.emptyHint = '购物车为空，请先选购商品'
          return
        }
        if (this.selectedItems.length === 0) {
          this.emptyHint = '未选择结算商品，请返回购物车勾选'
          return
        }
        for (const item of this.selectedItems) {
          const pr = await productApi.getProductById(item.productId)
          if (pr.success) {
            this.products[item.productId] = pr.data
          }
        }
        this.loadMockAddresses()
        await this.ensureBackendAddressIds()
      } catch (err) {
        console.error(err)
        this.emptyHint = '加载失败，请稍后重试'
      } finally {
        this.loading = false
      }
    },
    loadMockAddresses() {
      this.addresses = getMockAddressesForScenario(this.addressMockScenario)
      this.selectedAddressId = pickDefaultAddressId(this.addresses)
    },
    async onAddressScenarioChange() {
      this.errorBanner = ''
      this.loadMockAddresses()
      if (this.addressMockScenario === MOCK_ADDRESS_SCENARIO_EMPTY) {
        this.backendAddressIds = {}
        return
      }
      await this.ensureBackendAddressIds()
    },
    async ensureBackendAddressIds() {
      if (this.addressMockScenario === MOCK_ADDRESS_SCENARIO_EMPTY) {
        this.backendAddressIds = {}
        return
      }
      this.backendAddressIds = {}
      let list = []
      try {
        const res = await addressApi.getAddresses()
        if (res.success && Array.isArray(res.data)) {
          list = res.data
        }
      } catch (err) {
        console.warn('getAddresses failed, will seed mock addresses', err)
      }
      if (needsAddressSeed(list.length, MOCK_CHECKOUT_ADDRESSES.length)) {
        while (needsAddressSeed(list.length, MOCK_CHECKOUT_ADDRESSES.length)) {
          const mock = MOCK_CHECKOUT_ADDRESSES[list.length]
          const created = await addressApi.createAddress({
            receiver: mock.receiver,
            phone: mock.phone,
            province: mock.province,
            city: mock.city,
            district: mock.district,
            detailAddress: mock.detailAddress,
            isDefault: mock.isDefault
          })
          if (created.success && created.data) {
            list.push(created.data)
          } else {
            break
          }
        }
      }
      this.backendAddressIds = buildBackendAddressIdMap(MOCK_CHECKOUT_ADDRESSES, list)
    },
    resolveAddressId() {
      return resolveBackendAddressId(this.selectedAddressId, this.backendAddressIds)
    },
    async submitOrder() {
      this.errorBanner = ''
      this.showModal = false
      if (!this.selectedAddressId) {
        this.errorBanner = '请选择收货地址后再提交订单'
        this.modalMessage = '您还未选择收货地址，请先选择或新增收货地址。'
        this.showModal = true
        this.$refs.addressSection?.scrollIntoView?.({ behavior: 'smooth', block: 'start' })
        return
      }
      this.submitting = true
      try {
        const res = await orderApi.createOrder({
          addressId: this.resolveAddressId(),
          remark: this.remark || undefined
        })
        if (res.success && res.data?.id) {
          this.$router.push('/orders/' + res.data.id)
        } else {
          const msg = res.message || '下单失败'
          this.errorBanner = msg
          this.modalMessage = msg
          this.showModal = true
        }
      } catch (err) {
        const msg = err.response?.data?.message || err.message || '下单失败'
        this.errorBanner = msg
        this.modalMessage = msg
        this.showModal = true
      } finally {
        this.submitting = false
      }
    }
  }
}
</script>

<style scoped>
.checkout-page {
  --jd-red: #e4393c;
  --jd-red-dark: #c81623;
  --jd-red-light: #fff0f0;
  --bg: #f4f4f4;
  --card: #ffffff;
  --text: #333333;
  --text-secondary: #666666;
  --text-muted: #999999;
  --border: #e5e5e5;
  --shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  --radius: 4px;

  color: var(--text);
  line-height: 1.5;
  margin: -20px;
  padding: 0 0 56px;
  background: var(--bg);
  min-height: calc(100vh - 60px);
}

.checkout-top {
  max-width: 1190px;
  margin: 0 auto;
  padding: 16px 24px 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.checkout-title .title-main {
  font-size: 22px;
  font-weight: 700;
  color: var(--jd-red);
  letter-spacing: 1px;
}

.user-points {
  font-size: 14px;
  color: var(--text-secondary);
}

.user-points strong {
  color: var(--jd-red);
  font-size: 18px;
  margin-left: 4px;
}

.steps-bar {
  max-width: 1190px;
  margin: 20px auto 0;
  padding: 0 24px;
}

.steps {
  display: flex;
  align-items: center;
  gap: 0;
  font-size: 13px;
  color: var(--text-muted);
}

.steps .step {
  display: flex;
  align-items: center;
  gap: 6px;
}

.steps .step.active {
  color: var(--jd-red);
  font-weight: 600;
}

.steps .step.done {
  color: var(--text-secondary);
}

.step-link {
  text-decoration: none;
  cursor: pointer;
  transition: color 0.2s;
}

.step-link:hover {
  color: var(--jd-red);
}

.steps .arrow {
  margin: 0 12px;
  color: #ccc;
}

.error-banner {
  display: none;
  max-width: 1190px;
  margin: 12px auto 0;
  padding: 0 24px;
}

.error-banner.show {
  display: block;
}

.error-inner {
  background: var(--jd-red-light);
  border: 1px solid #ffccc7;
  color: var(--jd-red-dark);
  padding: 12px 16px;
  border-radius: var(--radius);
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.error-inner::before {
  content: "!";
  font-size: 16px;
}

.page {
  max-width: 1190px;
  margin: 16px auto 0;
  padding: 0 24px;
  display: grid;
  grid-template-columns: 1fr 300px;
  gap: 16px;
  align-items: start;
}

.card {
  background: var(--card);
  border-radius: var(--radius);
  box-shadow: var(--shadow);
  margin-bottom: 12px;
  overflow: hidden;
}

.card-head {
  padding: 14px 20px;
  border-bottom: 1px solid var(--border);
  font-size: 15px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 8px;
}

.card-head .icon {
  width: 20px;
  height: 20px;
  background: var(--jd-red);
  color: #fff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
}

.card-body {
  padding: 16px 20px;
}

.address-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.address-item {
  border: 2px solid var(--border);
  border-radius: var(--radius);
  padding: 14px 16px;
  cursor: pointer;
  transition: border-color 0.2s, background 0.2s;
  position: relative;
}

.address-item:hover {
  border-color: #ffb4b4;
}

.address-item.selected {
  border-color: var(--jd-red);
  background: var(--jd-red-light);
}

.address-item .radio {
  position: absolute;
  left: 16px;
  top: 50%;
  transform: translateY(-50%);
  width: 16px;
  height: 16px;
  accent-color: var(--jd-red);
}

.address-content {
  margin-left: 28px;
}

.address-row1 {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 6px;
}

.address-row1 .name {
  font-weight: 600;
  font-size: 15px;
}

.address-row1 .phone {
  color: var(--text-secondary);
  font-size: 14px;
}

.tag-default {
  background: var(--jd-red);
  color: #fff;
  font-size: 11px;
  padding: 1px 6px;
  border-radius: 2px;
}

.address-detail {
  font-size: 13px;
  color: var(--text-secondary);
  line-height: 1.6;
}

.address-empty {
  text-align: center;
  padding: 32px;
  color: var(--text-muted);
}

.address-empty .hint {
  margin-top: 8px;
  font-size: 13px;
  color: var(--jd-red);
}

.product-row {
  display: grid;
  grid-template-columns: 80px 1fr auto;
  gap: 16px;
  padding: 16px 0;
  border-bottom: 1px solid #f0f0f0;
  align-items: center;
}

.product-row:last-child {
  border-bottom: none;
}

.product-img {
  width: 80px;
  height: 80px;
  background: linear-gradient(135deg, #f8f8f8, #eee);
  border-radius: var(--radius);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  color: var(--text-muted);
  overflow: hidden;
}

.product-img-el {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.product-info .title {
  font-size: 14px;
  margin-bottom: 6px;
  line-height: 1.4;
}

.product-info .spec {
  font-size: 12px;
  color: var(--text-muted);
}

.product-info .stock-warn {
  font-size: 12px;
  color: var(--jd-red);
  margin-top: 4px;
  display: none;
}

.product-info .stock-warn.show {
  display: block;
}

.product-price {
  text-align: right;
  min-width: 100px;
}

.product-price .points {
  color: var(--jd-red);
  font-size: 16px;
  font-weight: 600;
}

.product-price .qty {
  font-size: 12px;
  color: var(--text-muted);
  margin-top: 4px;
}

.remark-input {
  width: 100%;
  border: 1px solid var(--border);
  border-radius: var(--radius);
  padding: 10px 12px;
  font-size: 13px;
  resize: vertical;
  min-height: 64px;
  font-family: inherit;
  box-sizing: border-box;
}

.remark-input:focus {
  outline: none;
  border-color: var(--jd-red);
}

.sidebar {
  position: sticky;
  top: 16px;
}

.summary-card .card-body {
  padding: 20px;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  color: var(--text-secondary);
  margin-bottom: 10px;
}

.summary-row.total {
  font-size: 15px;
  color: var(--text);
  font-weight: 600;
  padding-top: 12px;
  border-top: 1px solid var(--border);
  margin-top: 4px;
}

.summary-row.total .amount {
  color: var(--jd-red);
  font-size: 24px;
}

.summary-row.total .amount small {
  font-size: 14px;
}

.points-gap {
  margin-top: 10px;
  padding: 8px 10px;
  background: #fff7e6;
  border: 1px solid #ffe58f;
  border-radius: var(--radius);
  font-size: 12px;
  color: #d48806;
  display: none;
}

.points-gap.show {
  display: block;
}

.btn-submit {
  width: 100%;
  margin-top: 16px;
  padding: 14px;
  background: var(--jd-red);
  color: #fff;
  border: none;
  border-radius: var(--radius);
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s;
}

.btn-submit:hover:not(:disabled) {
  background: var(--jd-red-dark);
}

.btn-submit:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.overlay {
  display: none;
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.45);
  z-index: 1000;
  align-items: center;
  justify-content: center;
}

.overlay.show {
  display: flex;
}

.modal {
  background: #fff;
  border-radius: 8px;
  width: 420px;
  max-width: 90vw;
  padding: 32px 28px;
  text-align: center;
  animation: pop 0.25s ease;
}

@keyframes pop {
  from {
    transform: scale(0.9);
    opacity: 0;
  }
  to {
    transform: scale(1);
    opacity: 1;
  }
}

.modal-icon {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  margin: 0 auto 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
}

.modal-icon.error {
  background: var(--jd-red-light);
  color: var(--jd-red);
}

.modal h3 {
  font-size: 18px;
  margin-bottom: 8px;
}

.modal p {
  font-size: 14px;
  color: var(--text-secondary);
  line-height: 1.6;
  white-space: pre-line;
}

.modal-actions {
  margin-top: 24px;
  display: flex;
  gap: 12px;
  justify-content: center;
}

.modal-actions button {
  padding: 10px 28px;
  border-radius: var(--radius);
  font-size: 14px;
  cursor: pointer;
  border: none;
}

.btn-primary {
  background: var(--jd-red);
  color: #fff;
}

.state {
  text-align: center;
  padding: 60px 20px;
  color: var(--text-secondary);
}

.empty-state .back-btn {
  display: inline-block;
  margin-top: 16px;
  padding: 10px 24px;
  background: var(--jd-red);
  color: #fff;
  text-decoration: none;
  border-radius: var(--radius);
  font-size: 14px;
}

.empty-state .back-btn:hover {
  background: var(--jd-red-dark);
}

.mock-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: #1f1f1f;
  color: #ccc;
  padding: 10px 24px;
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 12px;
  z-index: 900;
}

.mock-bar label {
  color: #888;
}

.mock-bar select {
  background: #333;
  color: #fff;
  border: 1px solid #555;
  padding: 4px 10px;
  border-radius: 3px;
  font-size: 12px;
}

.mock-tag {
  background: #722ed1;
  color: #fff;
  padding: 2px 8px;
  border-radius: 3px;
  font-size: 11px;
}

.checkout-page {
  padding-bottom: 56px;
}

@media (max-width: 860px) {
  .page {
    grid-template-columns: 1fr;
  }

  .sidebar {
    position: static;
  }
}
</style>


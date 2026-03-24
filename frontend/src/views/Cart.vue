<template>
  <Layout>
    <div class="cart-page">
      <header class="cart-head">
        <div class="head-icon" aria-hidden="true">
          <svg class="bag-svg" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M6 7h12l-1 11H7L6 7z" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>
            <path d="M9 7V6a3 3 0 016 0v1" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
          </svg>
        </div>
        <div class="head-text">
          <h1 class="cart-title">
            <span class="title-part title-part--dark">购物车</span>
            <span class="title-part title-part--pink"> CART</span>
          </h1>
          <p class="cart-sub">
            <span class="star">★</span> 福利藏家版 <span class="star">★</span>
          </p>
          <div class="title-bar" aria-hidden="true"></div>
        </div>
      </header>

      <div v-if="cartItems.length === 0" class="empty-cart">
        <div class="empty-icon-wrap" aria-hidden="true">
          <svg class="empty-bag" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M6 7h12l-1 11H7L6 7z" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>
            <path d="M9 7V6a3 3 0 016 0v1" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
          </svg>
        </div>
        <p class="empty-title">购物车还是空的</p>
        <p class="empty-hint">去逛逛，把心水好物装进小车吧</p>
        <router-link to="/products" class="empty-cta">去挑选</router-link>
      </div>

      <div v-else class="cart-body">
        <ul class="cart-list" role="list">
          <li v-for="item in cartItems" :key="item.id" class="cart-card">
            <div class="card-blob" aria-hidden="true"></div>

            <label class="select-wrap">
              <input
                type="checkbox"
                class="visually-hidden"
                :checked="item.isSelected === 1"
                @change="toggleSelect(item, $event)"
              />
              <span class="check-faux" aria-hidden="true">
                <svg v-if="item.isSelected === 1" class="check-mark" viewBox="0 0 12 10" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M1 5l3.5 3.5L11 1" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
              </span>
            </label>

            <div class="card-thumb">
              <img
                v-if="getProductImage(item.productId)"
                :src="getProductImage(item.productId)"
                :alt="getProductName(item.productId)"
                loading="lazy"
              />
              <div v-else class="thumb-placeholder">{{ getProductName(item.productId).slice(0, 1) }}</div>
            </div>

            <div class="card-main">
              <h2 class="card-name">{{ displayName(item.productId) }}</h2>
              <p class="card-points">
                <span class="points-num">{{ formatPoints(linePoints(item)) }}</span>
                <span class="points-unit"> 积分</span>
              </p>
            </div>

            <div class="card-qty">
              <div class="stepper" role="group" aria-label="调整数量">
                <button type="button" class="step-btn step-btn--minus" @click="updateQuantity(item.id, item.quantity - 1)">−</button>
                <span class="qty-val">{{ item.quantity }}</span>
                <button type="button" class="step-btn step-btn--plus" @click="updateQuantity(item.id, item.quantity + 1)">+</button>
              </div>
            </div>

            <button type="button" class="remove-btn" title="移除" aria-label="从购物车移除" @click="removeItem(item.id)">
              <svg class="trash-ico" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M4 7h16M10 11v6M14 11v6M6 7l1 12h10l1-12M9 7V5a1 1 0 011-1h4a1 1 0 011 1v2" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </button>
          </li>
        </ul>

        <footer class="cart-dock" aria-label="结算区域">
          <div class="dock-inner">
            <div class="dock-info">
              <p class="dock-label">结算摘要 <span class="dock-stars">★★★</span></p>
              <p class="dock-total-line">
                <span class="dock-total-num">{{ formatPoints(summary.totalPoints || 0) }}</span>
                <span class="dock-total-unit"> 积分</span>
              </p>
              <p v-if="summary.selectedCount" class="dock-meta">已选 {{ summary.selectedCount }} 件商品</p>
            </div>
            <button type="button" class="checkout-btn" @click="checkout">
              <svg class="btn-cart-ico" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M6 7h12l-1 11H7L6 7z" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>
                <path d="M9 7V6a3 3 0 016 0v1" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              </svg>
              <span class="btn-go">GO!</span>
            </button>
          </div>
        </footer>
      </div>
    </div>
  </Layout>
</template>

<script>
import Layout from '../components/Layout.vue'
import { cartApi, productApi } from '../api/exports'

export default {
  name: 'Cart',
  components: { Layout },
  data() {
    return {
      cartItems: [],
      products: {},
      summary: {
        selectedCount: 0,
        totalPoints: 0
      }
    }
  },
  async mounted() {
    await this.loadCart()
  },
  methods: {
    async loadCart() {
      try {
        const response = await cartApi.getCart()
        if (response.success) {
          this.cartItems = response.data.cartItems || []
          this.summary = response.data.summary || {}

          for (const item of this.cartItems) {
            if (!this.products[item.productId]) {
              try {
                const productResponse = await productApi.getProductById(item.productId)
                if (productResponse.success) {
                  this.products[item.productId] = productResponse.data
                }
              } catch (err) {
                console.error('加载商品信息失败', err)
              }
            }
          }
        }
      } catch (err) {
        console.error('加载购物车失败', err)
      }
    },
    formatPoints(n) {
      const num = Number(n) || 0
      return num.toLocaleString('zh-CN')
    },
    displayName(productId) {
      return (this.getProductName(productId) || '').toUpperCase()
    },
    linePoints(item) {
      return this.getProductPoints(item.productId) * item.quantity
    },
    getProductName(productId) {
      return this.products[productId]?.name || '商品已下架'
    },
    getProductPoints(productId) {
      return this.products[productId]?.pointsRequired || 0
    },
    getProductImage(productId) {
      const raw = this.products[productId]?.images
      if (!raw || typeof raw !== 'string') return null
      try {
        const arr = JSON.parse(raw)
        if (Array.isArray(arr) && arr.length && typeof arr[0] === 'string') {
          return arr[0].trim() || null
        }
      } catch {
        if (raw.startsWith('http') || raw.startsWith('/')) return raw
      }
      return null
    },
    async toggleSelect(item, event) {
      const checked = event.target.checked
      const next = checked ? 1 : 0
      try {
        const response = await cartApi.updateSelection(item.id, next)
        if (response.success) {
          await this.loadCart()
        } else {
          event.target.checked = !checked
        }
      } catch (err) {
        event.target.checked = !checked
        console.error('更新选中状态失败', err)
        alert(err.response?.data?.message || '更新选中状态失败')
      }
    },
    async updateQuantity(id, quantity) {
      if (quantity < 1) {
        await this.removeItem(id)
        return
      }

      try {
        const response = await cartApi.updateQuantity(id, quantity)
        if (response.success) {
          await this.loadCart()
        }
      } catch (err) {
        alert(err.response?.data?.message || '更新失败')
      }
    },
    async removeItem(id) {
      try {
        const response = await cartApi.removeFromCart(id)
        if (response.success) {
          await this.loadCart()
        }
      } catch (err) {
        alert(err.response?.data?.message || '删除失败')
      }
    },
    checkout() {
      if (!this.summary.selectedCount || !this.summary.totalPoints) {
        alert('请先勾选要结算的商品')
        return
      }
      this.$router.push('/orders/create')
    }
  }
}
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@600;700;800&display=swap');

.cart-page {
  --pm-yellow: #ffd200;
  --pm-pink: #ff4da6;
  --pm-ink: #1a1a1b;
  --pm-cream: #faf8f3;
  --pm-grey: #f0f0f0;
  --pm-grey-mid: #b8b8b8;
  --pm-white: #ffffff;
  --pm-radius-card: 28px;
  --pm-radius-pill: 999px;

  min-height: 60vh;
  padding: 0 0 140px;
  font-family: 'Plus Jakarta Sans', system-ui, -apple-system, sans-serif;
  color: var(--pm-ink);
  background: var(--pm-cream);
}

/* ----- Header（对齐原型：左图标 + 标题 + 黄副标题 + 粉条） ----- */
.cart-head {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  padding: 20px 20px 8px;
  max-width: 720px;
  margin: 0 auto;
}

.head-icon {
  flex-shrink: 0;
  width: 52px;
  height: 52px;
  border-radius: 16px;
  background: var(--pm-yellow);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 0 rgba(26, 26, 27, 0.12);
}

.bag-svg {
  width: 28px;
  height: 28px;
  color: var(--pm-white);
}

.head-text {
  flex: 1;
  min-width: 0;
}

.cart-title {
  margin: 0;
  font-size: clamp(1.35rem, 4.2vw, 1.75rem);
  font-weight: 800;
  letter-spacing: 0.04em;
  line-height: 1.15;
}

.title-part--dark {
  color: var(--pm-ink);
}

.title-part--pink {
  color: var(--pm-pink);
}

.cart-sub {
  margin: 8px 0 0;
  font-size: 0.72rem;
  font-weight: 800;
  letter-spacing: 0.2em;
  color: var(--pm-yellow);
  text-transform: uppercase;
}

.cart-sub .star {
  color: var(--pm-yellow);
  letter-spacing: 0;
}

.title-bar {
  height: 6px;
  width: 100%;
  max-width: 280px;
  margin-top: 14px;
  border-radius: 4px;
  background: var(--pm-pink);
}

/* ----- Empty ----- */
.empty-cart {
  text-align: center;
  padding: 40px 24px 48px;
  max-width: 400px;
  margin: 24px auto 0;
  background: var(--pm-white);
  border-radius: var(--pm-radius-card);
  box-shadow: 0 16px 40px rgba(26, 26, 27, 0.06);
}

.empty-icon-wrap {
  width: 64px;
  height: 64px;
  margin: 0 auto 16px;
  border-radius: 18px;
  background: var(--pm-yellow);
  display: flex;
  align-items: center;
  justify-content: center;
}

.empty-bag {
 width: 36px;
 height: 36px;
 color: var(--pm-white);
}

.empty-title {
  font-size: 1.1rem;
  font-weight: 800;
  margin: 0 0 8px;
  color: var(--pm-ink);
}

.empty-hint {
  margin: 0 0 22px;
  font-size: 0.9rem;
  color: var(--pm-grey-mid);
  font-weight: 600;
}

.empty-cta {
  display: inline-block;
  padding: 14px 32px;
  background: var(--pm-yellow);
  color: var(--pm-ink);
  font-weight: 800;
  letter-spacing: 0.06em;
  text-decoration: none;
  border-radius: var(--pm-radius-pill);
  box-shadow: 0 4px 0 rgba(26, 26, 27, 0.18);
  transition: transform 0.12s ease, box-shadow 0.12s ease;
}

.empty-cta:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 0 rgba(26, 26, 27, 0.15);
}

.empty-cta:active {
  transform: translateY(2px);
  box-shadow: 0 2px 0 rgba(26, 26, 27, 0.2);
}

/* ----- List ----- */
.cart-list {
  list-style: none;
  margin: 16px 0 0;
  padding: 0 16px 24px;
  display: flex;
  flex-direction: column;
  gap: 18px;
  max-width: 720px;
  margin-left: auto;
  margin-right: auto;
}

.cart-card {
  position: relative;
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 14px 16px;
  padding: 18px 16px 18px 14px;
  background: var(--pm-white);
  border-radius: var(--pm-radius-card);
  box-shadow: 0 10px 32px rgba(26, 26, 27, 0.07);
  overflow: hidden;
}

.card-blob {
  position: absolute;
  top: -28px;
  right: -32px;
  width: 120px;
  height: 120px;
  border-radius: 50%;
  background: rgba(255, 77, 166, 0.12);
  pointer-events: none;
}

.select-wrap {
  position: relative;
  cursor: pointer;
  z-index: 1;
}

.visually-hidden {
  position: absolute;
  width: 1px;
  height: 1px;
  padding: 0;
  margin: -1px;
  overflow: hidden;
  clip: rect(0, 0, 0, 0);
  white-space: nowrap;
  border: 0;
}

.check-faux {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  border: 3px solid var(--pm-grey-mid);
  background: var(--pm-white);
  transition: background 0.15s ease, border-color 0.15s ease, transform 0.1s ease;
}

.check-mark {
  width: 14px;
  height: 11px;
  color: var(--pm-white);
}

.select-wrap input:checked + .check-faux {
  background: var(--pm-pink);
  border-color: var(--pm-pink);
}

.select-wrap input:focus-visible + .check-faux {
  outline: 3px solid var(--pm-yellow);
  outline-offset: 2px;
}

.select-wrap:active .check-faux {
  transform: scale(0.92);
}

.card-thumb {
  position: relative;
  z-index: 1;
  width: 76px;
  height: 76px;
  border-radius: 20px;
  overflow: hidden;
  flex-shrink: 0;
  background: var(--pm-grey);
}

.card-thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.thumb-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 800;
  font-size: 1.5rem;
  color: var(--pm-grey-mid);
}

.card-main {
  flex: 1 1 140px;
  min-width: 0;
  z-index: 1;
}

.card-name {
  margin: 0 0 6px;
  font-size: 0.82rem;
  font-weight: 800;
  line-height: 1.35;
  letter-spacing: 0.03em;
  color: var(--pm-ink);
  word-break: break-word;
}

.card-points {
  margin: 0;
  font-weight: 800;
  font-size: 0.95rem;
  line-height: 1.2;
}

.points-num {
  color: var(--pm-pink);
}

.points-unit {
  color: var(--pm-pink);
  font-size: 0.88em;
}

.card-qty {
  z-index: 1;
}

.stepper {
  display: inline-flex;
  align-items: center;
  background: var(--pm-grey);
  border-radius: var(--pm-radius-pill);
  padding: 4px 6px;
}

.step-btn {
  width: 36px;
  height: 32px;
  border: none;
  background: transparent;
  font-size: 1.2rem;
  font-weight: 800;
  line-height: 1;
  cursor: pointer;
  color: var(--pm-grey-mid);
  border-radius: var(--pm-radius-pill);
  transition: color 0.12s ease, background 0.12s ease;
}

.step-btn--minus {
  color: var(--pm-grey-mid);
}

.step-btn--plus {
  color: var(--pm-pink);
}

.step-btn:hover {
  background: rgba(255, 255, 255, 0.7);
}

.qty-val {
  min-width: 28px;
  text-align: center;
  font-weight: 800;
  font-size: 0.95rem;
  color: var(--pm-ink);
}

.remove-btn {
  z-index: 1;
  margin-left: auto;
  width: 44px;
  height: 44px;
  border: none;
  border-radius: 50%;
  background: var(--pm-grey);
  color: var(--pm-grey-mid);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.12s ease, color 0.12s ease, transform 0.1s ease;
}

.remove-btn:hover {
  background: #e8e8e8;
  color: var(--pm-ink);
}

.remove-btn:active {
  transform: scale(0.95);
}

.trash-ico {
  width: 22px;
  height: 22px;
}

@media (min-width: 600px) {
  .cart-card {
    flex-wrap: nowrap;
    padding-right: 20px;
  }

  .remove-btn {
    margin-left: 0;
  }
}

@media (max-width: 599px) {
  .cart-card {
    display: grid;
    grid-template-columns: auto 76px 1fr;
    grid-template-rows: auto auto auto;
    align-items: center;
  }

  .select-wrap {
    grid-column: 1;
    grid-row: 1;
  }

  .card-thumb {
    grid-column: 2;
    grid-row: 1;
  }

  .remove-btn {
    grid-column: 3;
    grid-row: 1;
    justify-self: end;
  }

  .card-main {
    grid-column: 1 / -1;
    grid-row: 2;
  }

  .card-qty {
    grid-column: 1 / -1;
    grid-row: 3;
    margin-top: 4px;
  }
}

/* ----- Dock（黄框 + 黑字总额 + 粉积分单位 + 黄 GO!） ----- */
.cart-dock {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 40;
  padding: 12px 14px calc(14px + env(safe-area-inset-bottom, 0));
  pointer-events: none;
  background: linear-gradient(180deg, transparent, var(--pm-cream) 30%);
}

.dock-inner {
  pointer-events: auto;
  max-width: 720px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 16px 18px;
  background: var(--pm-white);
  border-radius: 24px;
  border: 4px solid var(--pm-yellow);
  box-shadow: 0 12px 36px rgba(26, 26, 27, 0.1);
}

.dock-info {
  flex: 1;
  min-width: 0;
}

.dock-label {
  margin: 0 0 4px;
  font-size: 0.65rem;
  font-weight: 800;
  letter-spacing: 0.14em;
  color: var(--pm-grey-mid);
  text-transform: uppercase;
}

.dock-stars {
  color: var(--pm-yellow);
  letter-spacing: 0.1em;
}

.dock-total-line {
  margin: 0;
  line-height: 1.05;
}

.dock-total-num {
  font-size: clamp(1.65rem, 5vw, 2.1rem);
  font-weight: 800;
  color: var(--pm-ink);
}

.dock-total-unit {
  font-size: clamp(0.95rem, 3vw, 1.1rem);
  font-weight: 800;
  color: var(--pm-pink);
}

.dock-meta {
  margin: 6px 0 0;
  font-size: 0.78rem;
  font-weight: 600;
  color: var(--pm-grey-mid);
}

.checkout-btn {
  flex-shrink: 0;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 16px 26px;
  border: none;
  border-radius: var(--pm-radius-pill);
  background: var(--pm-yellow);
  color: var(--pm-white);
  cursor: pointer;
  box-shadow: 0 4px 0 rgba(26, 26, 27, 0.15);
  transition: transform 0.12s ease, box-shadow 0.12s ease;
}

.checkout-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 0 rgba(26, 26, 27, 0.12);
}

.checkout-btn:active {
  transform: translateY(2px);
  box-shadow: 0 2px 0 rgba(26, 26, 27, 0.2);
}

.btn-cart-ico {
  width: 22px;
  height: 22px;
}

.btn-go {
  font-size: 1.15rem;
  font-weight: 800;
  letter-spacing: 0.06em;
}
</style>

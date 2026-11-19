<template>
  <Layout>
    <div class="cart-page">
      <h2>购物车</h2>

      <div v-if="noticeMessage" class="notice-banner">{{ noticeMessage }}</div>
      
      <div v-if="cartItems.length === 0" class="empty-cart">
        购物车是空的
      </div>
      
      <div v-else>
        <div class="cart-items">
          <div v-for="item in cartItems" :key="item.id" class="cart-item">
            <input 
              type="checkbox" 
              :checked="item.isSelected === 1"
              @change="toggleSelect(item)"
            />
            <div class="item-info">
              <h4>{{ getProductName(item.productId) }}</h4>
              <p>{{ getProductPoints(item.productId) }} 积分 × {{ item.quantity }}</p>
            </div>
            <div class="item-actions">
              <button @click="updateQuantity(item.id, item.quantity - 1)">-</button>
              <span>{{ item.quantity }}</span>
              <button @click="updateQuantity(item.id, item.quantity + 1)">+</button>
              <button @click="removeItem(item.id)" class="remove-btn">删除</button>
            </div>
          </div>
        </div>
        
        <div class="cart-summary">
          <div class="summary-info">
            <p>已选 {{ summary.selectedCount }} 件商品</p>
            <p class="total-points">总计：{{ summary.totalPoints }} 积分</p>
          </div>
          <button class="checkout-btn" @click="checkout">结算</button>
        </div>
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
      noticeMessage: '',
      summary: {
        selectedCount: 0,
        totalPoints: 0
      }
    }
  },
  async mounted() {
    if (this.$route?.query?.notice === 'empty') {
      this.noticeMessage = '请先选择要兑换的商品'
    }
    await this.loadCart()
  },
  methods: {
    async loadCart() {
      try {
        const response = await cartApi.getCart()
        if (response.success) {
          this.cartItems = response.data.cartItems || []
          this.summary = response.data.summary || {}
          
          // 加载商品信息
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
    getProductName(productId) {
      return this.products[productId]?.name || '商品已下架'
    },
    getProductPoints(productId) {
      return this.products[productId]?.pointsRequired || 0
    },
    async toggleSelect(item) {
      const nextSelected = item.isSelected === 1 ? 0 : 1
      try {
        const response = await cartApi.selectItem(item.id, nextSelected)
        if (response.success) {
          await this.loadCart()
        }
      } catch (err) {
        this.noticeMessage = err.response?.data?.message || '勾选失败，请稍后重试'
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
        this.noticeMessage = err.response?.data?.message || '更新失败'
      }
    },
    async removeItem(id) {
      try {
        const response = await cartApi.removeFromCart(id)
        if (response.success) {
          await this.loadCart()
        }
      } catch (err) {
        this.noticeMessage = err.response?.data?.message || '删除失败'
      }
    },
    checkout() {
      if (!this.summary.selectedCount) {
        this.noticeMessage = '请先选择要兑换的商品'
        return
      }
      this.$router.push('/orders/create')
    }
  }
}
</script>

<style scoped>
.cart-page {
  padding: 20px 0;
}

.cart-page h2 {
  font-size: 24px;
  margin-bottom: 24px;
}

.notice-banner {
  background: #fff7e6;
  border: 1px solid #ffd591;
  color: #ad6800;
  border-radius: 6px;
  padding: 10px 14px;
  margin-bottom: 16px;
}

.empty-cart {
  text-align: center;
  padding: 60px 20px;
  color: #999;
  font-size: 16px;
}

.cart-items {
  background: white;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
}

.cart-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px 0;
  border-bottom: 1px solid #eee;
}

.cart-item:last-child {
  border-bottom: none;
}

.item-info {
  flex: 1;
}

.item-info h4 {
  font-size: 16px;
  margin-bottom: 8px;
}

.item-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.item-actions button {
  padding: 6px 12px;
  background: #f5f5f5;
  border: 1px solid #ddd;
  border-radius: 4px;
}

.remove-btn {
  background: #ffebee !important;
  color: #f44336;
}

.cart-summary {
  background: white;
  border-radius: 8px;
  padding: 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.summary-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.total-points {
  font-size: 20px;
  color: #ff9800;
  font-weight: 600;
}

.checkout-btn {
  padding: 12px 48px;
  background: #667eea;
  color: white;
  border-radius: 6px;
  font-size: 16px;
}
</style>

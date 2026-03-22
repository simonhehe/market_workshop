<template>
  <Layout>
    <div class="product-detail">
      <div v-if="loading" class="state-card">
        商品加载中...
      </div>

      <div v-else-if="errorMessage" class="state-card error-state">
        {{ errorMessage }}
      </div>

      <div v-else-if="product" class="detail-content">
        <div class="product-image-section">
          <div class="main-image">
            <div class="placeholder-image">暂无图片</div>
          </div>
        </div>
        
        <div class="product-info-section">
          <h1>{{ product.name }}</h1>
          <p class="description">{{ product.description }}</p>
          
          <div class="price-section">
            <span class="points">{{ product.pointsRequired }} 积分</span>
            <span class="stock">库存：{{ product.stock }}</span>
          </div>
          
          <div class="actions">
            <div class="quantity-selector">
              <button @click="decreaseQuantity">-</button>
              <input v-model.number="quantity" type="number" min="1" :max="product.stock" />
              <button @click="increaseQuantity">+</button>
            </div>
            
            <button class="add-to-cart-btn" @click="addToCart" :disabled="product.stock === 0">
              {{ product.stock === 0 ? '库存不足' : '加入购物车' }}
            </button>
            
            <button class="favorite-btn" @click="toggleFavorite">
              {{ isFavorite ? '已收藏' : '收藏' }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </Layout>
</template>

<script>
import Layout from '../components/Layout.vue'
import { productApi, cartApi, favoriteApi } from '../api/exports'

export default {
  name: 'ProductDetail',
  components: { Layout },
  data() {
    return {
      product: null,
      quantity: 1,
      isFavorite: false,
      loading: true,
      errorMessage: ''
    }
  },
  async mounted() {
    await this.loadProduct()
    if (this.product) {
      await this.checkFavorite()
    }
  },
  methods: {
    async loadProduct() {
      this.loading = true
      this.errorMessage = ''

      try {
        const id = this.$route.params.id
        const response = await productApi.getProductById(id)
        if (response.success) {
          this.product = response.data
        } else {
          this.errorMessage = '商品不存在或暂时无法访问'
        }
      } catch (err) {
        console.error('加载商品失败', err)
        this.errorMessage = err.response?.data?.message || '商品加载失败，请稍后重试'
      } finally {
        this.loading = false
      }
    },
    async checkFavorite() {
      try {
        const id = this.$route.params.id
        const response = await favoriteApi.checkFavorite(id)
        if (response.success) {
          this.isFavorite = response.data.isFavorite
        }
      } catch (err) {
        // 忽略错误
      }
    },
    decreaseQuantity() {
      if (this.quantity > 1) this.quantity--
    },
    increaseQuantity() {
      if (this.quantity < this.product.stock) this.quantity++
    },
    async addToCart() {
      try {
        const response = await cartApi.addToCart(this.product.id, this.quantity)
        if (response.success) {
          alert('已添加到购物车')
        }
      } catch (err) {
        alert(err.response?.data?.message || '添加失败')
      }
    },
    async toggleFavorite() {
      try {
        if (this.isFavorite) {
          // 需要先获取收藏ID，这里简化处理
          alert('取消收藏功能需要收藏ID，请从收藏列表操作')
        } else {
          const response = await favoriteApi.addFavorite(this.product.id)
          if (response.success) {
            this.isFavorite = true
            alert('已添加到收藏')
          }
        }
      } catch (err) {
        alert(err.response?.data?.message || '操作失败')
      }
    }
  }
}
</script>

<style scoped>
.product-detail {
  padding: 20px 0;
}

.state-card {
  background: white;
  border-radius: 12px;
  padding: 48px 24px;
  text-align: center;
  color: #666;
  font-size: 16px;
}

.error-state {
  color: #f44336;
}

.detail-content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 40px;
  background: white;
  padding: 40px;
  border-radius: 12px;
}

.product-image-section {
  width: 100%;
}

.main-image {
  width: 100%;
  height: 400px;
  background: #f5f5f5;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.placeholder-image {
  color: #999;
  font-size: 16px;
}

.product-info-section h1 {
  font-size: 28px;
  margin-bottom: 16px;
  color: #333;
}

.description {
  font-size: 16px;
  color: #666;
  margin-bottom: 24px;
  line-height: 1.6;
}

.price-section {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 32px;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
}

.price-section .points {
  font-size: 32px;
  color: #ff9800;
  font-weight: 600;
}

.stock {
  font-size: 16px;
  color: #666;
}

.actions {
  display: flex;
  gap: 16px;
  align-items: center;
}

.quantity-selector {
  display: flex;
  align-items: center;
  gap: 8px;
}

.quantity-selector button {
  width: 36px;
  height: 36px;
  background: #f5f5f5;
  border: 1px solid #ddd;
  border-radius: 4px;
}

.quantity-selector input {
  width: 60px;
  text-align: center;
  border: 1px solid #ddd;
}

.add-to-cart-btn,
.favorite-btn {
  flex: 1;
  padding: 12px 24px;
  background: #667eea;
  color: white;
  border-radius: 6px;
  font-size: 16px;
}

.favorite-btn {
  background: #ff9800;
}
</style>

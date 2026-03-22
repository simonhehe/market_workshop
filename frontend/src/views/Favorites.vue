<template>
  <Layout>
    <div class="favorites-page">
      <h2>我的收藏</h2>
      <div v-if="favorites.length === 0" class="empty">暂无收藏</div>
      <div v-else class="favorites-grid">
        <div v-for="fav in favorites" :key="fav.id" class="favorite-item">
          <div class="product-image">
            <div class="placeholder-image">暂无图片</div>
          </div>
          <div class="product-info">
            <h4>{{ getProductName(fav.productId) }}</h4>
            <p>{{ getProductPoints(fav.productId) }} 积分</p>
          </div>
          <button @click="removeFavorite(fav.id)" class="remove-btn">取消收藏</button>
        </div>
      </div>
    </div>
  </Layout>
</template>

<script>
import Layout from '../components/Layout.vue'
import { favoriteApi, productApi } from '../api/exports'

export default {
  name: 'Favorites',
  components: { Layout },
  data() {
    return {
      favorites: [],
      products: {}
    }
  },
  async mounted() {
    await this.loadFavorites()
  },
  methods: {
    async loadFavorites() {
      try {
        const response = await favoriteApi.getFavorites()
        if (response.success) {
          this.favorites = response.data
          
          // 加载商品信息
          for (const fav of this.favorites) {
            if (!this.products[fav.productId]) {
              try {
                const productResponse = await productApi.getProductById(fav.productId)
                if (productResponse.success) {
                  this.products[fav.productId] = productResponse.data
                }
              } catch (err) {
                console.error('加载商品信息失败', err)
              }
            }
          }
        }
      } catch (err) {
        console.error('加载收藏失败', err)
      }
    },
    getProductName(productId) {
      return this.products[productId]?.name || '商品已下架'
    },
    getProductPoints(productId) {
      return this.products[productId]?.pointsRequired || 0
    },
    async removeFavorite(id) {
      try {
        const response = await favoriteApi.removeFavorite(id)
        if (response.success) {
          await this.loadFavorites()
        }
      } catch (err) {
        alert(err.response?.data?.message || '取消收藏失败')
      }
    }
  }
}
</script>

<style scoped>
.favorites-page {
  padding: 20px 0;
}

.favorites-page h2 {
  font-size: 24px;
  margin-bottom: 24px;
}

.empty {
  text-align: center;
  padding: 60px 20px;
  color: #999;
  font-size: 16px;
}

.favorites-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 20px;
}

.favorite-item {
  background: white;
  border-radius: 8px;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.product-image {
  width: 100%;
  height: 150px;
  background: #f5f5f5;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.placeholder-image {
  color: #999;
  font-size: 14px;
}

.product-info h4 {
  font-size: 16px;
  margin-bottom: 8px;
}

.remove-btn {
  padding: 8px 16px;
  background: #ffebee;
  color: #f44336;
  border-radius: 4px;
}
</style>

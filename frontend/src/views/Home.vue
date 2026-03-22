<template>
  <Layout>
    <div class="home">
      <div class="banner">
        <h2>欢迎来到企业内部福利商城</h2>
        <p>使用积分兑换心仪商品</p>
      </div>
      
      <div class="categories">
        <h3>商品分类</h3>
        <div class="category-list">
          <div 
            v-for="category in categories" 
            :key="category.id"
            class="category-item"
            @click="goToProducts(category.id)"
          >
            {{ category.name }}
          </div>
        </div>
      </div>
      
      <div class="products-section">
        <h3>热门商品</h3>
        <div class="product-grid">
          <div 
            v-for="product in products" 
            :key="product.id"
            class="product-card"
            @click="goToProductDetail(product.id)"
          >
            <div class="product-image">
              <div class="placeholder-image">暂无图片</div>
            </div>
            <div class="product-info">
              <h4>{{ product.name }}</h4>
              <p class="product-desc">{{ product.description }}</p>
              <div class="product-footer">
                <span class="points">{{ product.pointsRequired }} 积分</span>
                <span class="stock">库存：{{ product.stock }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </Layout>
</template>

<script>
import Layout from '../components/Layout.vue'
import { categoryApi, productApi } from '../api/exports'

export default {
  name: 'Home',
  components: {
    Layout
  },
  data() {
    return {
      categories: [],
      products: []
    }
  },
  async mounted() {
    console.log('Home component mounted, loading data...')
    await this.loadCategories()
    await this.loadProducts()
    console.log('Home data loaded:', { categories: this.categories.length, products: this.products.length })
  },
  methods: {
    async loadCategories() {
      try {
        console.log('Loading categories...')
        const response = await categoryApi.getCategories()
        console.log('Categories response:', response)
        if (response && response.success) {
          this.categories = response.data ? response.data.slice(0, 5) : []
          console.log('Categories loaded:', this.categories.length)
        } else {
          console.warn('Categories response not successful:', response)
        }
      } catch (err) {
        console.error('加载分类失败', err)
        console.error('Error details:', err.response || err.message)
      }
    },
    async loadProducts() {
      try {
        console.log('Loading products...')
        const response = await productApi.getProducts()
        console.log('Products response:', response)
        if (response && response.success) {
          this.products = response.data ? response.data.slice(0, 8) : []
          console.log('Products loaded:', this.products.length)
        } else {
          console.warn('Products response not successful:', response)
        }
      } catch (err) {
        console.error('加载商品失败', err)
        console.error('Error details:', err.response || err.message)
      }
    },
    goToProducts(categoryId) {
      this.$router.push({ path: '/products', query: { categoryId } })
    },
    goToProductDetail(id) {
      this.$router.push(`/products/${id}`)
    }
  }
}
</script>

<style scoped>
.home {
  padding: 20px 0;
}

.banner {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 60px 40px;
  border-radius: 12px;
  text-align: center;
  margin-bottom: 40px;
}

.banner h2 {
  font-size: 32px;
  margin-bottom: 10px;
}

.banner p {
  font-size: 18px;
  opacity: 0.9;
}

.categories {
  margin-bottom: 40px;
}

.categories h3 {
  font-size: 20px;
  margin-bottom: 20px;
  color: #333;
}

.category-list {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}

.category-item {
  padding: 12px 24px;
  background: white;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.category-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0,0,0,0.15);
  color: #667eea;
}

.products-section h3 {
  font-size: 20px;
  margin-bottom: 20px;
  color: #333;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 20px;
}

.product-card {
  background: white;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.product-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 16px rgba(0,0,0,0.15);
}

.product-image {
  width: 100%;
  height: 200px;
  background: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
}

.placeholder-image {
  color: #999;
  font-size: 14px;
}

.product-info {
  padding: 16px;
}

.product-info h4 {
  font-size: 16px;
  margin-bottom: 8px;
  color: #333;
}

.product-desc {
  font-size: 13px;
  color: #666;
  margin-bottom: 12px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.points {
  color: #ff9800;
  font-weight: 600;
  font-size: 16px;
}

.stock {
  font-size: 12px;
  color: #999;
}
</style>

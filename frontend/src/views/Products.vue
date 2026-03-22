<template>
  <Layout>
    <div class="products-page">
      <div class="filters">
        <select v-model="selectedCategory" @change="loadProducts">
          <option value="">全部分类</option>
          <option v-for="cat in categories" :key="cat.id" :value="cat.id">
            {{ cat.name }}
          </option>
        </select>
        <input 
          v-model="keyword" 
          type="text" 
          placeholder="搜索商品..."
          @input="loadProducts"
        />
        <select v-model="sortBy" @change="loadProducts">
          <option value="">默认排序</option>
          <option value="points">按积分排序</option>
          <option value="sales">按销量排序</option>
          <option value="time">按时间排序</option>
        </select>
      </div>
      
      <div class="product-grid">
        <div 
          v-for="product in products" 
          :key="product.id"
          class="product-card"
          @click="goToDetail(product.id)"
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
      
      <div v-if="products.length === 0" class="empty">
        暂无商品
      </div>
    </div>
  </Layout>
</template>

<script>
import Layout from '../components/Layout.vue'
import { productApi, categoryApi } from '../api/exports'

export default {
  name: 'Products',
  components: { Layout },
  data() {
    return {
      products: [],
      categories: [],
      selectedCategory: '',
      keyword: '',
      sortBy: ''
    }
  },
  async mounted() {
    console.log('Products component mounted')
    await this.loadCategories()
    await this.loadProducts()
    
    // 从路由参数获取分类ID
    if (this.$route.query.categoryId) {
      this.selectedCategory = parseInt(this.$route.query.categoryId)
      await this.loadProducts()
    }
    
    console.log('Products mounted complete:', { 
      products: this.products.length, 
      categories: this.categories.length 
    })
  },
  methods: {
    async loadCategories() {
      try {
        console.log('Loading categories...')
        const response = await categoryApi.getCategories()
        console.log('Categories response:', response)
        if (response && response.success) {
          this.categories = response.data || []
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
        const params = {}
        if (this.selectedCategory) params.categoryId = this.selectedCategory
        if (this.keyword) params.keyword = this.keyword
        if (this.sortBy) params.sortBy = this.sortBy
        
        console.log('Request params:', params)
        const response = await productApi.getProducts(params)
        console.log('Products response:', response)
        if (response && response.success) {
          this.products = response.data || []
          console.log('Products loaded:', this.products.length)
        } else {
          console.warn('Products response not successful:', response)
        }
      } catch (err) {
        console.error('加载商品失败', err)
        console.error('Error details:', err.response || err.message)
      }
    },
    goToDetail(id) {
      this.$router.push(`/products/${id}`)
    }
  }
}
</script>

<style scoped>
.products-page {
  padding: 20px 0;
}

.filters {
  display: flex;
  gap: 16px;
  margin-bottom: 24px;
  background: white;
  padding: 16px;
  border-radius: 8px;
}

.filters select,
.filters input {
  flex: 1;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
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

.empty {
  text-align: center;
  padding: 60px 20px;
  color: #999;
  font-size: 16px;
}
</style>

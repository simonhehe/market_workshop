<template>
  <Layout>
    <div class="admin-products">
      <h2>商品管理</h2>
      <button @click="showAddForm = true" class="add-btn">添加商品</button>
      <div class="products-table">
        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>名称</th>
              <th>积分</th>
              <th>库存</th>
              <th>状态</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="product in products" :key="product.id">
              <td>{{ product.id }}</td>
              <td>{{ product.name }}</td>
              <td>{{ product.pointsRequired }}</td>
              <td>{{ product.stock }}</td>
              <td>{{ product.status }}</td>
              <td>
                <button @click="editProduct(product)">编辑</button>
                <button @click="deleteProduct(product.id)" class="delete-btn">删除</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </Layout>
</template>

<script>
import Layout from '../../components/Layout.vue'
import api from '../../api/index'

export default {
  name: 'AdminProducts',
  components: { Layout },
  data() {
    return {
      products: [],
      showAddForm: false
    }
  },
  async mounted() {
    await this.loadProducts()
  },
  methods: {
    async loadProducts() {
      try {
        const response = await api.get('/admin/products')
        if (response.success) {
          this.products = response.data
        }
      } catch (err) {
        console.error('加载商品失败', err)
      }
    },
    editProduct(product) {
      // 简化处理
      alert('编辑功能待实现')
    },
    async deleteProduct(id) {
      if (!confirm('确定要删除这个商品吗？')) return
      
      try {
        const response = await api.delete(`/admin/products/${id}`)
        if (response.success) {
          await this.loadProducts()
        }
      } catch (err) {
        alert(err.response?.data?.message || '删除失败')
      }
    }
  }
}
</script>

<style scoped>
.admin-products {
  padding: 20px 0;
}

.add-btn {
  margin-bottom: 20px;
  padding: 10px 20px;
  background: #667eea;
  color: white;
  border-radius: 4px;
}

.products-table {
  background: white;
  border-radius: 8px;
  overflow: hidden;
}

table {
  width: 100%;
  border-collapse: collapse;
}

th, td {
  padding: 12px;
  text-align: left;
  border-bottom: 1px solid #eee;
}

th {
  background: #f8f9fa;
  font-weight: 600;
}

.delete-btn {
  background: #f44336;
  color: white;
  margin-left: 8px;
}
</style>

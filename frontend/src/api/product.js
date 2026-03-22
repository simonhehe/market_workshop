import api from './index'

export const productApi = {
  getProducts(params) {
    return api.get('/products', { params })
  },
  getProductById(id) {
    return api.get(`/products/${id}`)
  }
}

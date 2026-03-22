import api from './index'

export const cartApi = {
  getCart() {
    return api.get('/cart')
  },
  addToCart(productId, quantity = 1) {
    return api.post('/cart', { productId, quantity })
  },
  updateQuantity(id, quantity) {
    return api.put(`/cart/${id}`, { quantity })
  },
  /** 使用 POST /cart/selection，避免 PUT 带多段路径在个别代理下 404 */
  updateSelection(id, isSelected) {
    return api.post('/cart/selection', { cartItemId: id, isSelected })
  },
  removeFromCart(id) {
    return api.delete(`/cart/${id}`)
  },
  clearCart() {
    return api.delete('/cart/clear')
  }
}

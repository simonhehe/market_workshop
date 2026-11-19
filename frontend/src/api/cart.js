import api from './index'

export const cartApi = {
  getCart() {
    return api.get('/cart')
  },
  addToCart(productId, quantity = 1) {
    return api.post('/cart', { productId, quantity })
  },
  selectItem(id, isSelected) {
    return api.put(`/cart/${id}/select`, { isSelected })
  },
  updateQuantity(id, quantity) {
    return api.put(`/cart/${id}`, { quantity })
  },
  removeFromCart(id) {
    return api.delete(`/cart/${id}`)
  },
  clearCart() {
    return api.delete('/cart/clear')
  }
}

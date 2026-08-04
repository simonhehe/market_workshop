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
  removeFromCart(id) {
    return api.delete(`/cart/${id}`)
  },
  selectCartItem(id, isSelected) {
    return api.put(`/cart/${id}/select`, { isSelected })
  },
  clearCart() {
    return api.delete('/cart/clear')
  }
}

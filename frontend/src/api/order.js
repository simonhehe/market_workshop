import api from './index'

export const orderApi = {
  getOrders() {
    return api.get('/orders')
  },
  getOrderById(id) {
    return api.get(`/orders/${id}`)
  },
  createOrder(data) {
    return api.post('/orders', data)
  },
  cancelOrder(id) {
    return api.put(`/orders/${id}/cancel`)
  }
}

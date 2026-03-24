import api from './index'

export const orderApi = {
  getOrders(params = {}) {
    return api.get('/orders', { params })
  },
  getOrderById(id) {
    return api.get(`/orders/${id}`)
  },
  createOrder(data) {
    return api.post('/orders', data)
  },
  cancelOrder(id) {
    return api.put(`/orders/${id}/cancel`)
  },
  confirmReceipt(id) {
    return api.put(`/orders/${id}/confirm-receipt`)
  }
}

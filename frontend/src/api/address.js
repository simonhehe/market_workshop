import api from './index'

export const addressApi = {
  getAddresses() {
    return api.get('/addresses')
  },
  createAddress(data) {
    return api.post('/addresses', data)
  },
  updateAddress(id, data) {
    return api.put(`/addresses/${id}`, data)
  },
  deleteAddress(id) {
    return api.delete(`/addresses/${id}`)
  }
}

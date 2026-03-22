import api from './index'

export const favoriteApi = {
  getFavorites() {
    return api.get('/favorites')
  },
  addFavorite(productId, tag) {
    return api.post('/favorites', { productId, tag })
  },
  removeFavorite(id) {
    return api.delete(`/favorites/${id}`)
  },
  checkFavorite(productId) {
    return api.get(`/favorites/check/${productId}`)
  }
}

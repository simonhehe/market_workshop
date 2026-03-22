import api from './index'

export const categoryApi = {
  getCategories() {
    return api.get('/categories')
  }
}

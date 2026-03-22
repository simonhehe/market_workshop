import api from './index'

export const authApi = {
  login(username, password) {
    return api.post('/auth/login', { username, password })
  },
  logout() {
    return api.post('/auth/logout')
  }
}

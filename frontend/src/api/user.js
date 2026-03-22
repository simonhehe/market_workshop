import api from './index'

export const userApi = {
  getUserInfo() {
    return api.get('/user/info')
  },
  updateUserInfo(data) {
    return api.put('/user/info', data)
  }
}

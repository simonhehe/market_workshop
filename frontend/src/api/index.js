import axios from 'axios'

/**
 * 必须使用「相对站点根」的绝对地址，避免在 /cart、/orders/1 等嵌套路由下，
 * 个别环境下把 baseURL `/api` 解析成「相对当前路径」→ 实际请求变成 /cart/api/...，
 * 无法命中 Vite 的 proxy(`/api`)，从而 404。
 */
function resolveApiBaseURL() {
  if (typeof window === 'undefined' || !window.location) {
    return '/api'
  }
  return new URL('/api', window.location.origin).href
}

const api = axios.create({
  baseURL: resolveApiBaseURL(),
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器 - 添加token
api.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器 - 处理错误
api.interceptors.response.use(
  response => {
    return response.data
  },
  error => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

export default api

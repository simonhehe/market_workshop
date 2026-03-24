import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/Login.vue'
import Home from '../views/Home.vue'
import Products from '../views/Products.vue'
import ProductDetail from '../views/ProductDetail.vue'
import Cart from '../views/Cart.vue'
import Orders from '../views/Orders.vue'
import OrderDetail from '../views/OrderDetail.vue'
import OrderCreate from '../views/OrderCreate.vue'
import Profile from '../views/Profile.vue'
import Favorites from '../views/Favorites.vue'
import AdminDashboard from '../views/admin/Dashboard.vue'
import AdminProducts from '../views/admin/Products.vue'
import AdminOrders from '../views/admin/Orders.vue'
import AdminUsers from '../views/admin/Users.vue'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: Login
  },
  {
    path: '/',
    name: 'Home',
    component: Home,
    meta: { requiresAuth: true }
  },
  {
    path: '/products',
    name: 'Products',
    component: Products,
    meta: { requiresAuth: true }
  },
  {
    path: '/products/:id',
    name: 'ProductDetail',
    component: ProductDetail,
    meta: { requiresAuth: true }
  },
  {
    path: '/cart',
    name: 'Cart',
    component: Cart,
    meta: { requiresAuth: true }
  },
  {
    path: '/orders',
    name: 'Orders',
    component: Orders,
    meta: { requiresAuth: true }
  },
  {
    path: '/orders/create',
    name: 'OrderCreate',
    component: OrderCreate,
    meta: { requiresAuth: true }
  },
  {
    path: '/orders/:id',
    name: 'OrderDetail',
    component: OrderDetail,
    meta: { requiresAuth: true }
  },
  {
    path: '/profile',
    name: 'Profile',
    component: Profile,
    meta: { requiresAuth: true }
  },
  {
    path: '/favorites',
    name: 'Favorites',
    component: Favorites,
    meta: { requiresAuth: true }
  },
  {
    path: '/admin',
    name: 'AdminDashboard',
    component: AdminDashboard,
    meta: { requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/admin/products',
    name: 'AdminProducts',
    component: AdminProducts,
    meta: { requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/admin/orders',
    name: 'AdminOrders',
    component: AdminOrders,
    meta: { requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/admin/users',
    name: 'AdminUsers',
    component: AdminUsers,
    meta: { requiresAuth: true, requiresAdmin: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  try {
    console.log('Router guard: navigating to', to.path, 'from', from.path)
    const token = localStorage.getItem('token')
    let userInfo = {}
    try {
      const userInfoStr = localStorage.getItem('userInfo')
      if (userInfoStr) {
        userInfo = JSON.parse(userInfoStr)
      }
    } catch (e) {
      console.warn('Failed to parse userInfo from localStorage', e)
    }
    
    console.log('Token exists:', !!token, 'Requires auth:', to.meta.requiresAuth)
    
    if (to.meta.requiresAuth && !token) {
      console.log('Redirecting to /login (no token)')
      next('/login')
    } else if (to.meta.requiresAdmin && userInfo.role !== 'admin') {
      console.log('Redirecting to / (not admin)')
      next('/')
    } else {
      console.log('Allowing navigation to', to.path)
      next()
    }
  } catch (error) {
    console.error('Router guard error:', error)
    next('/login')
  }
})

export default router

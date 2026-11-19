import { defineConfig } from 'vitest/config'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  test: {
    environment: 'jsdom',
    globals: true,
    include: ['src/**/*.{test,spec}.js'],
    setupFiles: ['./src/test/setup.js'],
    coverage: {
      provider: 'v8',
      reporter: ['text', 'json-summary'],
      include: [
        'src/views/Checkout.vue',
        'src/views/Cart.vue',
        'src/views/Orders.vue',
        'src/views/OrderDetail.vue',
        'src/api/cart.js',
        'src/api/order.js'
      ]
    }
  }
})

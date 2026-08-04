import { defineConfig } from 'vitest/config'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  test: {
    environment: 'jsdom',
    globals: true,
    include: ['src/**/*.test.js', 'src/**/*.spec.js'],
    coverage: {
      provider: 'v8',
      include: [
        'src/mocks/checkoutAddresses.js',
        'src/checkout/**/*.js',
        'src/views/Checkout.vue'
      ],
      exclude: ['src/**/*.test.js', 'src/**/*.spec.js'],
      reportsDirectory: './coverage',
      reporter: ['text', 'json-summary', 'html']
    }
  },
  resolve: {
    alias: {
      '@': '/src'
    }
  }
})

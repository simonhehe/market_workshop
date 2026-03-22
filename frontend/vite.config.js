import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

/** 与 dev 共用：否则 `vite preview` / `npm run preview` 不会转发 /api，PUT 等会 404 */
const apiProxy = {
  '/api': {
    target: 'http://localhost:8080',
    changeOrigin: true,
    rewrite: (path) => path
  }
}

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 3000,
    host: true,
    open: false,
    strictPort: false,
    headers: {
      // Remove CSP header in dev mode to allow Vite HMR
    },
    proxy: apiProxy
  },
  resolve: {
    alias: {
      '@': '/src'
    }
  },
  build: {
    sourcemap: false,
    rollupOptions: {
      output: {
        manualChunks: undefined
      }
    }
  },
  preview: {
    port: 3000,
    host: true,
    proxy: apiProxy
  }
})

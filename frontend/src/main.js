import { createApp } from 'vue'
import App from './App.vue'
import router from './router'

const app = createApp(App)
app.use(router)

app.mount('#app')

// 调试信息
console.log('Vue app mounted successfully')
console.log('Current route:', router.currentRoute.value.path)

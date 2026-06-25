import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import { ElMessage } from 'element-plus'
import 'element-plus/dist/index.css'
import ECharts from 'vue-echarts'
import 'echarts'
import App from './App.vue'
import router from './router'
import { pinia } from './stores'
import './style.css'

const app = createApp(App)
app.use(pinia).use(router).use(ElementPlus).component('v-chart', ECharts)

// 全局 Vue 错误处理
app.config.errorHandler = (err, instance, info) => {
  console.error('Vue Error:', err, info)
  ElMessage.error('系统出现异常，请刷新页面后重试')
}

// 全局未捕获 Promise 拒绝处理
window.addEventListener('unhandledrejection', (event) => {
  console.error('Unhandled Promise Rejection:', event.reason)
  // 避免重复提示（axios 拦截器已处理的 401 等不再提示）
  if (event.reason?.message && !event.reason.message.includes('登录已过期')) {
    ElMessage.error('操作失败: ' + (event.reason.message || '网络错误'))
  }
})

app.mount('#app')

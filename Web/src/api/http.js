import axios from 'axios'
import { ElMessage } from 'element-plus'

const baseURL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'

const client = axios.create({
  baseURL,
  timeout: 15000,
})

// 根据请求 URL 判断所属区域，用于 401 时清除对应 token
function getAreaFromUrl(url) {
  if (url.includes('/admin/')) return 'admin'
  if (url.includes('/delivery/')) return 'delivery'
  return 'user'
}

const TOKEN_KEYS = { admin: 'admin_token', user: 'user_token', delivery: 'delivery_token' }
const PROFILE_KEYS = { admin: 'admin_profile', user: 'user_profile', delivery: 'delivery_profile' }

function clearAuth(area) {
  localStorage.removeItem(TOKEN_KEYS[area] || 'user_token')
  localStorage.removeItem(PROFILE_KEYS[area] || 'user_profile')
}

client.interceptors.response.use(
  (response) => {
    const payload = response.data
    if (payload?.code !== 200) {
      if (payload?.code === 401) {
        const area = getAreaFromUrl(response.config.url || '')
        clearAuth(area)
        ElMessage.error('登录已过期，请重新登录')
      }
      return Promise.reject(new Error(payload?.message || '请求失败'))
    }
    return payload.data
  },
  (error) => {
    if (error.response?.status === 401) {
      const area = getAreaFromUrl(error.config?.url || '')
      clearAuth(area)
      ElMessage.error('登录已过期，请重新登录')
    } else if (!error.response) {
      ElMessage.error('网络连接失败，请检查网络')
    }
    return Promise.reject(error)
  },
)

export function request({ url, method = 'get', params, data, area = 'public' }) {
  const key = area === 'admin' ? 'admin_token' : area === 'user' ? 'user_token' : area === 'delivery' ? 'delivery_token' : ''
  const token = key ? localStorage.getItem(key) : ''
  return client({
    url,
    method,
    params,
    data,
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  })
}

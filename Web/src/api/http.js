import axios from 'axios'

const baseURL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'

const client = axios.create({
  baseURL,
  timeout: 15000,
})

client.interceptors.response.use(
  (response) => {
    const payload = response.data
    if (payload?.code !== 200) {
      return Promise.reject(new Error(payload?.message || '请求失败'))
    }
    return payload.data
  },
  (error) => Promise.reject(error),
)

export function request({ url, method = 'get', params, data, area = 'public' }) {
  const key = area === 'admin' ? 'admin_token' : area === 'user' ? 'user_token' : ''
  const token = key ? localStorage.getItem(key) : ''
  return client({
    url,
    method,
    params,
    data,
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  })
}

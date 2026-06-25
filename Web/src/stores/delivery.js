import { defineStore } from 'pinia'
import { ElMessage } from 'element-plus'
import { deliveryApi } from '../api'

export const useDeliveryStore = defineStore('delivery', {
  state: () => ({
    token: localStorage.getItem('delivery_token') || '',
    profile: JSON.parse(localStorage.getItem('delivery_profile') || 'null'),
  }),
  getters: {
    isLoggedIn: (state) => !!state.token,
  },
  actions: {
    async login({ username, password }) {
      const data = await deliveryApi.login({ username, password })
      this.setSession(data)
    },
    async bootstrap() {
      if (!this.token) return
      try {
        const profile = await deliveryApi.profile()
        this.profile = profile
        localStorage.setItem('delivery_profile', JSON.stringify(profile))
      } catch (error) {
        this.logout()
      }
    },
    setSession(data) {
      this.token = data.token
      this.profile = data.staffInfo
      localStorage.setItem('delivery_token', data.token)
      localStorage.setItem('delivery_profile', JSON.stringify(data.staffInfo))
    },
    logout() {
      this.token = ''
      this.profile = null
      localStorage.removeItem('delivery_token')
      localStorage.removeItem('delivery_profile')
    },
  },
})

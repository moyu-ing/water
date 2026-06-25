import { defineStore } from 'pinia'
import { ElMessage } from 'element-plus'
import { adminApi } from '../api'

export const useAdminStore = defineStore('admin', {
  state: () => ({
    token: localStorage.getItem('admin_token') || '',
    profile: JSON.parse(localStorage.getItem('admin_profile') || 'null'),
  }),
  getters: {
    isLoggedIn: (state) => !!state.token,
    permissions: (state) => state.profile?.permissions || [],
  },
  actions: {
    setSession(token, profile) {
      this.token = token
      this.profile = profile
      localStorage.setItem('admin_token', token)
      localStorage.setItem('admin_profile', JSON.stringify(profile))
    },
    logout() {
      this.token = ''
      this.profile = null
      localStorage.removeItem('admin_token')
      localStorage.removeItem('admin_profile')
    },
    async login(form) {
      const result = await adminApi.login(form)
      this.setSession(result.token, result.adminInfo)
    },
    async bootstrap() {
      if (!this.token) return
      try {
        this.profile = await adminApi.profile()
        localStorage.setItem('admin_profile', JSON.stringify(this.profile))
      } catch (error) {
        this.logout()
      }
    },
  },
})

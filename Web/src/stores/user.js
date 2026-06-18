import { defineStore } from 'pinia'
import { userApi } from '../api'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('user_token') || '',
    profile: JSON.parse(localStorage.getItem('user_profile') || 'null'),
  }),
  getters: {
    isLoggedIn: (state) => !!state.token,
  },
  actions: {
    setSession(token, profile) {
      this.token = token
      this.profile = profile
      localStorage.setItem('user_token', token)
      localStorage.setItem('user_profile', JSON.stringify(profile))
    },
    logout() {
      this.token = ''
      this.profile = null
      localStorage.removeItem('user_token')
      localStorage.removeItem('user_profile')
    },
    async login(form) {
      const result = await userApi.login(form)
      this.setSession(result.token, result.userInfo)
    },
    async bootstrap() {
      if (!this.token) return
      try {
        this.profile = await userApi.profile()
        localStorage.setItem('user_profile', JSON.stringify(this.profile))
      } catch (error) {
        this.logout()
      }
    },
  },
})

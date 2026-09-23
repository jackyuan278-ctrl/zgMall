import { defineStore } from 'pinia'
import { userApi } from '@/api'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('zg_token') || '',
    username: localStorage.getItem('zg_username') || '',
    userInfo: null
  }),
  getters: {
    logged: (state) => !!state.token
  },
  actions: {
    async login(form) {
      const res = await userApi.login(form)
      this.token = res.token
      this.username = res.username
      localStorage.setItem('zg_token', res.token)
      localStorage.setItem('zg_username', res.username)
    },
    logout() {
      this.token = ''
      this.username = ''
      this.userInfo = null
      localStorage.removeItem('zg_token')
      localStorage.removeItem('zg_username')
    },
    async fetchMe() {
      this.userInfo = await userApi.me()
    },
    async updateMe(data) {
      this.userInfo = await userApi.updateMe(data)
    }
  }
})

import { defineStore } from 'pinia'
import { userApi } from '@/api'
import { clearAuth, isTokenFresh } from '@/utils/auth'

export const useUserStore = defineStore('user', {
  state: () => {
    // 启动时 token 若已过期就当未登录，否则头部显示"已登录"而所有接口都 401
    const fresh = isTokenFresh()
    return {
      token: fresh ? localStorage.getItem('zg_token') : '',
      username: fresh ? (localStorage.getItem('zg_username') || '') : '',
      userInfo: null
    }
  },
  getters: {
    logged: (state) => isTokenFresh(state.token)
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
      clearAuth()
    },
    async fetchMe() {
      this.userInfo = await userApi.me()
    },
    async updateMe(data) {
      this.userInfo = await userApi.updateMe(data)
    }
  }
})

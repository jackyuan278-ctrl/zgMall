import axios from 'axios'
import { ElMessage } from 'element-plus'
import { toLogin } from '@/utils/auth'

const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

request.interceptors.request.use((config) => {
  const token = localStorage.getItem('zg_token')
  if (token) {
    config.headers.authorization = token
  }
  return config
})

request.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res.code !== 200) {
      ElMessage.error(res.msg || '请求失败')
      return Promise.reject(new Error(res.msg))
    }
    return res.data
  },
  (error) => {
    if (error.response?.status === 401) {
      ElMessage.warning('登录已过期，请重新登录')
      toLogin(window.location.pathname)
    } else {
      ElMessage.error(error.message || '网络异常')
    }
    return Promise.reject(error)
  }
)

export default request

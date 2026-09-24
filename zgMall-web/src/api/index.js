import request from './request'
import { items, categories } from '@/mock/items'

// ============ 后端网关（zg-gateway 8080）启动后改成 false 即可对接真实接口 ============
export const USE_MOCK = false

// ---------------- 用户（已切真实接口，后端网关 8080） ----------------
export const userApi = {
  login(data) {
    return request.post('/users/login', data)
  },
  register(data) {
    return request.post('/users/register', data)
  },
  changePassword(data) {
    return request.put('/users/password', data)
  },
  me() {
    return request.get('/users/me')
  },
  updateMe(data) {
    return request.put('/users/me', data)
  }
}

// ---------------- 商品 ----------------
export const itemApi = {
  categories() {
    if (USE_MOCK) return Promise.resolve(categories)
    return request.get('/items/categories')
  },
  page(params = {}) {
    if (USE_MOCK) {
      const { keyword = '', categoryId = null, page = 1, pageSize = 10, sort = '' } = params
      let list = items.filter((it) => it.status !== 2)
      if (categoryId) list = list.filter((it) => it.categoryId === Number(categoryId))
      if (keyword) {
        const kw = keyword.trim()
        list = list.filter((it) => it.name.includes(kw) || it.brand.includes(kw) || it.categoryName.includes(kw))
      }
      if (sort === 'sales') list = [...list].sort((a, b) => b.sales - a.sales)
      if (sort === 'priceAsc') list = [...list].sort((a, b) => a.price - b.price)
      if (sort === 'priceDesc') list = [...list].sort((a, b) => b.price - a.price)
      const total = list.length
      return Promise.resolve({ list: list.slice((page - 1) * pageSize, page * pageSize), total })
    }
    return request.get('/items', { params })
  },
  detail(id) {
    if (USE_MOCK) {
      const item = items.find((it) => it.id === Number(id))
      return item ? Promise.resolve(item) : Promise.reject(new Error('商品不存在'))
    }
    return request.get(`/items/${id}`)
  }
}

// ---------------- 购物车 ----------------
// mock 模式购物车走 stores/cart.js 的 localStorage，这里只定义真实接口
export const cartApi = {
  list() {
    return request.get('/cart')
  },
  add(data) {
    return request.post('/cart', data)
  },
  updateNum(itemId, num) {
    return request.put(`/cart/${itemId}`, { num })
  },
  remove(itemId) {
    return request.delete(`/cart/${itemId}`)
  },
  clear() {
    return request.delete('/cart')
  }
}

// ---------------- 收货地址 ----------------
const ADDR_KEY = 'zg_mock_addresses'

function initAddresses() {
  const seed = [
    { id: 1, receiver: '张同学', phone: '13800138000', province: '江西省', city: '南昌市', district: '红谷滩区', detail: '学府大道999号 南昌大学前湖校区 3栋302', isDefault: 1 },
    { id: 2, receiver: '张同学', phone: '13800138000', province: '江西省', city: '南昌市', district: '青山湖区', detail: '南京东路235号 南昌大学青山湖校区', isDefault: 0 }
  ]
  localStorage.setItem(ADDR_KEY, JSON.stringify(seed))
  return seed
}

function loadAddresses() {
  const raw = localStorage.getItem(ADDR_KEY)
  return raw ? JSON.parse(raw) : initAddresses()
}

function saveAddresses(list) {
  localStorage.setItem(ADDR_KEY, JSON.stringify(list))
}

export const addressApi = {
  list() {
    if (USE_MOCK) return Promise.resolve(loadAddresses())
    return request.get('/users/addresses')
  },
  save(data) {
    if (USE_MOCK) {
      const list = loadAddresses()
      if (data.isDefault) list.forEach((a) => (a.isDefault = 0))
      const addr = { id: Date.now(), ...data }
      if (list.length === 0) addr.isDefault = 1
      list.unshift(addr)
      saveAddresses(list)
      return Promise.resolve(true)
    }
    return request.post('/users/addresses', data)
  },
  update(id, data) {
    if (USE_MOCK) {
      const list = loadAddresses()
      const i = list.findIndex((a) => a.id === Number(id))
      if (i > -1) {
        if (data.isDefault) list.forEach((a) => (a.isDefault = 0))
        list[i] = { ...list[i], ...data }
        saveAddresses(list)
      }
      return Promise.resolve(true)
    }
    return request.put(`/users/addresses/${id}`, data)
  },
  remove(id) {
    if (USE_MOCK) {
      saveAddresses(loadAddresses().filter((a) => a.id !== Number(id)))
      return Promise.resolve(true)
    }
    return request.delete(`/users/addresses/${id}`)
  },
  setDefault(id) {
    if (USE_MOCK) {
      const list = loadAddresses()
      list.forEach((a) => (a.isDefault = a.id === Number(id) ? 1 : 0))
      saveAddresses(list)
      return Promise.resolve(true)
    }
    return request.put(`/users/addresses/${id}/default`)
  }
}

// ---------------- 订单 ----------------
export const orderApi = {
  create({ receiver, phone, address, remark, goods }) {
    return request.post('/orders', { receiver, phone, address, remark, goods })
  },
  list() {
    return request.get('/orders')
  },
  detail(orderId) {
    return request.get(`/orders/${orderId}`)
  },
  pay(orderId, payType = 'mock') {
    return request.post(`/pay/orders/${orderId}/pay`, { payType })
  }
}

export function formatPrice(fen) {
  return (fen / 100).toFixed(2)
}

export const ORDER_STATUS = {
  1: { text: '待付款', type: 'warning' },
  2: { text: '已付款', type: 'primary' },
  3: { text: '已发货', type: 'success' },
  4: { text: '已完成', type: 'success' },
  5: { text: '已关闭', type: 'info' }
}

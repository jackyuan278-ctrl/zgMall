import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/', name: 'home', component: () => import('@/views/HomeView.vue') },
  { path: '/items', name: 'item-list', component: () => import('@/views/ItemListView.vue') },
  { path: '/items/:id', name: 'item-detail', component: () => import('@/views/ItemDetailView.vue') },
  { path: '/cart', name: 'cart', component: () => import('@/views/CartView.vue') },
  { path: '/order/confirm', name: 'order-confirm', component: () => import('@/views/OrderConfirmView.vue') },
  { path: '/orders', name: 'orders', component: () => import('@/views/OrderListView.vue') },
  { path: '/address', name: 'address', component: () => import('@/views/AddressView.vue') },
  { path: '/profile', name: 'profile', component: () => import('@/views/ProfileView.vue') },
  { path: '/password', name: 'password', component: () => import('@/views/ChangePasswordView.vue') },
  { path: '/login', name: 'login', component: () => import('@/views/LoginView.vue'), meta: { bare: true } },
  { path: '/ai', name: 'ai-chat', component: () => import('@/views/AiChatView.vue') }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to) => {
  const token = localStorage.getItem('zg_token')
  const needLogin = ['/cart', '/order/confirm', '/orders', '/address', '/profile', '/password', '/ai'].includes(to.path)
  if (needLogin && !token) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }
})

export default router

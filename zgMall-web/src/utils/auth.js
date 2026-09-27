const TOKEN_KEY = 'zg_token'
const NAME_KEY = 'zg_username'

export function getToken() {
  return localStorage.getItem(TOKEN_KEY) || ''
}

/**
 * 只解 JWT payload 看 exp，不校验签名（签名由网关校验）。
 * 路由守卫和 SSE 都靠它判断"还登着吗"，避免拿着过期 token 进页面后被接口一路 401。
 */
export function isTokenFresh(token = getToken()) {
  if (!token) return false
  try {
    const payload = JSON.parse(atob(token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')))
    return !payload.exp || payload.exp * 1000 > Date.now()
  } catch {
    return false
  }
}

export function clearAuth() {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(NAME_KEY)
}

export function toLogin(redirect) {
  clearAuth()
  window.location.href = redirect ? `/login?redirect=${encodeURIComponent(redirect)}` : '/login'
}

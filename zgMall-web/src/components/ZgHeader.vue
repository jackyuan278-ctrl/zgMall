<template>
  <header class="zg-header">
    <div class="zg-header__inner">
      <router-link to="/" class="zg-logo">
        <span class="zg-logo__mark">智</span>
        <span class="zg-logo__text">智购商城</span>
      </router-link>

      <div class="zg-search">
        <el-input
          v-model="keyword"
          placeholder="搜索商品，例如：手机、空调、跑鞋"
          clearable
          @keyup.enter="doSearch"
        >
          <template #append>
            <el-button :icon="Search" @click="doSearch" />
          </template>
        </el-input>
      </div>

      <nav class="zg-nav">
        <router-link to="/" class="zg-nav__link">首页</router-link>
        <router-link to="/items" class="zg-nav__link">全部商品</router-link>
        <router-link to="/ai" class="zg-nav__link zg-nav__link--ai">
          <span class="zg-nav__ai-icon">✨</span> 智购导购
        </router-link>
      </nav>

      <div class="zg-actions">
        <router-link to="/cart" class="zg-cart-btn">
          <el-badge :value="cartStore.totalNum" :hidden="cartStore.totalNum === 0" :max="99">
            <span class="zg-cart-icon">🛒</span>
          </el-badge>
        </router-link>
        <template v-if="userStore.logged">
          <el-dropdown @command="onCommand">
            <span class="zg-user">
              <el-avatar :size="28" class="zg-avatar">{{ userStore.username[0] }}</el-avatar>
              {{ userStore.username }}
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人资料</el-dropdown-item>
                <el-dropdown-item command="address">我的地址</el-dropdown-item>
                <el-dropdown-item command="orders">我的订单</el-dropdown-item>
                <el-dropdown-item command="password">修改密码</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
        <el-button v-else type="primary" plain @click="$router.push('/login')">登录</el-button>
      </div>
    </div>
  </header>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { Search } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { useCartStore } from '@/stores/cart'

const router = useRouter()
const keyword = ref('')
const userStore = useUserStore()
const cartStore = useCartStore()

function doSearch() {
  router.push({ path: '/items', query: keyword.value ? { keyword: keyword.value } : {} })
}

function onCommand(cmd) {
  if (cmd === 'logout') {
    userStore.logout()
    router.push('/')
  } else if (cmd === 'orders') {
    router.push('/orders')
  } else if (cmd === 'password') {
    router.push('/password')
  } else if (cmd === 'profile') {
    router.push('/profile')
  } else if (cmd === 'address') {
    router.push('/address')
  }
}
</script>

<style scoped>
.zg-header {
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid var(--zg-border);
  position: sticky;
  top: 0;
  z-index: 100;
}
.zg-header__inner {
  max-width: 1200px;
  margin: 0 auto;
  height: 64px;
  display: flex;
  align-items: center;
  gap: 24px;
  padding: 0 8px;
}
.zg-logo {
  display: flex;
  align-items: center;
  gap: 6px;
  text-decoration: none;
  flex-shrink: 0;
}
.zg-logo__mark {
  width: 32px;
  height: 32px;
  border-radius: 10px;
  background: linear-gradient(135deg, var(--zg-primary), var(--zg-accent));
  color: #fff;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
}
.zg-logo__text {
  font-size: 18px;
  font-weight: 700;
  color: var(--zg-primary-dark);
}
.zg-search {
  width: 360px;
}
.zg-nav {
  display: flex;
  gap: 12px;
  margin-left: auto;
}
.zg-nav__link {
  text-decoration: none;
  color: var(--zg-text);
  font-size: 14px;
  padding: 6px 12px;
  border-radius: 8px;
  transition: all 0.2s;
}
.zg-nav__link:hover {
  color: var(--zg-primary-dark);
  background: var(--el-color-primary-light-9);
}
.zg-nav__link--ai {
  color: #fff;
  background: linear-gradient(135deg, var(--zg-primary), var(--zg-accent));
}
.zg-nav__link--ai:hover {
  color: #fff;
  opacity: 0.9;
}
.zg-nav__ai-icon {
  margin-right: 2px;
}
.zg-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}
.zg-cart-btn {
  text-decoration: none;
  font-size: 22px;
  line-height: 1;
}
.zg-cart-icon {
  vertical-align: middle;
}
.zg-user {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  font-size: 14px;
  outline: none;
}
.zg-avatar {
  background: var(--zg-primary);
  color: #fff;
}
</style>

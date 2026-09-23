<template>
  <div class="login-page">
    <div class="login-card">
      <div class="login-brand">
        <span class="login-brand__mark">智</span>
        <div>
          <h1>智购商城</h1>
          <p>AI 导购 · 智购优选</p>
        </div>
      </div>

      <el-tabs v-model="tab" stretch>
        <el-tab-pane label="登录" name="login">
          <el-form :model="form" label-position="top" @submit.prevent>
            <el-form-item label="用户名">
              <el-input v-model="form.username" placeholder="demo" size="large" clearable />
            </el-form-item>
            <el-form-item label="密码">
              <el-input v-model="form.password" type="password" placeholder="请输入密码" size="large" show-password @keyup.enter="submit" />
            </el-form-item>
          </el-form>
          <el-button type="primary" size="large" class="login-btn" :loading="loading" @click="submit">
            登 录
          </el-button>
        </el-tab-pane>
        <el-tab-pane label="注册" name="register">
          <el-form :model="form" label-position="top" @submit.prevent>
            <el-form-item label="用户名">
              <el-input v-model="form.username" size="large" clearable />
            </el-form-item>
            <el-form-item label="手机号">
              <el-input v-model="form.phone" size="large" clearable />
            </el-form-item>
            <el-form-item label="密码">
              <el-input v-model="form.password" type="password" size="large" show-password />
            </el-form-item>
          </el-form>
          <el-button type="primary" size="large" class="login-btn" :loading="loading" @click="submit">
            注册并登录
          </el-button>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { userApi } from '@/api'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const tab = ref('login')
const loading = ref(false)
const form = reactive({ username: '', phone: '', password: '' })

async function submit() {
  if (!form.username || !form.password) {
    ElMessage.warning('请填写用户名和密码')
    return
  }
  loading.value = true
  try {
    if (tab.value === 'register') {
      await userApi.register(form)
    }
    await userStore.login(form)
    ElMessage.success('欢迎回来')
    router.push(route.query.redirect || '/')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #e6faf7 0%, #e0f2fe 55%, #f0f9ff 100%);
  display: flex;
  align-items: center;
  justify-content: center;
}
.login-card {
  width: 420px;
  background: #fff;
  border-radius: 20px;
  padding: 36px 40px;
  box-shadow: 0 20px 50px rgba(13, 148, 136, 0.12);
}
.login-brand {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;
}
.login-brand__mark {
  width: 48px;
  height: 48px;
  border-radius: 14px;
  background: linear-gradient(135deg, var(--zg-primary), var(--zg-accent));
  color: #fff;
  font-size: 24px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
}
.login-brand h1 {
  margin: 0;
  font-size: 20px;
  color: #115e59;
}
.login-brand p {
  margin: 2px 0 0;
  font-size: 12px;
  color: var(--zg-text-light);
}
.login-btn {
  width: 100%;
  margin-top: 8px;
}
</style>

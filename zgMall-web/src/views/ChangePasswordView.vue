<template>
  <div class="pwd-wrap">
    <div class="pwd-card">
      <h2 class="pwd-card__title">修改密码</h2>
      <p class="pwd-card__tip">修改成功后需要重新登录</p>

      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @submit.prevent>
        <el-form-item label="原密码" prop="oldPassword">
          <el-input v-model="form.oldPassword" type="password" size="large" show-password placeholder="请输入当前密码" />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="form.newPassword" type="password" size="large" show-password placeholder="6-20位，建议字母+数字组合" />
        </el-form-item>
        <el-form-item label="确认新密码" prop="confirmPassword">
          <el-input v-model="form.confirmPassword" type="password" size="large" show-password placeholder="再次输入新密码" @keyup.enter="submit" />
        </el-form-item>
      </el-form>

      <el-button type="primary" size="large" class="pwd-btn" :loading="loading" @click="submit">
        确认修改
      </el-button>
      <el-button size="large" class="pwd-btn pwd-btn--plain" @click="$router.back()">返回</el-button>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { userApi } from '@/api'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const rules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为 6-20 位', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value && value === form.oldPassword) {
          callback(new Error('新密码不能与原密码相同'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== form.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

async function submit() {
  try {
    await formRef.value.validate()
  } catch (err) {
    return
  }
  loading.value = true
  try {
    await userApi.changePassword({
      oldPassword: form.oldPassword,
      newPassword: form.newPassword
    })
    ElMessage.success('密码修改成功，请重新登录')
    userStore.logout()
    router.push('/login')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.pwd-wrap {
  display: flex;
  justify-content: center;
  padding-top: 40px;
}
.pwd-card {
  width: 480px;
  background: #fff;
  border-radius: 20px;
  padding: 32px 40px;
  box-shadow: var(--zg-card-shadow);
}
.pwd-card__title {
  margin: 0 0 6px;
  font-size: 20px;
  color: #115e59;
}
.pwd-card__tip {
  margin: 0 0 24px;
  font-size: 13px;
  color: var(--zg-text-light);
}
.pwd-btn {
  width: 100%;
  margin: 8px 0 0;
}
.pwd-btn--plain {
  margin-top: 12px;
}
</style>

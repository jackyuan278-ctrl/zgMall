<template>
  <div class="zg-profile">
    <h2 class="zg-page-title">个人资料</h2>

    <div class="zg-profile__grid">
      <el-card class="zg-profile__card" shadow="never">
        <div class="zg-profile__head">
          <el-avatar :size="72" :src="info.avatar || ''" class="zg-profile__avatar">
            {{ avatarText }}
          </el-avatar>
          <div class="zg-profile__head-info">
            <div class="zg-profile__nickname">{{ info.nickname || info.username }}</div>
            <div class="zg-profile__username">账号：{{ info.username }}</div>
          </div>
        </div>
        <el-divider />
        <div class="zg-profile__balance">
          <span class="zg-profile__balance-label">账户余额</span>
          <span class="zg-profile__balance-value">¥ {{ formatPrice(info.balance || 0) }}</span>
        </div>
        <el-button type="primary" plain class="zg-profile__password-btn" @click="$router.push('/password')">
          修改密码
        </el-button>
      </el-card>

      <el-card class="zg-profile__card" shadow="never">
        <template #header>
          <span class="zg-profile__card-title">编辑资料</span>
        </template>
        <el-form :model="form" :rules="rules" ref="formRef" label-width="80px" class="zg-profile__form">
          <el-form-item label="昵称" prop="nickname">
            <el-input v-model="form.nickname" placeholder="请输入昵称" maxlength="20" show-word-limit />
          </el-form-item>
          <el-form-item label="手机号" prop="phone">
            <el-input v-model="form.phone" placeholder="请输入手机号" maxlength="11" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="saving" @click="onSave">保存修改</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { formatPrice } from '@/api'

const userStore = useUserStore()
const info = computed(() => userStore.userInfo || {})
const avatarText = computed(() => {
  const name = info.value.nickname || info.value.username || '用'
  return name[0]
})

const formRef = ref()
const saving = ref(false)
const form = reactive({ nickname: '', phone: '' })

const rules = {
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ]
}

onMounted(async () => {
  await userStore.fetchMe()
  form.nickname = info.value.nickname || ''
  form.phone = info.value.phone || ''
})

async function onSave() {
  await formRef.value.validate()
  saving.value = true
  try {
    await userStore.updateMe({ nickname: form.nickname, phone: form.phone })
    ElMessage.success('保存成功')
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.zg-profile__grid {
  display: grid;
  grid-template-columns: 320px 1fr;
  gap: 16px;
  align-items: start;
}
.zg-profile__card {
  border: none;
  border-radius: 16px;
  box-shadow: var(--zg-card-shadow);
}
.zg-profile__head {
  display: flex;
  align-items: center;
  gap: 16px;
}
.zg-profile__avatar {
  background: linear-gradient(135deg, var(--zg-primary), var(--zg-accent));
  color: #fff;
  font-size: 28px;
  flex-shrink: 0;
}
.zg-profile__nickname {
  font-size: 18px;
  font-weight: 700;
}
.zg-profile__username {
  margin-top: 4px;
  font-size: 13px;
  color: var(--zg-text-light);
}
.zg-profile__balance {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.zg-profile__balance-label {
  font-size: 14px;
  color: var(--zg-text-light);
}
.zg-profile__balance-value {
  font-size: 20px;
  font-weight: 700;
  color: var(--zg-price);
}
.zg-profile__password-btn {
  width: 100%;
  margin-top: 16px;
}
.zg-profile__card-title {
  font-weight: 700;
}
.zg-profile__form {
  max-width: 420px;
}
</style>

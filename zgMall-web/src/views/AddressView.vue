<template>
  <div>
    <div class="addr-head">
      <h2 class="zg-page-title">收货地址</h2>
      <el-button type="primary" @click="openAdd">新增地址</el-button>
    </div>

    <div v-loading="loading" class="addr-list">
      <div v-for="a in list" :key="a.id" class="addr-card">
        <div class="addr-card__main">
          <div class="addr-card__top">
            <b class="addr-card__receiver">{{ a.receiver }}</b>
            <span class="addr-card__phone">{{ a.phone }}</span>
            <el-tag v-if="a.isDefault" type="success" effect="light" size="small">默认</el-tag>
          </div>
          <p class="addr-card__detail">
            {{ a.province }}{{ a.city }}{{ a.district }} {{ a.detail }}
          </p>
        </div>
        <div class="addr-card__ops">
          <el-button v-if="!a.isDefault" text type="primary" @click="setDefault(a)">设为默认</el-button>
          <el-button text type="primary" @click="openEdit(a)">编辑</el-button>
          <el-button text type="danger" @click="remove(a)">删除</el-button>
        </div>
      </div>

      <el-empty v-if="!loading && list.length === 0" description="还没有收货地址">
        <el-button type="primary" @click="openAdd">新增地址</el-button>
      </el-empty>
    </div>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑地址' : '新增地址'" width="480px" align-center>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="收货人" prop="receiver">
          <el-input v-model="form.receiver" placeholder="请输入收货人姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" maxlength="11" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="所在地区" prop="region">
          <el-row :gutter="8">
            <el-col :span="8"><el-input v-model="form.province" placeholder="省" /></el-col>
            <el-col :span="8"><el-input v-model="form.city" placeholder="市" /></el-col>
            <el-col :span="8"><el-input v-model="form.district" placeholder="区/县" /></el-col>
          </el-row>
        </el-form-item>
        <el-form-item label="详细地址" prop="detail">
          <el-input v-model="form.detail" type="textarea" :rows="2" placeholder="街道、小区、楼栋、门牌号等" />
        </el-form-item>
        <el-form-item label="设为默认">
          <el-switch v-model="form.isDefault" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { addressApi } from '@/api'

const list = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const saving = ref(false)
const editingId = ref(null)
const formRef = ref(null)

const form = reactive({
  receiver: '',
  phone: '',
  province: '',
  city: '',
  district: '',
  detail: '',
  isDefault: 0
})

const rules = {
  receiver: [{ required: true, message: '请输入收货人', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  detail: [{ required: true, message: '请输入详细地址', trigger: 'blur' }]
}

async function load() {
  loading.value = true
  try {
    list.value = await addressApi.list()
  } finally {
    loading.value = false
  }
}

function resetForm() {
  Object.assign(form, {
    receiver: '',
    phone: '',
    province: '',
    city: '',
    district: '',
    detail: '',
    isDefault: 0
  })
}

function openAdd() {
  editingId.value = null
  resetForm()
  dialogVisible.value = true
}

function openEdit(a) {
  editingId.value = a.id
  Object.assign(form, {
    receiver: a.receiver,
    phone: a.phone,
    province: a.province || '',
    city: a.city || '',
    district: a.district || '',
    detail: a.detail,
    isDefault: a.isDefault || 0
  })
  dialogVisible.value = true
}

async function submit() {
  try {
    await formRef.value.validate()
  } catch (err) {
    return
  }
  saving.value = true
  try {
    if (editingId.value) {
      await addressApi.update(editingId.value, { ...form })
      ElMessage.success('地址已更新')
    } else {
      await addressApi.save({ ...form })
      ElMessage.success('地址已新增')
    }
    dialogVisible.value = false
    load()
  } finally {
    saving.value = false
  }
}

async function setDefault(a) {
  await addressApi.setDefault(a.id)
  ElMessage.success('已设为默认地址')
  load()
}

async function remove(a) {
  try {
    await ElMessageBox.confirm(`确定删除该地址吗？`, '提示', { type: 'warning' })
  } catch (err) {
    return
  }
  await addressApi.remove(a.id)
  ElMessage.success('已删除')
  load()
}

onMounted(load)
</script>

<style scoped>
.addr-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.addr-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-height: 200px;
}
.addr-card {
  background: #fff;
  border-radius: 16px;
  padding: 18px 24px;
  box-shadow: var(--zg-card-shadow);
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
}
.addr-card__top {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 6px;
}
.addr-card__receiver {
  font-size: 15px;
}
.addr-card__phone {
  color: var(--zg-text-light);
  font-size: 14px;
}
.addr-card__detail {
  margin: 0;
  color: var(--zg-text);
  font-size: 14px;
}
.addr-card__ops {
  display: flex;
  gap: 4px;
  flex-shrink: 0;
}
</style>

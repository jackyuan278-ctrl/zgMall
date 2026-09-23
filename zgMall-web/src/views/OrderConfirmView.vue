<template>
  <div>
    <h2 class="zg-page-title">确认订单</h2>

    <div class="block">
      <h3>收货信息</h3>
      <el-radio-group v-model="addrMode" class="addr-mode">
        <el-radio-button value="book">从地址簿选择</el-radio-button>
        <el-radio-button value="manual">手动填写</el-radio-button>
      </el-radio-group>

      <template v-if="addrMode === 'book'">
        <div v-if="addresses.length" class="addr-list">
          <div
            v-for="a in addresses"
            :key="a.id"
            class="addr-item"
            :class="{ 'addr-item--active': a.id === selectedId }"
            @click="selectedId = a.id"
          >
            <div class="addr-item__line">
              <b>{{ a.receiver }}</b>
              <span class="addr-item__phone">{{ a.phone }}</span>
              <el-tag v-if="a.isDefault" size="small" type="success" effect="plain">默认</el-tag>
            </div>
            <div class="addr-item__addr">{{ a.province }} {{ a.city }} {{ a.district }} {{ a.detail }}</div>
          </div>
        </div>
        <el-empty v-else description="暂无收货地址，请先添加" :image-size="60">
          <el-button type="primary" plain size="small" @click="$router.push('/address')">去添加地址</el-button>
        </el-empty>
      </template>

      <el-form v-else :model="form" label-width="80px" style="max-width: 560px">
        <el-form-item label="收货人">
          <el-input v-model="form.receiver" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="form.phone" maxlength="11" />
        </el-form-item>
        <el-form-item label="收货地址">
          <el-input v-model="form.address" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" placeholder="选填" />
        </el-form-item>
      </el-form>
    </div>

    <div class="block">
      <h3>商品清单</h3>
      <div v-for="g in goods" :key="g.itemId" class="goods-row">
        <img class="goods-row__thumb" :src="g.image" :alt="g.name" />
        <span class="goods-row__name">{{ g.name }}</span>
        <span class="goods-row__num">x{{ g.num }}</span>
        <span class="goods-row__price">¥{{ formatPrice(g.price * g.num) }}</span>
      </div>
      <div class="total-row">
        应付总额：<b>¥{{ formatPrice(totalFee) }}</b>
      </div>
    </div>

    <div class="submit-row">
      <el-button type="primary" size="large" :loading="submitting" @click="submit">提交订单</el-button>
      <el-button size="large" @click="$router.back()">返回购物车</el-button>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { addressApi, orderApi, formatPrice } from '@/api'
import { useCartStore } from '@/stores/cart'

const router = useRouter()
const cartStore = useCartStore()
const submitting = ref(false)

const goods = computed(() => cartStore.checkedItems)
const totalFee = computed(() => cartStore.checkedTotal)

const addresses = ref([])
const selectedId = ref(null)
const addrMode = ref('book')

const form = reactive({
  receiver: '',
  phone: '',
  address: '',
  remark: ''
})

onMounted(async () => {
  addresses.value = await addressApi.list()
  const def = addresses.value.find((a) => a.isDefault === 1) || addresses.value[0]
  selectedId.value = def ? def.id : null
})

function fullAddress(a) {
  return `${a.province}${a.city}${a.district}${a.detail}`
}

async function submit() {
  let receiver, phone, address
  if (addrMode.value === 'book') {
    const addr = addresses.value.find((a) => a.id === selectedId.value)
    if (!addr) {
      ElMessage.warning('请选择收货地址')
      return
    }
    receiver = addr.receiver
    phone = addr.phone
    address = fullAddress(addr)
  } else {
    if (!form.receiver || !form.phone || !form.address) {
      ElMessage.warning('请填写完整的收货信息')
      return
    }
    receiver = form.receiver
    phone = form.phone
    address = form.address
  }
  submitting.value = true
  try {
    const order = await orderApi.create({
      receiver,
      phone,
      address,
      remark: form.remark,
      goods: goods.value.map((g) => ({ itemId: g.itemId, name: g.name, price: g.price, num: g.num, spec: g.spec, image: g.image }))
    })
    cartStore.clearChecked()
    ElMessage.success('下单成功，请尽快支付')
    router.push({ path: '/orders', query: { pay: order.id } })
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.block {
  background: #fff;
  border-radius: 16px;
  padding: 20px 24px;
  margin-bottom: 16px;
  box-shadow: var(--zg-card-shadow);
}
.block h3 {
  margin: 0 0 16px;
  font-size: 16px;
}
.addr-mode {
  margin-bottom: 16px;
}
.addr-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  max-width: 560px;
}
.addr-item {
  border: 1px solid var(--zg-border);
  border-radius: 12px;
  padding: 12px 16px;
  cursor: pointer;
  transition: all 0.2s;
}
.addr-item:hover {
  border-color: var(--zg-primary);
}
.addr-item--active {
  border-color: var(--zg-primary);
  background: var(--el-color-primary-light-9);
}
.addr-item__line {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
}
.addr-item__phone {
  color: var(--zg-text-light);
}
.addr-item__addr {
  margin-top: 6px;
  font-size: 13px;
  color: var(--zg-text);
}
.goods-row {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 0;
  border-bottom: 1px dashed #f0f0f0;
}
.goods-row__thumb {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  object-fit: cover;
  background: #f6faf9;
  flex-shrink: 0;
}
.goods-row__name {
  flex: 1;
  font-size: 14px;
  min-width: 0;
}
.goods-row__num {
  color: #999;
  width: 60px;
  text-align: center;
}
.goods-row__price {
  color: var(--zg-price);
  width: 100px;
  text-align: right;
  font-weight: 600;
}
.total-row {
  text-align: right;
  padding-top: 16px;
  font-size: 14px;
}
.total-row b {
  color: var(--zg-price);
  font-size: 22px;
}
.submit-row {
  text-align: right;
}
</style>

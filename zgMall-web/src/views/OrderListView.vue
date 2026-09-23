<template>
  <div>
    <h2 class="zg-page-title">我的订单</h2>

    <div v-for="o in orders" :key="o.id" class="order">
      <div class="order__head">
        <span>订单号：{{ o.id }}</span>
        <span>{{ o.createTime }}</span>
        <el-tag :type="statusOf(o).type" effect="light" size="small">{{ statusOf(o).text }}</el-tag>
      </div>
      <div class="order__body">
        <div v-for="g in o.goods" :key="g.itemId" class="order__goods">
          <img class="order__thumb" :src="g.image" :alt="g.name" />
          <span class="order__name">{{ g.name }}</span>
          <span class="order__num">x{{ g.num }}</span>
          <span class="order__price">¥{{ formatPrice(g.price * g.num) }}</span>
        </div>
      </div>
      <div class="order__foot">
        <span class="order__addr">📍 {{ o.receiver }} {{ o.phone }} ｜ {{ o.address }}</span>
        <span class="order__total">实付 <b>¥{{ formatPrice(o.totalFee) }}</b></span>
        <el-button v-if="o.status === 1" type="primary" size="small" @click="openPay(o)">立即支付</el-button>
      </div>
    </div>

    <el-empty v-if="loaded && orders.length === 0" description="还没有订单，去逛逛吧">
      <el-button type="primary" @click="$router.push('/items')">去逛逛</el-button>
    </el-empty>

    <el-dialog v-model="payVisible" title="模拟支付" width="360px" align-center>
      <div class="pay-box">
        <div class="pay-box__amount">¥{{ payOrder ? formatPrice(payOrder.totalFee) : '' }}</div>
        <p class="pay-box__tip">智购模拟支付（真实项目对接支付网关）</p>
        <el-radio-group v-model="payType">
          <el-radio value="balance">账户余额</el-radio>
          <el-radio value="mock">模拟扫码</el-radio>
        </el-radio-group>
      </div>
      <template #footer>
        <el-button @click="payVisible = false">取消</el-button>
        <el-button type="primary" :loading="paying" @click="doPay">确认支付</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { orderApi, formatPrice, ORDER_STATUS } from '@/api'

const route = useRoute()
const orders = ref([])
const loaded = ref(false)
const payVisible = ref(false)
const payOrder = ref(null)
const payType = ref('balance')
const paying = ref(false)

function statusOf(o) {
  return ORDER_STATUS[o.status] || { text: '未知', type: 'info' }
}

async function load() {
  orders.value = await orderApi.list()
  loaded.value = true
}

function openPay(o) {
  payOrder.value = o
  payVisible.value = true
}

async function doPay() {
  paying.value = true
  try {
    await orderApi.pay(payOrder.value.id, payType.value)
    ElMessage.success('支付成功')
    payVisible.value = false
    load()
  } finally {
    paying.value = false
  }
}

onMounted(async () => {
  await load()
  if (route.query.pay) {
    const order = orders.value.find((o) => o.id === Number(route.query.pay))
    if (order && order.status === 1) openPay(order)
  }
})
</script>

<style scoped>
.order {
  background: #fff;
  border-radius: 16px;
  margin-bottom: 16px;
  overflow: hidden;
  box-shadow: var(--zg-card-shadow);
}
.order__head {
  display: flex;
  align-items: center;
  gap: 24px;
  padding: 12px 20px;
  background: #f8fbfc;
  font-size: 13px;
  color: #64748b;
}
.order__body {
  padding: 8px 20px;
}
.order__goods {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 0;
}
.order__thumb {
  width: 44px;
  height: 44px;
  border-radius: 8px;
  object-fit: cover;
  background: #f6faf9;
  flex-shrink: 0;
}
.order__name {
  flex: 1;
  font-size: 14px;
  min-width: 0;
}
.order__num {
  color: #999;
  width: 60px;
  text-align: center;
}
.order__price {
  width: 100px;
  text-align: right;
  font-weight: 600;
}
.order__foot {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 12px 20px;
  border-top: 1px solid #f5f5f5;
  font-size: 13px;
}
.order__addr {
  color: #999;
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.order__total b {
  color: var(--zg-price);
  font-size: 16px;
  margin-left: 4px;
}
.pay-box {
  text-align: center;
}
.pay-box__amount {
  font-size: 32px;
  font-weight: 700;
  color: var(--zg-price);
}
.pay-box__tip {
  color: #999;
  font-size: 12px;
}
</style>

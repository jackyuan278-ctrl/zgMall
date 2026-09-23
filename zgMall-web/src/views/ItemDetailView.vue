<template>
  <div v-if="item" class="detail">
    <div class="detail__img">
      <img :src="item.image" :alt="item.name" />
    </div>
    <div class="detail__info">
      <h1 class="detail__name">{{ item.name }}</h1>
      <div class="detail__desc">{{ item.desc }}</div>
      <div class="detail__price-box">
        <span class="detail__label">智购价</span>
        <span class="detail__price">¥{{ formatPrice(item.price) }}</span>
      </div>
      <div class="detail__meta">
        <span>品牌：{{ item.brand }}</span>
        <span>分类：{{ item.categoryName }}</span>
        <span>销量：{{ item.sales }}</span>
        <span>库存：{{ item.stock }}</span>
      </div>
      <div class="detail__spec">规格：{{ item.spec }}</div>

      <div class="detail__buy">
        <el-input-number v-model="num" :min="1" :max="item.stock" />
        <el-button type="primary" plain size="large" @click="addToCart">🛒 加入购物车</el-button>
        <el-button type="primary" size="large" @click="buyNow">立即购买</el-button>
      </div>

      <div class="detail__ai">
        <el-button text type="primary" @click="$router.push('/ai')">
          ✨ 拿不准？让智购 AI 帮你判断这款适不适合你
        </el-button>
      </div>
    </div>
  </div>
  <el-skeleton v-else :rows="6" animated />
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { itemApi, formatPrice } from '@/api'
import { useCartStore } from '@/stores/cart'

const route = useRoute()
const router = useRouter()
const cartStore = useCartStore()
const item = ref(null)
const num = ref(1)

function addToCart() {
  cartStore.add(item.value, num.value)
  ElMessage.success('已加入购物车')
}

function buyNow() {
  cartStore.add(item.value, num.value)
  cartStore.list.forEach((it) => (it.checked = it.itemId === item.value.id))
  router.push('/order/confirm')
}

onMounted(async () => {
  item.value = await itemApi.detail(route.params.id)
})
</script>

<style scoped>
.detail {
  display: flex;
  gap: 40px;
  background: #fff;
  border-radius: 16px;
  padding: 32px;
  box-shadow: var(--zg-card-shadow);
}
.detail__img {
  width: 420px;
  height: 420px;
  border-radius: 12px;
  background: #f6faf9;
  overflow: hidden;
  flex-shrink: 0;
}
.detail__img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}
.detail__info {
  flex: 1;
  min-width: 0;
}
.detail__name {
  font-size: 22px;
  line-height: 1.5;
  margin: 0 0 8px;
}
.detail__desc {
  color: #888;
  font-size: 14px;
  margin-bottom: 16px;
}
.detail__price-box {
  background: #f0fdfa;
  border-radius: 12px;
  padding: 16px;
  display: flex;
  align-items: baseline;
  gap: 12px;
}
.detail__label {
  color: var(--zg-primary-dark);
  font-size: 14px;
}
.detail__price {
  color: var(--zg-price);
  font-size: 32px;
  font-weight: 700;
}
.detail__meta {
  display: flex;
  gap: 24px;
  margin: 16px 0;
  color: #666;
  font-size: 14px;
  flex-wrap: wrap;
}
.detail__spec {
  color: #666;
  font-size: 14px;
  margin-bottom: 24px;
}
.detail__buy {
  display: flex;
  align-items: center;
  gap: 16px;
}
.detail__ai {
  margin-top: 20px;
}
</style>

<template>
  <div class="zg-card" @click="$router.push(`/items/${item.id}`)">
    <div class="zg-card__img">
      <img :src="item.image" :alt="item.name" loading="lazy" />
    </div>
    <div class="zg-card__body">
      <div class="zg-card__name">{{ item.name }}</div>
      <div class="zg-card__spec">{{ item.spec }}</div>
      <div class="zg-card__foot">
        <span class="zg-card__price">¥{{ formatPrice(item.price) }}</span>
        <span class="zg-card__sales">已售{{ formatSales(item.sales) }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { formatPrice } from '@/api'

defineProps({
  item: { type: Object, required: true }
})

function formatSales(n) {
  return n >= 10000 ? (n / 10000).toFixed(1).replace(/\.0$/, '') + '万' : n
}
</script>

<style scoped>
.zg-card {
  background: #fff;
  border-radius: 14px;
  overflow: hidden;
  cursor: pointer;
  transition: box-shadow 0.2s, transform 0.2s;
}
.zg-card:hover {
  box-shadow: 0 8px 24px rgba(20, 184, 166, 0.12);
  transform: translateY(-3px);
}
.zg-card__img {
  height: 180px;
  background: #f6faf9;
  overflow: hidden;
}
.zg-card__img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
  transition: transform 0.35s;
}
.zg-card:hover .zg-card__img img {
  transform: scale(1.06);
}
.zg-card__body {
  padding: 12px 14px 14px;
}
.zg-card__name {
  font-size: 14px;
  line-height: 1.5;
  height: 42px;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}
.zg-card__spec {
  font-size: 12px;
  color: var(--zg-text-light);
  margin-top: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.zg-card__foot {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  margin-top: 8px;
}
.zg-card__price {
  color: var(--zg-price);
  font-size: 18px;
  font-weight: 700;
}
.zg-card__sales {
  font-size: 12px;
  color: var(--zg-text-light);
}
</style>

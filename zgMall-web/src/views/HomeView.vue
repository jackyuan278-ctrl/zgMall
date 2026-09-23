<template>
  <div>
    <!-- 顶部横幅 -->
    <div class="hero">
      <div class="hero__text">
        <h1>智购商城</h1>
        <p>AI 导购 · 智购优选 —— 说出需求，剩下的交给智购</p>
        <el-button type="primary" size="large" round @click="$router.push('/ai')">
          ✨ 体验 AI 导购
        </el-button>
      </div>
      <div class="hero__emoji">🛍️</div>
    </div>

    <!-- 分类导航 -->
    <div class="section">
      <h2 class="section__title">全部分类</h2>
      <div class="cate-list">
        <div
          v-for="c in categories"
          :key="c.id"
          class="cate-item"
          @click="$router.push({ path: '/items', query: { categoryId: c.id } })"
        >
          <span class="cate-item__emoji">{{ c.emoji }}</span>
          <span>{{ c.name }}</span>
        </div>
      </div>
    </div>

    <!-- 热卖推荐 -->
    <div class="section">
      <div class="section__head">
        <h2 class="section__title">热卖榜单</h2>
        <el-link type="primary" :underline="false" @click="$router.push('/items')">查看更多 ></el-link>
      </div>
      <div class="grid">
        <ZgProductCard v-for="it in hotItems" :key="it.id" :item="it" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { itemApi } from '@/api'
import ZgProductCard from '@/components/ZgProductCard.vue'

const categories = ref([])
const hotItems = ref([])

onMounted(async () => {
  categories.value = await itemApi.categories()
  const { list } = await itemApi.page({ sort: 'sales', pageSize: 8 })
  hotItems.value = list
})
</script>

<style scoped>
.hero {
  background: linear-gradient(120deg, #e6faf7 0%, #e0f2fe 100%);
  border-radius: 16px;
  padding: 40px 48px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.hero__text h1 {
  margin: 0 0 8px;
  font-size: 36px;
  color: #115e59;
}
.hero__text p {
  margin: 0 0 20px;
  color: #64748b;
}
.hero__emoji {
  font-size: 120px;
}
.section {
  margin-top: 28px;
}
.section__head {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.section__title {
  font-size: 20px;
  margin: 0 0 16px;
}
.cate-list {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 12px;
}
.cate-item {
  background: #fff;
  border-radius: 14px;
  padding: 20px 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s;
}
.cate-item:hover {
  box-shadow: 0 6px 20px rgba(20, 184, 166, 0.15);
  color: var(--zg-primary-dark);
  transform: translateY(-2px);
}
.cate-item__emoji {
  font-size: 32px;
}
.grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}
</style>

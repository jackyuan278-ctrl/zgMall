<template>
  <div>
    <div class="filter-bar">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item>全部商品</el-breadcrumb-item>
      </el-breadcrumb>

      <div class="filter-row">
        <el-radio-group v-model="categoryId" size="default" @change="reload">
          <el-radio-button :value="null">全部</el-radio-button>
          <el-radio-button v-for="c in categories" :key="c.id" :value="c.id">{{ c.name }}</el-radio-button>
        </el-radio-group>
        <el-select v-model="sort" placeholder="综合排序" clearable style="width: 140px" @change="reload">
          <el-option label="综合排序" value="" />
          <el-option label="销量优先" value="sales" />
          <el-option label="价格从低到高" value="priceAsc" />
          <el-option label="价格从高到低" value="priceDesc" />
        </el-select>
      </div>
    </div>

    <div v-loading="loading" class="grid">
      <ZgProductCard v-for="it in list" :key="it.id" :item="it" />
      <el-empty v-if="!loading && list.length === 0" description="没有找到相关商品" style="grid-column: 1 / -1" />
    </div>

    <el-pagination
      v-if="total > pageSize"
      v-model:current-page="page"
      :page-size="pageSize"
      :total="total"
      layout="prev, pager, next"
      class="pager"
      @current-change="load"
    />
  </div>
</template>

<script setup>
import { onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { itemApi } from '@/api'
import ZgProductCard from '@/components/ZgProductCard.vue'

const route = useRoute()
const categories = ref([])
const list = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = 12
const loading = ref(false)
const categoryId = ref(null)
const sort = ref('')

async function load() {
  loading.value = true
  try {
    const res = await itemApi.page({
      keyword: route.query.keyword || '',
      categoryId: categoryId.value,
      sort: sort.value,
      page: page.value,
      pageSize
    })
    list.value = res.list
    total.value = res.total
  } finally {
    loading.value = false
  }
}

function reload() {
  page.value = 1
  load()
}

watch(() => route.query, reload)

onMounted(async () => {
  categories.value = await itemApi.categories()
  if (route.query.categoryId) categoryId.value = Number(route.query.categoryId)
  load()
})
</script>

<style scoped>
.filter-bar {
  background: #fff;
  border-radius: 16px;
  padding: 16px 20px;
  margin-bottom: 16px;
  box-shadow: var(--zg-card-shadow);
}
.filter-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 14px;
  flex-wrap: wrap;
  gap: 12px;
}
.grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  min-height: 300px;
}
.pager {
  margin-top: 24px;
  justify-content: center;
}
</style>

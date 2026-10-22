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

    <div v-if="hasMore || page > 1" class="pager">
      <el-button :disabled="page <= 1 || loading" @click="previous">上一页</el-button>
      <template v-if="page <= MAX_OFFSET_PAGE">
        <el-button
          v-for="pageNumber in visiblePages"
          :key="pageNumber"
          :type="pageNumber === page ? 'primary' : 'default'"
          :disabled="loading"
          @click="goToPage(pageNumber)"
        >{{ pageNumber }}</el-button>
      </template>
      <span v-else>第 {{ page }} 页</span>
      <el-button :disabled="!hasMore || loading" @click="next">下一页</el-button>
    </div>
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
const MAX_OFFSET_PAGE = 5
const hasMore = ref(false)
const visiblePages = ref([1])
const cursorStack = ref([])
const loading = ref(false)
const categoryId = ref(null)
const sort = ref('')

async function load() {
  loading.value = true
  try {
    const res = await itemApi.page({
      keyword: String(route.query.keyword || ''),
      categoryId: categoryId.value,
      sort: sort.value,
      ...(page.value <= MAX_OFFSET_PAGE
        ? { page: page.value }
        : {
            cursorId: cursorStack.value[page.value - MAX_OFFSET_PAGE - 1]?.id,
            cursorSortValue: cursorStack.value[page.value - MAX_OFFSET_PAGE - 1]?.sortValue
          }),
      pageSize
    })
    list.value = res.list
    hasMore.value = page.value > MAX_OFFSET_PAGE
      ? Boolean(res.hasMore)
      : page.value * pageSize < (res.total || 0)
    if (page.value === MAX_OFFSET_PAGE && hasMore.value) {
      const lastItem = res.list?.[res.list.length - 1]
      cursorStack.value[0] = lastItem
        ? { id: lastItem.id, sortValue: cursorSortValue(lastItem) }
        : null
    } else if (page.value > MAX_OFFSET_PAGE && res.nextCursor != null) {
      cursorStack.value[page.value - MAX_OFFSET_PAGE] = {
        id: res.nextCursor,
        sortValue: res.nextCursorSortValue
      }
    }
    total.value = res.total ?? total.value
    if (page.value <= MAX_OFFSET_PAGE && res.total != null) {
      const pageCount = Math.min(MAX_OFFSET_PAGE, Math.ceil(res.total / pageSize))
      visiblePages.value = Array.from({ length: Math.max(1, pageCount) }, (_, index) => index + 1)
    }
  } finally {
    loading.value = false
  }
}

function cursorSortValue(item) {
  if (sort.value === 'sales') return item.sales
  if (sort.value === 'priceAsc' || sort.value === 'priceDesc') return item.price
  return null
}

function goToPage(pageNumber) {
  if (loading.value || pageNumber < 1 || pageNumber > MAX_OFFSET_PAGE || pageNumber === page.value) return
  page.value = pageNumber
  load()
}

function next() {
  if (!hasMore.value || loading.value) return
  page.value += 1
  load()
}

function previous() {
  if (page.value <= 1 || loading.value) return
  page.value -= 1
  load()
}

function reload() {
  page.value = 1
  cursorStack.value = []
  total.value = 0
  visiblePages.value = [1]
  load()
}

watch(() => route.query, (query) => {
  categoryId.value = query.categoryId ? Number(query.categoryId) : null
  reload()
})

onMounted(async () => {
  categories.value = await itemApi.categories()
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
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-top: 24px;
}
</style>

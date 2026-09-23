<template>
  <div class="zg-layout">
    <ZgHeader v-if="!isBarePage" />
    <main class="zg-main" :class="{ 'zg-main--bare': isBarePage }">
      <router-view />
    </main>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import ZgHeader from '@/components/ZgHeader.vue'

const route = useRoute()
const isBarePage = computed(() => route.meta.bare === true)
</script>

<style scoped>
.zg-layout {
  min-height: 100vh;
  background: var(--zg-bg);
}
.zg-main {
  max-width: 1200px;
  margin: 0 auto;
  padding: 16px 0 48px;
}
.zg-main--bare {
  max-width: none;
  margin: 0;
  padding: 0;
}
</style>

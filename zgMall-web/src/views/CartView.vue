<template>
  <div>
    <h2 class="zg-page-title">我的购物车</h2>
    <div v-if="cartStore.list.length" class="cart">
      <div class="cart__head">
        <el-checkbox
          :model-value="cartStore.allChecked"
          @change="cartStore.toggleAll($event)"
        >全选</el-checkbox>
        <span class="cart__col-goods">商品信息</span>
        <span class="cart__col">单价</span>
        <span class="cart__col">数量</span>
        <span class="cart__col">小计</span>
        <span class="cart__col">操作</span>
      </div>
      <div v-for="it in cartStore.list" :key="it.itemId" class="cart__row">
        <el-checkbox v-model="it.checked" @change="cartStore.persist()" />
        <div class="cart__goods" @click="$router.push(`/items/${it.itemId}`)">
          <img class="cart__thumb" :src="it.image" :alt="it.name" />
          <div>
            <div class="cart__name">{{ it.name }}</div>
            <div class="cart__spec">{{ it.spec }}</div>
          </div>
        </div>
        <span class="cart__col price">¥{{ formatPrice(it.price) }}</span>
        <div class="cart__col">
          <el-input-number
            :model-value="it.num"
            :min="1"
            size="small"
            @change="(v) => cartStore.updateNum(it.itemId, v)"
          />
        </div>
        <span class="cart__col price">¥{{ formatPrice(it.price * it.num) }}</span>
        <div class="cart__col">
          <el-button text type="danger" @click="cartStore.remove(it.itemId)">删除</el-button>
        </div>
      </div>

      <div class="cart__foot">
        <span class="cart__total">
          已选 <b>{{ cartStore.checkedItems.length }}</b> 件商品，合计：
          <b class="cart__total-price">¥{{ formatPrice(cartStore.checkedTotal) }}</b>
        </span>
        <el-button type="primary" size="large" :disabled="cartStore.checkedItems.length === 0" @click="goCheckout">
          去结算
        </el-button>
      </div>
    </div>
    <el-empty v-else description="购物车还是空的，去逛逛吧">
      <el-button type="primary" @click="$router.push('/items')">去逛逛</el-button>
    </el-empty>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { formatPrice } from '@/api'
import { useCartStore } from '@/stores/cart'

const router = useRouter()
const cartStore = useCartStore()

// 真实模式进页拉取后端购物车；mock 模式走 localStorage，load 内部不操作
onMounted(() => cartStore.load())

function goCheckout() {
  if (cartStore.checkedItems.length === 0) {
    ElMessage.warning('请先勾选商品')
    return
  }
  router.push('/order/confirm')
}
</script>

<style scoped>
.cart {
  background: #fff;
  border-radius: 16px;
  padding: 16px 24px;
  box-shadow: var(--zg-card-shadow);
}
.cart__head,
.cart__row {
  display: grid;
  grid-template-columns: 40px 1fr 110px 130px 110px 90px;
  align-items: center;
  gap: 8px;
}
.cart__head {
  color: #999;
  font-size: 13px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}
.cart__row {
  padding: 16px 0;
  border-bottom: 1px solid #f7f7f7;
}
.cart__col {
  text-align: center;
  font-size: 14px;
}
.cart__col.price {
  color: var(--zg-price);
  font-weight: 600;
}
.cart__goods {
  display: flex;
  gap: 12px;
  align-items: center;
  cursor: pointer;
  min-width: 0;
}
.cart__thumb {
  width: 64px;
  height: 64px;
  border-radius: 8px;
  object-fit: cover;
  background: #f6faf9;
  flex-shrink: 0;
}
.cart__name {
  font-size: 14px;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.cart__spec {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}
.cart__foot {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 24px;
  padding: 20px 0 8px;
}
.cart__total {
  font-size: 14px;
}
.cart__total-price {
  color: var(--zg-price);
  font-size: 22px;
}
</style>

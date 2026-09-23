import { defineStore } from 'pinia'
import { cartApi, USE_MOCK } from '@/api'

const CART_KEY = 'zg_mock_cart'

export const useCartStore = defineStore('cart', {
  state: () => ({
    // [{itemId, name, price, image, spec, num, checked}]
    list: JSON.parse(localStorage.getItem(CART_KEY) || '[]')
  }),
  getters: {
    totalNum: (state) => state.list.reduce((sum, it) => sum + it.num, 0),
    checkedItems: (state) => state.list.filter((it) => it.checked),
    checkedTotal: (state) => state.checkedItems.reduce((sum, it) => sum + it.price * it.num, 0),
    allChecked: (state) => state.list.length > 0 && state.list.every((it) => it.checked)
  },
  actions: {
    persist() {
      if (USE_MOCK) localStorage.setItem(CART_KEY, JSON.stringify(this.list))
    },
    // 真实模式从后端拉取（checked 是前端本地状态，拉回后默认勾选）
    async load() {
      if (!USE_MOCK) {
        const list = await cartApi.list()
        this.list = list.map((it) => ({ ...it, checked: true }))
      }
    },
    async add(item, num = 1) {
      if (USE_MOCK) {
        const exist = this.list.find((it) => it.itemId === item.id)
        if (exist) {
          exist.num += num
        } else {
          this.list.push({
            itemId: item.id,
            name: item.name,
            price: item.price,
            image: item.image,
            spec: item.spec,
            num,
            checked: true
          })
        }
        this.persist()
      } else {
        await cartApi.add({ itemId: item.id, num })
        await this.load()
      }
    },
    async updateNum(itemId, num) {
      if (USE_MOCK) {
        const it = this.list.find((x) => x.itemId === itemId)
        if (it && num > 0) it.num = num
        this.persist()
      } else {
        await cartApi.updateNum(itemId, num)
        await this.load()
      }
    },
    async remove(itemId) {
      if (USE_MOCK) {
        this.list = this.list.filter((x) => x.itemId !== itemId)
        this.persist()
      } else {
        await cartApi.remove(itemId)
        await this.load()
      }
    },
    toggleAll(checked) {
      this.list.forEach((it) => (it.checked = checked))
      this.persist()
    },
    clearChecked() {
      this.list = this.list.filter((it) => !it.checked)
      this.persist()
    }
  }
})

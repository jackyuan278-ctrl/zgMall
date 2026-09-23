<template>
  <div class="chat">
    <div class="chat__head">
      <span class="chat__head-icon">✨</span>
      <div>
        <h2>智购 AI 导购</h2>
        <p>说出你的预算和需求，我来帮你挑</p>
      </div>
    </div>

    <div ref="listRef" class="chat__list">
      <div class="msg msg--ai">
        <div class="msg__avatar">智</div>
        <div class="msg__bubble">
          你好！我是智购助手，可以帮你：<br />
          ① 按预算选商品（如「5000以内买手机」）<br />
          ② 按场景推荐（如「宿舍用的静音键盘」）<br />
          ③ 对比商品、解答参数含义<br />
          直接说吧～
        </div>
      </div>

      <div v-for="(m, idx) in messages" :key="idx" class="msg" :class="m.role === 'user' ? 'msg--user' : 'msg--ai'">
        <div v-if="m.role === 'ai'" class="msg__avatar">智</div>
        <div class="msg__content">
          <div class="msg__bubble" :class="{ 'msg__bubble--streaming': m.streaming }">
            <span class="msg__text">{{ m.content }}</span><span v-if="m.streaming" class="msg__cursor">|</span>
          </div>
          <div v-if="m.items && m.items.length" class="msg__items">
            <div v-for="it in m.items" :key="it.id" class="mini-card" @click="$router.push(`/items/${it.id}`)">
              <img class="mini-card__img" :src="it.image" :alt="it.name" />
              <div class="mini-card__body">
                <div class="mini-card__name">{{ it.name }}</div>
                <div class="mini-card__price">¥{{ formatPrice(it.price) }}</div>
              </div>
            </div>
          </div>
        </div>
        <div v-if="m.role === 'user'" class="msg__avatar msg__avatar--user">我</div>
      </div>
    </div>

    <div v-if="messages.length === 0 && !streaming" class="chat__quick">
      <span v-for="q in quickQuestions" :key="q" class="chat__quick-item" @click="send(q)">{{ q }}</span>
    </div>

    <div class="chat__input">
      <el-input
        v-model="draft"
        type="textarea"
        :rows="2"
        resize="none"
        placeholder="例如：预算3000左右，想要一台打游戏不错的笔记本"
        @keydown.enter.exact.prevent="send()"
      />
      <el-button type="primary" class="chat__send" :loading="streaming" @click="send()">
        {{ streaming ? '思考中…' : '发送' }}
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { nextTick, onBeforeUnmount, ref } from 'vue'
import { chatStream } from '@/api/ai'
import { formatPrice } from '@/api'

const quickQuestions = [
  '5000以内买手机',
  '预算8000打游戏笔记本',
  '宿舍用的蓝牙耳机',
  '夏天开的省电空调',
  '送女友的礼物'
]

const sessionId = ref('s-' + Date.now())
const draft = ref('')
const messages = ref([])
const streaming = ref(false)
const listRef = ref(null)
let cancelFn = null

function scrollBottom() {
  nextTick(() => {
    if (listRef.value) listRef.value.scrollTop = listRef.value.scrollHeight
  })
}

function send(text) {
  const content = (text ?? draft.value).trim()
  if (!content || streaming.value) return
  draft.value = ''
  messages.value.push({ role: 'user', content })
  const aiMsg = { role: 'ai', content: '', items: [], streaming: true }
  messages.value.push(aiMsg)
  streaming.value = true
  scrollBottom()
  cancelFn = chatStream(content, sessionId.value, {
    onToken(token) {
      aiMsg.content += token
      scrollBottom()
    },
    onItems(items) {
      aiMsg.items = items
      scrollBottom()
    },
    onDone() {
      aiMsg.streaming = false
      streaming.value = false
    },
    onError() {
      aiMsg.content = aiMsg.content || '（连接中断，请稍后重试）'
      aiMsg.streaming = false
      streaming.value = false
    }
  })
}

onBeforeUnmount(() => {
  if (cancelFn) cancelFn()
})
</script>

<style scoped>
.chat {
  background: #fff;
  border-radius: 16px;
  display: flex;
  flex-direction: column;
  height: calc(100vh - 128px);
  overflow: hidden;
  box-shadow: var(--zg-card-shadow);
}
.chat__head {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 24px;
  background: linear-gradient(120deg, var(--zg-primary), var(--zg-accent));
  color: #fff;
}
.chat__head-icon {
  font-size: 32px;
}
.chat__head h2 {
  margin: 0;
  font-size: 18px;
  color: #fff;
}
.chat__head p {
  margin: 2px 0 0;
  font-size: 12px;
  opacity: 0.9;
}
.chat__list {
  flex: 1;
  overflow-y: auto;
  padding: 20px 24px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.msg {
  display: flex;
  gap: 10px;
  align-items: flex-start;
}
.msg--user {
  justify-content: flex-end;
}
.msg__avatar {
  width: 36px;
  height: 36px;
  border-radius: 12px;
  background: linear-gradient(135deg, var(--zg-primary), var(--zg-accent));
  color: #fff;
  font-size: 16px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.msg__avatar--user {
  background: var(--zg-primary);
}
.msg__content {
  max-width: 72%;
}
.msg__bubble {
  background: #f4f7f9;
  border-radius: 14px;
  padding: 12px 16px;
  font-size: 14px;
  line-height: 1.7;
  white-space: pre-wrap;
  word-break: break-word;
}
.msg--user .msg__bubble {
  background: var(--zg-primary);
  color: #fff;
}
.msg__cursor {
  animation: blink 0.8s infinite;
  font-weight: 700;
}
@keyframes blink {
  50% { opacity: 0; }
}
.msg__items {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 10px;
}
.mini-card {
  display: flex;
  gap: 10px;
  border: 1px solid var(--zg-border);
  border-radius: 12px;
  padding: 10px;
  width: 280px;
  cursor: pointer;
  transition: all 0.2s;
  background: #fff;
}
.mini-card:hover {
  box-shadow: 0 6px 18px rgba(20, 184, 166, 0.15);
  border-color: var(--zg-primary);
  transform: translateY(-2px);
}
.mini-card__img {
  width: 52px;
  height: 52px;
  border-radius: 10px;
  object-fit: cover;
  background: #f6faf9;
  flex-shrink: 0;
}
.mini-card__body {
  min-width: 0;
}
.mini-card__name {
  font-size: 12px;
  line-height: 1.4;
  height: 34px;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}
.mini-card__price {
  color: var(--zg-price);
  font-weight: 700;
  font-size: 14px;
  margin-top: 4px;
}
.chat__quick {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  padding: 0 24px 12px;
}
.chat__quick-item {
  border: 1px solid var(--el-color-primary-light-7);
  color: var(--zg-primary-dark);
  border-radius: 18px;
  padding: 6px 14px;
  font-size: 13px;
  cursor: pointer;
  background: var(--el-color-primary-light-9);
  transition: all 0.2s;
}
.chat__quick-item:hover {
  background: var(--el-color-primary-light-8);
}
.chat__input {
  border-top: 1px solid var(--zg-border);
  padding: 12px 16px;
  display: flex;
  gap: 12px;
  align-items: flex-end;
}
.chat__send {
  height: 54px;
  background: linear-gradient(135deg, var(--zg-primary), var(--zg-accent));
  border: none;
}
</style>

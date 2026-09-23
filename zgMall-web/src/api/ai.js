import { USE_MOCK, itemApi } from './index'

// 智购 AI 导购：SSE 流式对话
// 后端接口约定：GET /api/ai/chat/stream?message=xxx&sessionId=xxx
// 事件格式：data: 文本片段（逐 token）；event: items + data: [商品JSON]（推荐卡片）；data: [DONE] 结束
export function chatStream(message, sessionId, { onToken, onItems, onDone, onError }) {
  if (USE_MOCK) {
    return mockStream(message, { onToken, onItems, onDone })
  }
  const token = localStorage.getItem('zg_token')
  const es = new EventSource(`/api/ai/chat/stream?message=${encodeURIComponent(message)}&sessionId=${sessionId}&authorization=${encodeURIComponent(token || '')}`)
  let finished = false
  es.onmessage = (e) => {
    if (e.data === '[DONE]') {
      finished = true
      es.close()
      onDone && onDone()
      return
    }
    onToken && onToken(e.data)
  }
  es.addEventListener('items', (e) => {
    try {
      onItems && onItems(JSON.parse(e.data))
    } catch (err) { /* 忽略非法 JSON */ }
  })
  es.onerror = () => {
    if (!finished) {
      es.close()
      onError && onError(new Error('连接中断'))
    }
  }
  return () => es.close()
}

// ---------- mock：本地规则引擎模拟智购导购 ----------
const KEYWORD_CATEGORY = {
  '手机': '手机数码', '平板': '手机数码', '耳机': '手机数码', '手环': '手机数码', '相机': '手机数码',
  '电脑': '电脑办公', '笔记本': '电脑办公', '鼠标': '电脑办公',
  '空调': '家用电器', '洗衣机': '家用电器', '净化器': '家用电器',
  '衣服': '服饰鞋包', '外套': '服饰鞋包', '鞋': '服饰鞋包', '跑鞋': '服饰鞋包',
  '坚果': '食品生鲜', '零食': '食品生鲜', '枕头': '食品生鲜',
  '运动': '运动户外', '健身': '运动户外'
}

function pickReply(message, allItems) {
  const budgetMatch = message.match(/(\d+)\s*(元|块|千|w|W|万)?/)
  let budget = null
  if (budgetMatch && (message.includes('预算') || /以内|以下|左右|元/.test(message))) {
    let val = Number(budgetMatch[1])
    if (budgetMatch[2] === '千') val *= 1000
    if (budgetMatch[2] === 'w' || budgetMatch[2] === 'W' || budgetMatch[2] === '万') val *= 10000
    budget = val
  }
  let pool = [...allItems]
  let reason = '综合销量和好评'
  const hitCategory = Object.keys(KEYWORD_CATEGORY).find((kw) => message.includes(kw))
  if (hitCategory) {
    pool = pool.filter((it) => it.categoryName === KEYWORD_CATEGORY[hitCategory])
    reason = `您提到了「${hitCategory}」相关需求`
  }
  if (budget) {
    pool = pool.filter((it) => it.price <= budget * 100)
    reason = `${reason}，且预算在 ${budget} 元以内`
  }
  pool.sort((a, b) => b.sales - a.sales)
  const picked = pool.slice(0, 3)
  if (picked.length === 0) {
    return {
      text: '抱歉，暂时没有找到完全匹配的商品。可以换个说法，比如告诉我预算和用途？',
      items: []
    }
  }
  const intro = `根据您的需求（${reason}），我从商品库里挑了这几款推荐给您：\n\n`
  const body = picked
    .map((it, i) => `${i + 1}. ${it.name}\n   ¥${(it.price / 100).toFixed(2)}｜${it.spec}｜已售 ${it.sales}`)
    .join('\n')
  return { text: intro + body + '\n\n点下方卡片可以直接查看详情，需要对比或换推荐随时告诉我。', items: picked }
}

function mockStream(message, { onToken, onItems, onDone }) {
  let cancelled = false
  itemApi.page({ pageSize: 100 }).then(({ list }) => {
    const { text, items: picked } = pickReply(message, list)
    let i = 0
    const timer = setInterval(() => {
      if (cancelled) { clearInterval(timer); return }
      if (i >= text.length) {
        clearInterval(timer)
        if (picked.length) onItems && onItems(picked)
        onDone && onDone()
        return
      }
      const step = 2 + Math.floor(Math.random() * 4)
      onToken && onToken(text.slice(i, i + step))
      i += step
    }, 40)
  })
  return () => { cancelled = true }
}

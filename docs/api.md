# 智购商城（zgmall）API 文档

- 版本：v0.1
- 日期：2026-09-22
- 文档口径：接口路径**以网关路由（zg-gateway/application.yml）+ 前端契约（zgmall-web/src/api）+ zg-api Feign 契约为准**；统一响应/错误码**对齐 zg-common 现有类**，不自造。代码与文档冲突时以代码为准并标注。
- 状态图例：✅ 已实现 / 🔶 骨架已铺（契约或常量已定） / ⬜ 待实现（**Service 层待用户实现**） / 📋 规划（契约未定，本表为设计提案）

---

## 1. 通用约定

### 1.1 Base URL

- 统一入口（网关）：`http://localhost:8080`，所有业务接口带 `/api` 前缀。
- 各服务直连（调试用，绕过网关鉴权）：user `:8081`、item `:8082`、cart `:8083`、trade `:8084`、pay `:8085`、ai `:8086`，路径为**去掉 `/api` 前缀后**的部分（网关 StripPrefix=1 已剥掉）。

### 1.2 统一响应结构（对齐 `zg-common/Result.java`）

```json
{ "code": 200, "msg": "success", "data": {} }
```

| 字段 | 类型 | 说明 |
|---|---|---|
| code | Integer | 200 成功；其余为错误码（见 1.5） |
| msg | String | 提示信息 |
| data | T | 业务数据，可为 null |

前端约定（`request.js`）：`code === 200` 时取 `data` 作为返回值；非 200 弹 `msg` 并 reject。

### 1.3 鉴权

- 方式：JWT（jjwt 0.12.6），登录接口签发，TTL 30 分钟（`zg.jwt.ttl: 1800000`）。请求头携带 token。
- ⚠️ **现状差异**：前端 `request.js` 发的 `authorization` 头是**纯 token，没有 `Bearer ` 前缀**（`config.headers.authorization = token`），网关 `AuthGlobalFilter` 直接 `parseToken(header)`。文档示例按任务要求写 `Bearer <token>`，但**当前代码以纯 token 为准**。建议统一方案：`AuthGlobalFilter` 解析前剥掉可选的 `Bearer ` 前缀（`startsWith("Bearer ")` 则截断），前端两种写法都能兼容。
- **网关白名单**（`AuthGlobalFilter.isExclude`，以下请求不校验 JWT）：

| 路径 | 方法 |
|---|---|
| `/api/users/login`、`/api/users/register` | 任意 |
| `/api/items/**` | 仅 GET |
| `/api/health/**`、`/health/**` | 任意 |

- 其余全部接口**必须携带有效 token**，否则网关直接返回 **HTTP 401** + `{"code":401,"msg":"未登录"}`（注意：这是网关层响应，**不包 Result 结构**；前端 `request.js` 已按 HTTP 401 处理跳登录）。
- **身份透传机制**：网关验签通过后，把 userId 写入 `user-info` 请求头转发给下游；各服务 `UserInfoInterceptor` 读该头存入 `UserContext`（ThreadLocal）。**下游业务取 userId 一律走 `UserContext.getUser()`，绝不信任前端传的用户标识**（横向越权防线）。

### 1.4 分页结构（前端契约）

```json
{ "list": [ ... ], "total": 128 }
```

- 请求参数：`page`（默认 1）、`pageSize`（默认 10）。
- ⚠️ zg-common 目前**没有 PageDTO 类**，骨架阶段需补一个通用分页包装类（或各服务自建），响应字段名 `list/total` 不能变（前端已按此消费）。

### 1.5 错误码约定（对齐 `GlobalExceptionHandler` + `BizException`）

| code | 语义 | 产生方式 |
|---|---|---|
| 200 | 成功 | `Result.success()` |
| 400 | 参数校验失败 | `MethodArgumentNotValidException` 处理（取第一个字段错误信息） |
| 401 | 未登录 / token 失效 | 网关（HTTP 401，非 Result 包裹） |
| 404 | 资源不存在（约定用法） | `throw new BizException(404, "商品不存在")` |
| 500 | 系统异常 | 兜底 `Exception` 处理 / `BizException("msg")` 默认 500 |
| 自定义 | 业务错误 | `throw new BizException(code, msg)`，建议业务码段用 4xx/5xx 区分 |

⚠️ 注意：业务异常经 `GlobalExceptionHandler` 返回时 **HTTP 状态码仍是 200**（只改 body 的 code），前端按 `res.code` 判断。若某天前端要按 HTTP 状态码处理，需给 handler 加 `@ResponseStatus` 或返回 `ResponseEntity`——当前**不要**混用两套判断。

### 1.6 数据格式

- 金额：**分**（Integer）。
- 时间：`yyyy-MM-dd HH:mm:ss`（`JacksonConfig` 统一序列化 LocalDateTime）。
- ID：雪花 Long。

---

## 2. 网关路由表（`zg-gateway/application.yml`）

| 路由 id | 匹配路径 | 转发服务 | 剥前缀 | 免鉴权 |
|---|---|---|---|---|
| user-service | `/api/users/**` | lb://user-service | 1 | 仅 login/register |
| item-service | `/api/items/**` | lb://item-service | 1 | 仅 GET |
| cart-service | `/api/cart/**` | lb://cart-service | 1 | 否 |
| trade-service | `/api/orders/**` | lb://trade-service | 1 | 否 |
| pay-service | `/api/pay/**` | lb://pay-service | 1 | 否 |
| ai-service | `/api/ai/**` | lb://ai-service | 1 | 否 |

---

## 3. 接口清单（按服务）

### 3.0 健康检查 ✅

| 项 | 内容 |
|---|---|
| 路径 | `GET /health`（各服务直连，如 `http://localhost:8081/health`，不经过网关） |
| 鉴权 | 无（网关白名单也放了 `/health/**`，防误拦） |
| 响应 | `{"code":200,"msg":"success","data":"ok"}` |
| 状态 | ✅ 已实现（`zg-common/HealthController`） |

---

### 3.1 用户服务（user-service :8081，库 zg_user）

#### 3.1.1 注册 ⬜

| 方法 | 路径 | 鉴权 |
|---|---|---|
| POST | `/api/users/register` | 免 |

请求体（前端 `LoginView` 契约）：

```json
{ "username": "newbie", "phone": "13700001111", "password": "abc123456" }
```

响应：

```json
{ "code": 200, "msg": "success", "data": true }
```

错误示例：用户名已存在 → `{"code":400,"msg":"用户名已存在","data":null}`（code 可自定义，建议 400）。

规则：密码 BCrypt 加密入库；`uk_username` 唯一键兜底并发。**Service 待用户实现**。

#### 3.1.2 登录 ⬜（网关白名单 ✅、JwtTool ✅）

| 方法 | 路径 | 鉴权 |
|---|---|---|
| POST | `/api/users/login` | 免 |

请求体：

```json
{ "username": "demo", "password": "123456" }
```

响应（**字段以前端 user store 消费为准**）：

```json
{
  "code": 200,
  "msg": "success",
  "data": { "token": "<jwt>", "userId": 1, "username": "demo" }
}
```

错误示例：`{"code":400,"msg":"用户名或密码错误","data":null}`（用户名不存在与密码错误同文案）；禁用用户：`{"code":400,"msg":"账号已禁用","data":null}`。

#### 3.1.3 修改密码 ⬜（前端页面 ✅）

| 方法 | 路径 | 鉴权 |
|---|---|---|
| PUT | `/api/users/password` | 是（`Authorization: Bearer <token>`） |

请求体（前端 `ChangePasswordView` 契约）：

```json
{ "oldPassword": "123456", "newPassword": "abc123456" }
```

响应：`{"code":200,"msg":"success","data":true}`；原密码错误 → `{"code":400,"msg":"原密码错误","data":null}`。

规则：新旧密码不能相同（后端必须复验）；userId 取 `UserContext`。**Service 待用户实现**。

#### 3.1.4 个人信息 📋（规划）

| 方法 | 路径 | 鉴权 | 说明 |
|---|---|---|---|
| GET | `/api/users/me` | 是 | 查询 `{id, username, nickname, phone, avatar, balance}` |
| PUT | `/api/users/me` | 是 | 更新 nickname/phone/avatar |

前端无对应页面，契约为提案。**待用户拍板后实现**。

#### 3.1.5 收货地址 📋（规划，表已建）

| 方法 | 路径 | 鉴权 | 说明 |
|---|---|---|---|
| GET | `/api/users/addresses` | 是 | 我的地址列表（按 is_default 降序、create_time 倒序） |
| POST | `/api/users/addresses` | 是 | 新增（body：receiver/phone/province/city/district/detail/isDefault） |
| PUT | `/api/users/addresses/{id}` | 是 | 修改（仅本人） |
| DELETE | `/api/users/addresses/{id}` | 是 | 删除（仅本人） |
| PUT | `/api/users/addresses/{id}/default` | 是 | 设为默认（同用户其余置 0） |

前端结算页目前手填地址，契约为提案。

---

### 3.2 商品服务（item-service :8082，库 zg_item）

#### 3.2.1 分类列表 ⬜

| 方法 | 路径 | 鉴权 |
|---|---|---|
| GET | `/api/items/categories` | 免（GET items 白名单） |

响应：

```json
{ "code": 200, "msg": "success", "data": [ { "id": 1, "name": "手机数码" }, { "id": 2, "name": "电脑办公" } ] }
```

按 `sort` 升序。**Service 待用户实现**。

#### 3.2.2 商品分页列表（含搜索/排序） ⬜

| 方法 | 路径 | 鉴权 |
|---|---|---|
| GET | `/api/items` | 免 |

请求参数：

| 参数 | 必填 | 说明 |
|---|---|---|
| keyword | 否 | 搜索词（匹配名称/品牌/分类名，MySQL LIKE；语义搜索走 AI 导购 Milvus，ES 不上） |
| categoryId | 否 | 分类筛选 |
| sort | 否 | `''` 综合 / `sales` 销量 / `priceAsc` / `priceDesc` |
| page / pageSize | 否 | 默认 1 / 10（前端列表页传 12） |

响应（分页结构见 1.4）：

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "total": 16,
    "list": [
      {
        "id": 1001,
        "name": "小米14 Pro 5G手机 骁龙8Gen3 徕卡光学镜头 16GB+512GB 黑色",
        "price": 499900,
        "stock": 128,
        "image": "/images/p1001.jpg",
        "categoryId": 1,
        "categoryName": "手机数码",
        "brand": "小米",
        "spec": "16GB+512GB / 骁龙8Gen3 / 6.73英寸2K屏",
        "sales": 2341,
        "desc": "第二代骁龙8移动平台，徕卡可变光圈主摄，2K全等深微曲屏，120W秒充。"
      }
    ]
  }
}
```

⚠️ **字段映射坑**：前端消费 `categoryName`（分类名）、`brand`（品牌名）、`desc`（描述），DB 对应 `category_id`、`brand_id`、`description`。后端查询需 join 并映射成前端字段名。只返回 `status=1` 商品。**Service 待用户实现**。

#### 3.2.3 商品详情 ⬜

| 方法 | 路径 | 鉴权 |
|---|---|---|
| GET | `/api/items/{id}` | 免 |

响应：单对象，字段同上（含 `status`）。不存在 → `{"code":404,"msg":"商品不存在","data":null}`。下架商品（status=0）→ 提示"商品已下架"。

该路径同时是 `ItemClient.queryItemById` 的 Feign 契约（见 4）。

#### 3.2.4 库存预扣 🔶（Feign 契约已定，内部接口）

| 方法 | 路径 | 鉴权 |
|---|---|---|
| PUT | `/api/items/stock/deduct` | **内部接口**：Feign 直连 `item-service:8082` 调用；经网关会被 401 拦（白名单只放行 GET items），这也防止了外部直调 |

请求体（`OrderDetailDTO` 列表）：

```json
[ { "itemId": 1001, "num": 1 }, { "itemId": 1006, "num": 2 } ]
```

响应：`{"code":200,"msg":"success","data":null}`；库存不足 → `{"code":400,"msg":"商品[xxx]库存不足","data":null}`。

实现要点（面试深挖）：条件更新 `UPDATE tb_item SET stock = stock - ? WHERE id = ? AND stock >= ?`，影响行数 0 即失败——原子防超卖，勿先查后改。

#### 3.2.5 库存回滚 📋（审查发现的缺口，建议补）

`ItemClient` 目前只有 `deductStock`，超时关单要加回库存，建议补：

| 方法 | 路径 | 鉴权 |
|---|---|---|
| PUT | `/api/items/stock/restore` | 内部接口（同上） |

请求体同 3.2.4（`[{itemId, num}]`）。**待用户实现时一并补上**。

---

### 3.3 购物车服务（cart-service :8083，纯 Redis） 📋

⚠️ 前端 `src/api/index.js` **未定义 cart 真实接口**（mock 走 localStorage），以下为**设计提案**，实现前与前端对齐后落地。存储方案已定：Redis Hash，key `zg:cart:{userId}`、field `itemId`、value 快照 JSON `{itemId,name,price,image,spec,num}`（Redis db0 已配）。

| 方法 | 路径 | 鉴权 | 说明 |
|---|---|---|---|
| GET | `/api/cart` | 是 | 我的购物车列表（快照数组） |
| POST | `/api/cart` | 是 | 加购 body `{"itemId":1001,"num":1}`；已存在则 num 累加 |
| PUT | `/api/cart/{itemId}` | 是 | 改数量 body `{"num":3}` |
| DELETE | `/api/cart/{itemId}` | 是 | 移除单项 |
| DELETE | `/api/cart` | 是 | 清空 |

响应示例（GET）：`{"code":200,"msg":"success","data":[{"itemId":1001,"name":"小米14 Pro...","price":499900,"image":"/images/p1001.jpg","spec":"16GB+512GB...","num":1}]}`

规则：userId 取 `UserContext`；数量上限 99；快照价仅展示，结算以 item 实时价为准。

---

### 3.4 交易服务（trade-service :8084，库 zg_trade）

#### 3.4.1 创建订单 ⬜（前端契约 ✅）

| 方法 | 路径 | 鉴权 |
|---|---|---|
| POST | `/api/orders` | 是 |

请求体（前端 `OrderConfirmView` 契约）：

```json
{
  "receiver": "张同学",
  "phone": "13800138000",
  "address": "江西省南昌市红谷滩区 学府大道999号",
  "remark": "尽快发货",
  "goods": [
    { "itemId": 1001, "name": "小米14 Pro 5G手机 骁龙8Gen3 徕卡光学镜头 16GB+512GB 黑色", "price": 499900, "num": 1, "spec": "16GB+512GB / 骁龙8Gen3 / 6.73英寸2K屏", "image": "/images/p1001.jpg" }
  ]
}
```

响应（前端跳转支付依赖 `id`，列表页依赖其余字段）：

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": 10003,
    "totalFee": 499900,
    "status": 1,
    "createTime": "2026-09-22 21:30:00",
    "receiver": "张同学",
    "phone": "13800138000",
    "address": "江西省南昌市红谷滩区 学府大道999号",
    "goods": [
      { "itemId": 1001, "name": "小米14 Pro 5G手机 骁龙8Gen3 徕卡光学镜头 16GB+512GB 黑色", "price": 499900, "num": 1, "image": "/images/p1001.jpg", "spec": "16GB+512GB / 骁龙8Gen3 / 6.73英寸2K屏" }
    ]
  }
}
```

规则（实现思路）：实时价重算 totalFee（不采信前端 price）→ Feign 预扣库存 → 本地事务写订单+明细（快照）→ 发延迟消息（见 5）。⚠️ DB 无 `remark` 列，需拍板加列或砍掉。**Service 待用户实现**。

#### 3.4.2 订单列表 ⬜

| 方法 | 路径 | 鉴权 |
|---|---|---|
| GET | `/api/orders` | 是 |

响应：`{"code":200,"msg":"success","data":[ {订单对象...}, ... ]}`，数组元素结构同 3.4.1 响应的 data（含 `goods`），按 `create_time` 倒序，仅本人订单。一期不分页（前端无分页 UI）。

#### 3.4.3 订单详情 📋（规划）

| 方法 | 路径 | 鉴权 |
|---|---|---|
| GET | `/api/orders/{id}` | 是 |

前端暂未使用，契约提案：单订单对象，非本人订单返回 404。

---

### 3.5 支付服务（pay-service :8085，库 zg_pay）

#### 3.5.1 模拟支付 ⬜（前端弹窗 ✅）

| 方法 | 路径 | 鉴权 |
|---|---|---|
| POST | `/api/pay/orders/{orderId}/pay` | 是 |

请求体：⚠️ 前端弹窗有"账户余额/模拟扫码"选择（`balance`/`mock`），但 `orderApi.pay` **当前没传支付方式**。需补 body：

```json
{ "payType": "balance" }
```

（`payType` 取值：`balance` 余额 / `mock` 模拟扫码；对应 `tb_order.payment_type` 1/2。**前端与后端需同步补上**。）

响应：`{"code":200,"msg":"success","data":null}`。

错误示例：
- 订单不存在/已关闭：`{"code":400,"msg":"订单不存在或已关闭","data":null}`
- 余额不足：`{"code":400,"msg":"余额不足","data":null}`

规则：以 `tb_pay_order.uk_biz_order_no` 唯一键 + 状态判断实现**幂等**（重复支付不重复扣款）；余额支付跨库扣 `tb_user.balance`，**zg-api 需补 `UserClient`**（见 4.2）；支付成功发 MQ `pay.success`（见 5）。**Service 待用户实现**。

---

### 3.6 AI 服务（ai-service :8086）

#### 3.6.1 AI 导购流式对话 ⬜（SSE 契约前端 ✅、Milvus/GLM 基础设施 ✅）

| 方法 | 路径 | 鉴权 |
|---|---|---|
| GET | `/api/ai/chat/stream` | 是 |

请求参数（query）：

| 参数 | 必填 | 说明 |
|---|---|---|
| message | 是 | 用户问题（URL encode） |
| sessionId | 是 | 会话 id，前端进页面生成 `s-<时间戳>`，用于 Redis 多轮记忆 |
| authorization | 是 | ⚠️ **token 走 query 传**：浏览器 EventSource 无法自定义请求头，前端把 token 放这里 |

⚠️ **联调必改点（网关）**：`AuthGlobalFilter` 目前只读 `authorization` **请求头**，EventSource 带不上头会直接被 401。两个方案二选一：① 网关 filter 改为"头里没有就从 query 参数 `authorization` 取"（推荐，改动小）；② 把 `/api/ai/chat/stream` 加入网关白名单、ai 服务内自行验签。**建议方案 ①**。

SSE 事件协议（前端 `ai.js` 契约）：

```text
Content-Type: text/event-stream

data: 你好！根据
data: 你的需求

event: items
data: [{"id":1001,"name":"小米14 Pro ...","price":499900,"image":"/images/p1001.jpg"}]

data: [DONE]
```

| 事件 | 说明 |
|---|---|
| `data: <文本片段>` | 逐 token 流式回答（前端打字机拼接） |
| `event: items` + `data: [商品JSON]` | 推荐商品卡片（字段含 id/name/price/image） |
| `data: [DONE]` | 结束标记，前端关流 |

响应约定：流式接口**不包 Result 结构**（SSE 流无法中途改 body）；异常时建议先发一段 `data: <错误文案>` 再 `[DONE]` 收尾，前端已有兜底文案"连接中断"。

规则：RAG 链路（embedding → Milvus top-k → prompt 模板 → GLM → SSE）；人设 6 条规则见 `prompts/system.st`；会话记忆存 Redis db2，按 sessionId 保留最近 N 轮并截断。**Service 待用户实现**。

---

## 4. 内部 Feign 契约（服务间调用，不经网关）

### 4.1 ItemClient ✅（`zg-api/client/ItemClient.java` 已定）

```java
@FeignClient(value = "item-service", path = "/items")
public interface ItemClient {
    @GetMapping("/{id}")
    ItemDTO queryItemById(@PathVariable("id") Long id);   // trade 下单核价、cart 结算刷新用

    @PutMapping("/stock/deduct")
    void deductStock(@RequestBody List<OrderDetailDTO> details);  // trade 下单预扣
}
```

- `ItemDTO` 字段：id/name/price/stock/image/categoryId/brandId/spec/sales/status/createTime/updateTime。
- `OrderDetailDTO` 字段：itemId/num。
- 调用方：trade-service（下单、超时回滚）。**缺口**：无 restore 接口、无 UserClient（见下）。

### 4.2 UserClient 📋（待补，支付扣余额用）

pay 服务余额支付需跨库扣 `tb_user.balance`，建议补：

```java
@FeignClient(value = "user-service", path = "/users")
public interface UserClient {
    @PutMapping("/{id}/balance/deduct")
    void deductBalance(@PathVariable("id") Long userId, @RequestBody Map<String,Integer> body); // {"amount": 分}
}
```

（契约为提案，字段以用户实现时定案为准。）

### 4.3 ItemClient（AI 侧）📋（待补，知识入库/推荐卡实时数据用）

zg-ai 无数据库，商品知识向量化与推荐卡片回填需经 Feign 拿商品数据（可复用 `ItemClient.queryItemById`，另需分页全量接口用于知识入库批量任务）。

---

## 5. MQ 消息契约（`zg-api/constants/MqConstants.java` ✅ 已定）

| 组件 | 名称 | 用途 |
|---|---|---|
| 交换机 | `trade.topic` | 直连交换机，接收死信转投 |
| 交换机 | `trade.delay.direct` | 延迟交换机（配合 TTL 队列） |
| 队列 | `trade.order.dead.queue` | TTL 队列（如 30min），无消费者，过期成死信 |
| 队列 | `trade.order.create.queue` | 死信落点，trade 服务消费 → 关单 |
| 路由键 | `order.delay` | 下单成功 → 发往延迟队列 |
| 路由键 | `order.create` | 死信转投键（绑定关系实现时对齐常量） |
| 交换机 | `pay.topic` | 支付成功通知交换机 |
| 队列 | `pay.notify.queue` | trade 服务消费 → 订单置已付款、写 pay_time |
| 路由键 | `pay.success` | pay 服务支付成功 → 发此键 |

**流程**：下单 → 发 `order.delay` 延迟消息 → TTL 过期变死信 → 经 `trade.topic`/`order.create` 进 `trade.order.create.queue` → trade 消费：查订单仍为 status=1 则置 5 已关闭 + 回滚库存（需 3.2.5 restore 接口）。支付成功 → `pay.success` → trade 消费：订单 1→2 + 写 pay_time + 销量累加（方案待拍板）。

---

## 6. 前端对接差异清单（联调前必读）

| # | 差异 | 现状 | 建议 |
|---|---|---|---|
| 1 | authorization 头无 `Bearer ` 前缀 | 前端存纯 token | 网关 filter 剥可选前缀，兼容两种 |
| 2 | AI 流式 token 走 query 参数 | EventSource 限制 | 网关从 query 兜底取 token |
| 3 | 支付方式未传后端 | 弹窗选了但 `orderApi.pay` 无 body | 前后端同步补 `payType` |
| 4 | cart 无真实接口定义 | mock 走 localStorage | 按 3.3 提案落接口，前端补 `cartApi` |
| 5 | 商品字段名映射 | 前端要 `brand/categoryName/desc`，DB 是 `brand_id/description` | 后端 DTO 迁就前端（join 映射） |
| 6 | 登录响应字段 | 前端 store 用 `res.token/res.username` | 后端返回 `{token,userId,username}` |
| 7 | 订单响应 | 前端用 `o.id/o.totalFee/o.goods/o.createTime/...` | 后端返回结构见 3.4.1 |
| 8 | 分页结构 | 前端消费 `{list,total}` | zg-common 补 PageDTO 类 |

---

## 7. 实现状态总表与建议顺序

| 服务 | 接口数 | 状态 |
|---|---|---|
| common（health） | 1 | ✅ |
| user | 3（+2 组规划） | ⬌ 契约 ✅，Service 待用户实现 |
| item | 3（+1 内部 🔶 +1 建议补） | ⬌ 契约 ✅，Service 待用户实现 |
| cart | 5（提案） | 📋 双端待定 |
| trade | 2（+1 规划） | ⬌ 契约 ✅，Service 待用户实现 |
| pay | 1 | ⬌ 契约 ✅，Service 待用户实现 |
| ai | 1（SSE） | ⬌ 契约 ✅，Service 待用户实现 |

**建议实现顺序**（每步 Service 由用户写、AI 审）：user（最独立，练 JWT/BCrypt）→ item（练 MP 分页/join 映射）→ cart（练 Redis Hash）→ trade（练 Feign+事务+快照，最重头）→ pay（练跨库扣款+MQ+幂等）→ ai（练 Spring AI + Milvus + SSE，压轴）。

# 智购商城（zgmall）需求文档

- 版本：v0.1
- 日期：2026-09-22
- 文档口径：本文档**反推自现有代码、SQL 脚本与前端页面**，不是凭空设计。代码/脚本与本文档冲突时**以代码为准**，并在文中标注差异。

> 状态图例：
> - ✅ 已实现（代码已存在且验证过）
> - 🔶 骨架已铺（配置/常量/Feign 契约/MQ 常量已落盘，业务代码待写）
> - ⬜ 待实现（**Service 层核心业务由用户本人实现**，见"分工约定"）
> - 📋 规划中（连接口契约都未定，仅业务设计）

---

## 1. 项目概述

**定位**：普通综合商城 + AI 导购"小购"（RAG 场景的简历项目）。AI 导购是差异化核心，商城是 AI 的数据与业务底座。

**架构**：微服务，9 个 Maven 模块、7 个运行时服务：

| 模块 | 端口 | 职责 | 数据源 |
|---|---|---|---|
| zg-gateway | 8080 | 统一入口、路由、JWT 全局鉴权 | 无（仅 Nacos） |
| zg-user | 8081 | 注册/登录/密码/个人信息/地址 | MySQL `zg_user` |
| zg-item | 8082 | 分类/商品/搜索/详情/库存 | MySQL `zg_item`（关键词 LIKE；ES 归论坛项目） |
| zg-cart | 8083 | 购物车 | **纯 Redis**（无数据库） |
| zg-trade | 8084 | 下单/订单/超时关单/降价提醒(规划) | MySQL `zg_trade` + Redis db1 + RabbitMQ |
| zg-pay | 8085 | 模拟支付 | MySQL `zg_pay` + RabbitMQ |
| zg-ai | 8086 | AI 导购"小购"（RAG + SSE） | Milvus + Redis db2（无数据库） |
| zg-common | — | 公共类：Result/异常/JWT/拦截器/MP 配置 | — |
| zg-api | — | Feign 客户端 + DTO + MQ 常量（无主类，纯依赖库） | — |

**技术栈**（以父 pom 为准）：Spring Boot 3.4.5 · Spring Cloud 2024.0.1 · Spring Cloud Alibaba 2023.0.3.2 · Spring AI 1.0.0（zhipuai + milvus starter）· MyBatis-Plus 3.5.12（+ mybatis-plus-jsqlparser）· jjwt 0.12.6 · Java 17 · Redis · RabbitMQ · Nacos 注册中心 · Milvus 向量库。

**环境拓扑**：中间件全部部署在阿里云 ECS `你的ECS公网IP`（MySQL 3306 / Redis 6379 密码见本地dev配置 / Nacos 8848+9848 / RabbitMQ 5672 / Milvus 19530），Java 服务在用户本地 IDEA 运行，经网关 8080 联调。

**分工约定**：Entity/Mapper/DTO/Controller 骨架、配置、文档、审查由 AI 完成；**Service 层核心业务（登录注册、下单扣库存、订单状态流转、超时关单、支付、RAG 检索问答等面试深挖点）由用户本人实现**，AI 只给实现思路，不代写。

---

## 2. 总体业务约定

1. **金额**：一律以**分**为单位存 `INT`（如 4999.00 元 = 499900）。
2. **时间**：`DATETIME`，接口 JSON 序列化为 `yyyy-MM-dd HH:mm:ss`（zg-common `JacksonConfig` 已配置）。
3. **主键**：雪花算法 `BIGINT`（MyBatis-Plus `id-type: assign_id` 已配置）；种子数据用固定 id（用户 1/2、商品 1001~1016）。
4. **鉴权**：JWT（jjwt 0.12.6），登录后签发，TTL 30 分钟（`zg.jwt.ttl: 1800000`）。网关校验，放行白名单见 api.md。⚠️ 差异：当前前端 `request.js` 发的 `authorization` 头是**纯 token，无 `Bearer ` 前缀**，网关 `AuthGlobalFilter` 也是直接解析整串。建议后续统一（见"遗留问题"）。
5. **订单状态**（`tb_order.status`）：1 未付款 / 2 已付款 / 3 已发货 / 4 已完成 / 5 已关闭。
6. **支付方式**（`tb_order.payment_type`、`tb_pay_order` 相关）：1 余额 / 2 模拟支付（前端弹窗选项为 `balance` / `mock`）。
7. **商品状态**：1 上架 / 0 下架；**用户状态**：1 正常 / 0 禁用；**支付单状态**：1 未支付 / 2 已支付。
8. **数据库拆分**：四个业务库互不关联（无外键），跨库协作走 Feign + MQ；订单表/明细表对商品、地址做**快照冗余**，商品改价、改地址不影响历史订单。

---

## 3. 用户模块（zg-user，库 zg_user）

### 3.1 注册 ⬜

**用户故事**：作为游客，我想要注册账号，以便登录后购物、下单、使用 AI 导购。

**业务规则**
- 前端提交 `{username, phone, password}`；注册成功后前端自动再调登录（注册并登录）。
- 密码 BCrypt 加密存储（种子数据中 `123456` 的 BCrypt 密文可验证格式）；明文密码绝不入库。
- `username` 唯一（`uk_username` 唯一键兜底）；`phone` 建了普通索引 `idx_phone`。

**边界条件**
- 用户名/手机号重复：注册前查重，捕获唯一键冲突兜底，返回友好提示。
- 用户名校验：非空、长度 ≤50；密码规则与修改密码一致（6~20 位）。

**数据来源**：MySQL `zg_user.tb_user`（INSERT）。

### 3.2 登录 ⬜（网关/JWT 侧 ✅）

**用户故事**：作为注册用户，我想要登录，以便访问需登录的功能。

**业务规则**
- 提交 `{username, password}`；校验 BCrypt 密码 → 签发 JWT（`JwtTool.createToken(userId)`，subject=userId）→ 返回 `{token, userId, username}`（**响应字段以前端 store 消费为准**）。
- `status=0`（禁用）的用户拒绝登录。
- 网关 `AuthGlobalFilter` 已把 `/api/users/login`、`/api/users/register` 加入白名单；`JwtTool`（jjwt 0.12 新 API）已实现签发/解析。

**边界条件**
- 用户名不存在与密码错误**统一提示"用户名或密码错误"**，不暴露账号是否存在（防撞库）。
- Token 过期（30 分钟）→ 网关 401 → 前端自动清 token 跳登录页（`request.js` 已处理）。

**数据来源**：MySQL `tb_user`（SELECT）；签发 JWT。

### 3.3 修改密码 ⬜（前端页面 ✅）

**用户故事**：作为登录用户，我想要修改密码，以便保障账号安全。

**业务规则**
- 前端提交 `{oldPassword, newPassword}`；校验原密码正确后更新为新 BCrypt 密文；成功后前端强制重新登录（页面已实现）。
- 密码 6~20 位；新旧密码不能相同（前端已校验，**后端必须复验**——前端校验只是体验，不是安全边界）。

**边界条件**：原密码错误 → 拒绝并提示；未登录 → 网关 401。

**数据来源**：MySQL `tb_user`（UPDATE password）。

### 3.4 个人信息 📋

**用户故事**：作为登录用户，我想要查看/编辑昵称、手机号、头像，以便完善个人资料。

**现状**：`tb_user` 有 `nickname/phone/avatar/balance` 字段，但**前端无个人信息页面**（只有登录态存储了 username），接口契约未定。规划：`GET /users/me` 查询 + `PUT /users/me` 更新（phone 唯一性不加约束，仅索引）。

**边界条件**：仅能改自己的信息（userId 取自网关注入的 `user-info` 头 → `UserContext`）。

**数据来源**：MySQL `tb_user`。

### 3.5 收货地址 📋

**用户故事**：作为用户，我想要维护收货地址簿，以便下单时快速选择。

**现状**：`tb_address` 表已建（含省/市/区、`is_default` 默认标记），种子数据给 demo 用户配了 2 条地址；但**前端结算页目前是手填表单**，地址簿 UI 与接口均未定义。规划：地址 CRUD + 设默认 + 结算页选择地址。下单仍按**快照**复制进订单（改地址不影响历史订单）。

**边界条件**：只能操作自己的地址；删除默认地址后需重新指定或清空默认。

**数据来源**：MySQL `tb_address`。

---

## 4. 商品模块（zg-item，库 zg_item）

### 4.1 分类列表 ⬜（前端页面 ✅）

**用户故事**：作为游客，我想要浏览商品分类，以便按类目找商品。

**业务规则**：返回全部分类，按 `sort` 升序（越小越靠前）；仅返回有意义的分类（当前无下架概念）。

**数据来源**：MySQL `tb_category`（6 条种子数据）。

### 4.2 商品分页列表 + 搜索 ⬜（前端页面 ✅）

**用户故事**：作为游客，我想要搜索/筛选/排序商品，以便快速找到目标商品。

**业务规则**
- 查询参数（前端契约）：`keyword`（搜索词）、`categoryId`（分类筛选，可空）、`page`（默认 1）、`pageSize`（默认 10，前端列表页实际传 12）、`sort`（`''` 综合 / `sales` 销量 / `priceAsc` 价格升序 / `priceDesc` 价格降序）。
- 只展示 `status=1`（上架）的商品；前端 mock 的过滤条件是 `status !== 2`（mock 遗留，**后端以 DB 的 1 上架/0 下架为准**）。
- 响应分页结构 `{list, total}`（前端契约；zg-common 尚无 PageDTO 类，骨架阶段需补一个通用分页包装类）。
- **字段映射差异（联调必踩坑，务必对齐）**：前端卡片消费 `categoryName`（分类名）、`brand`（品牌名）、`desc`（描述）三个字段，而 DB 是 `category_id`、`brand_id`、`description`。后端查询时需 join `tb_category`/`tb_brand` 并在 DTO 里给出前端要的字段名（或前端改字段名，二选一，推荐后端 DTO 迁就前端，前端 0 改动）。

**搜索实现（2026-09-23 已定案）**
- 关键词匹配：MySQL `LIKE` 查 `name` + `brand` + `categoryName`（商品量级 100~1000+，全表扫毫秒级，无需倒排索引）。
- 语义搜索：AI 导购走 Milvus 向量检索（RAG），与关键词搜索是两条通道。
- ES：**不上**。倒排索引+分词的用武之地（长文本/相关性）归论坛项目帖子搜索，避免两项目技术重复；选型对比本身是面试加分点。

**边界条件**：空关键词+空分类=全部上架商品；排序仅支持上述 4 种；分页参数做范围保护（pageSize 上限如 50）。

**数据来源**：MySQL `tb_item`（+ join `tb_category`/`tb_brand`）。

### 4.3 商品详情 ⬜（前端页面 ✅）

**用户故事**：作为游客，我想要查看商品详情，以便决定是否购买。

**业务规则**：按 id 返回完整字段（含 `description`/`spec`/`stock`/`sales`）；下架商品（status=0）详情页提示"已下架"。

**边界条件**：id 不存在 → 404 语义（返回 `Result.error(404, "商品不存在")`，前端 mock 有对应提示文案）。

**数据来源**：MySQL `tb_item` + join 品牌/分类名。

### 4.4 库存预扣与回滚 🔶（Feign 契约已定，实现 ⬜）

**用户故事**：作为交易系统，我要在下单时原子性预扣库存，避免超卖；超时关单时回滚库存。

**业务规则**
- 契约已定（`zg-api ItemClient`）：`PUT /items/stock/deduct`，body 为 `[{itemId, num}, ...]`（`OrderDetailDTO`）。
- ⚠️ **审查发现的缺口**：`ItemClient` 只有 `deductStock`，没有库存回滚方法。超时关单要加回库存，需要补 `PUT /items/stock/restore`（或 deduct 支持负数，推荐显式 restore 接口，语义清晰）。
- 扣减实现要点（供用户实现时参考）：`UPDATE tb_item SET stock = stock - ? WHERE id = ? AND stock >= ?`，受影响行数=0 即库存不足——**条件更新天然防超卖**，不要先查再改（非原子，并发会超卖）。机制解释：先查后改在并发下两个线程都能查到库存 1，双双扣减成功导致超卖；条件 UPDATE 让数据库行锁串行化判定。
- 该接口是**内部接口**：Feign 直连 item-service（不经网关）。网关白名单只放行 `GET /api/items/**`，PUT 会被拦，正好防止外部直接调。

**边界条件**：任一商品库存不足 → 整体下单失败，已扣的部分要回滚（事务/补偿）。

**数据来源**：MySQL `tb_item.stock`。

---

## 5. 购物车模块（zg-cart，纯 Redis）

### 5.1 购物车增删改查 ⬌（后端 ⬜ + 前端真实接口未定义）

**用户故事**：作为登录用户，我想要把商品加入购物车并管理数量，以便合并结算。

**业务规则（存储方案已定，见项目决策记录）**
- **无数据库**，纯 Redis Hash：key = `zg:cart:{userId}`，field = `itemId`，value = 加购时商品快照 JSON `{itemId, name, price, image, spec, num}`（`zg-cart/application.yml` 使用 Redis db0（连接信息见本地 dev 配置））。
- userId 从网关注入的 `user-info` 头取（`UserContext`），不信任前端传参。
- 加购已存在商品 → 数量累加；数量上限建议 99（边界保护）。
- **快照仅用于展示**：购物车里的 price 可能过期，**结算以 item 服务实时价为准**（已定的决策提案）。
- 勾选（checked）是**前端本地状态**，不上 Redis。

**边界条件**
- ⚠️ 前端 `src/api/index.js` **没有定义 cart 的真实接口**（mock 模式全走 localStorage），所以后端 cart 接口是设计提案（见 api.md），实现后前端需补 `cartApi` 并把 `USE_MOCK` 切 false 联调。
- 商品下架/删除后，购物车快照仍存在 → 结算时校验 item 服务返回状态，失效商品提示"已失效"。
- 未登录访问购物车 → 网关 401 → 前端跳登录。

**数据来源**：Redis db0，`zg:cart:{userId}` Hash。

---

## 6. 交易模块（zg-trade，库 zg_trade + Redis db1 + MQ）

### 6.1 下单 ⬜（前端契约 ✅）

**用户故事**：作为用户，我想要从购物车勾选商品提交订单，以便完成购买。

**业务规则**
- 前端提交：`{receiver, phone, address, remark, goods: [{itemId, name, price, num, spec, image}]}`；响应需含前端消费字段：`id, totalFee, status, createTime, receiver, phone, address, goods`（前端跳转支付弹窗全依赖这些）。
- 服务端处理顺序（实现思路，供参考）：① 校验收货信息与 goods 非空 → ② 调 `ItemClient.queryItemById` 逐件核验商品存在且上架 → ③ **金额以服务端实时价为准**重算 `totalFee`（前端传的 price 仅作展示，**不能采信**——防止改包篡改价格，这是安全要点）→ ④ 调 `ItemClient.deductStock` 预扣库存 → ⑤ 写 `tb_order`（status=1 未付款，地址三字段快照）+ `tb_order_detail`（商品名/单价/数量/图/规格快照）→ ⑥ 发送延迟消息（见 6.3）。
- 多表写入用本地事务保证原子性；Feign 扣库存成功但本地事务失败时需补偿回滚库存（跨服务一致性难点，面试深挖点）。
- `remark`（备注）：DB 无此字段。方案 A：订单表加 `remark VARCHAR(255)` 列；方案 B：一期不存（前端仍传，后端忽略）。**需用户拍板**，推荐方案 A（ALTER 一条的事，简历上多一个完整度）。
- 支付成功后按 `payment_type` 分流：1=余额扣款（pay 服务调 user 服务扣 `balance`），2=模拟扫码。

**边界条件**
- 库存不足 → 下单失败并回滚已扣库存。
- 商品下架 → 下单失败。
- 重复提交：前端有 loading 防抖；服务端可加幂等（Redis db1 预留），一期可不做。

**数据来源**：MySQL `tb_order`/`tb_order_detail`（INSERT）；Feign 调 item 服务扣库存；MQ 发送延迟消息。

### 6.2 订单列表/详情 ⬜（列表页前端 ✅）

**用户故事**：作为用户，我想要查看我的订单，以便支付、跟踪状态。

**业务规则**
- 列表：按用户过滤（`user-info` 头），按 `create_time` 倒序；订单+明细组装返回（前端渲染 `o.goods`）。
- 详情接口 `GET /orders/{id}` 规划中（前端暂未用）。
- 状态展示：1 待付款（可点"立即支付"）/ 2 已付款 / 3 已发货 / 4 已完成 / 5 已关闭（前端 `ORDER_STATUS` 已定义标签）。

**边界条件**：只能看自己的订单（userId 服务端取，不信任参数）。

**数据来源**：MySQL `tb_order` + `tb_order_detail`（join order_id）。

### 6.3 超时关单 🔶（MQ 常量已定，实现 ⬜）

**用户故事**：作为系统，我要自动关闭超时未支付的订单并释放库存，以便库存不被锁死。

**业务规则（MQ 常量已落盘 `MqConstants`）**
- 组件：延迟交换机 `trade.delay.direct`、直连交换机 `trade.topic`、队列 `trade.order.dead.queue`（TTL 队列，无消费者）、`trade.order.create.queue`、路由键 `order.delay` / `order.create`。
- **机制解释（TTL + 死信，黑马经典套路）**：下单成功后向延迟交换机发一条消息（routing key `order.delay`）→ 进入 TTL 队列（如 30 分钟过期，无消费者）→ 消息过期变成死信 → 按队列配置的死信交换机 `trade.topic` 转发 → `trade.order.create.queue` 的消费者（trade 服务）收到 → 查订单，若仍 status=1 则置 5 已关闭、写 `close_time`，并回滚库存（需要补 4.4 的 restore 接口）。
- 超时时长：建议 30 分钟（可配，写进常量/配置即可）。
- 消费者要做**状态判断**（只关 status=1 的），因为用户可能已付款——关单动作是幂等安全网。
- ⚠️ 注意两个队列/两个交换机的绑定关系：死信队列的 `x-dead-letter-exchange`/`x-dead-letter-routing-key` 与 `trade.topic` 的绑定键**实现时按 MqConstants 对齐**，声明队列与消费方绑定要一致，否则消息进死信后无人消费。

**边界条件**：已付款订单不受影响；关单与支付并发时以订单状态为最终裁决（支付成功后关单消费者查到 status=2 则跳过）。

**数据来源**：RabbitMQ 延迟消息；MySQL `tb_order`（UPDATE status/close_time）；Feign 回滚库存。

### 6.4 销量累加 ⬜

**表设计注释已定**：`tb_item.sales` 是"冗余字段，下单后异步累加"。实现时机建议：**支付成功后**（而非下单时）累加，更符合"真实销量"语义；渠道可用 MQ（复用支付成功通知）或 trade 直接调 item 加销量接口。具体方案用户实现时拍板。

**数据来源**：MySQL `tb_item.sales`（UPDATE 累加）。

### 6.5 降价提醒 📋（之前商定的功能，未实现）

**用户故事**：作为用户，我想要在关注的商品降价时收到提醒，以便不错过优惠。

**现状**：**完全未实现**——前端无 UI（已 grep 确认无"降价/提醒"相关代码）、`tb_item` 无价格历史表、trade 无提醒表。这是商定要做的功能，需先补设计：

**设计要点（提案，待用户挑刺后定案）**
1. 检测时机：商品改价处（item 服务）对比新旧价格，或定时任务对比价格历史。
2. 数据落点二选一：
   - 方案 A（新表）：`tb_price_history(item_id, price, create_time)` 记录每次改价；`tb_price_alert(user_id, item_id, alert_price, status, create_time)` 存订阅（放 zg_trade 或新建库均可）。
   - 方案 B（Redis）：`zg:alert:{itemId}` Hash 存订阅者，改价时比对后推 MQ 通知；订阅关系要持久化则仍须落库。
3. 通知渠道：一期可只做**站内**（订单列表页顶部提醒条 / 前端消息中心），不做短信邮件（成本）。
4. 交互：商品详情页"降价提醒"按钮 + 目标价设置（如"降到 100 元以下提醒我"）。

**边界条件**：同用户同商品重复订阅幂等；提醒触达一次后订阅置为已提醒；商品下架自动失效。

**数据来源**：待定案（MySQL 新表 + MQ 通知 + 前端新 UI）。

---

## 7. 支付模块（zg-pay，库 zg_pay + MQ）

### 7.1 模拟支付 ⬜（前端弹窗 ✅）

**用户故事**：作为用户，我想要支付订单，以便完成购买。

**业务规则**
- 前端契约：`POST /pay/orders/{orderId}/pay`。⚠️ 前端弹窗有"账户余额/模拟扫码"二选一，但 `orderApi.pay` **没把 payType 传给后端**（前端契约缺口，需补 body `{payType}` 或 query 参数）。
- 支付单：`tb_pay_order` 以 `biz_order_no`（=订单 id）**唯一键**保证一笔订单只有一张支付单——**幂等**：重复支付请求查支付单，已支付直接返回成功；唯一键兜底并发。
- 支付方式分流：
  - **余额**：校验 `tb_user.balance >= 金额` → 扣余额 → 改支付单/订单状态。⚠️ 余额在 user 库，pay 服务跨库扣款，**zg-api 需补 `UserClient`**（如 `PUT /users/{id}/balance/deduct`），目前不存在。
  - **模拟扫码**：直接置成功（demo 语义）。
- 支付成功后发 MQ 消息（交换机 `pay.topic`，路由键 `pay.success` → 队列 `pay.notify.queue`），trade 服务监听后：订单 1→2、写 `pay_time`、触发销量累加（6.4）。
- 订单状态流转闭环：下单(1) → 支付成功(2) → 发货(3)/完成(4)（3/4 一期无后台操作界面，可留接口或砍掉，状态码已预留）。

**边界条件**
- 订单不存在/已关闭（status=5）→ 拒绝支付。
- 余额不足 → 拒绝并提示差额。
- 重复支付 → 幂等返回成功（不重复扣款）。
- 金额以订单 `total_fee` 为准（服务端数据），不采信前端。

**数据来源**：MySQL `tb_pay_order`；Feign 扣 user 余额；RabbitMQ `pay.success` 通知；MySQL `tb_order` 状态由 trade 更新。

---

## 8. AI 导购模块（zg-ai，Milvus + Redis db2）

### 8.1 RAG 问答链路 🔶（基础设施已验证，业务 ⬜）

**用户故事**：作为购物者，我想要向"小购"描述需求（预算、场景），以便获得不超过 3 款的精准推荐和对比。

**链路设计（已定案）**：
```
用户问题 → [1] 向量化(embedding) → [2] Milvus 相似检索(top-k 商品知识)
        → [3] 组装 prompt(rag-prompt-template.st: {context}+{question})
        → [4] GLM 生成回答 → [5] SSE 流式返回前端(打字机效果)
```

**各环节现状与待办**
- [2][3][4] 基础设施 ✅：Milvus `zg_mall.vector_store` 集合已由 Spring AI 自动初始化（doc_id PK / content / metadata JSON / embedding 2048 维、COSINE、已加载），搜索链路已验证；GLM 配置走环境变量 `ZHIPUAI_API_KEY`；两个 prompt 模板已落盘（见 8.2）。
- [1] 知识入库 ⬜：**商品知识向量化任务尚未实现**（无任何代码）。实现思路：读取 `tb_item`（zg-item 库，zg-ai 无数据库 → 走 Feign 调 item 服务拿商品数据，或 item 服务侧提供导出接口），把 `name + spec + description + 品牌/分类名` 拼成知识文档，用 GLM `embedding-3`（2048 维，与集合维度一致）生成向量，`DocumentWriter` 写入 Milvus；metadata 里带 `itemId/price` 供检索后回填。
- [5] SSE ⬜：`GET /ai/chat/stream?message=&sessionId=`，事件协议见 api.md；前端 EventSource 已写好。
- 多轮会话记忆 ⬜：Redis db2 已配好。思路：按 `sessionId` 存最近 N 轮对话（LIST 或 String JSON），超长截断（只保留最近几轮），把历史拼进 prompt 实现上下文连续；**RAG 检索仍只针对最新问题**。
- 推荐卡片：回答中附 `event: items` 事件推送商品 JSON（前端渲染迷你卡片，点击跳详情）——**商品数据必须来自检索结果/实时查询，不得让 LLM 编造价格库存**（见人设规则）。

**边界条件**
- 检索不到相关知识 → 按模板要求直接说明"知识不足"，不得胡编（防幻觉是核心卖点）。
- 未登录 → 网关 401（前端 EventSource 无法设 header，token 走 query 参数传递，后端实现时需从 query 解析）。
- 流式中断 → 前端显示"连接中断"（已实现）。

**数据来源**：Milvus `zg_mall.vector_store`（检索）；GLM（生成）；Redis db2（会话记忆）；Feign（商品实时数据）。

### 8.2 人设与防幻觉规则 ✅（prompts 已落盘）

`system.st` 已定义"小购"人设 6 条规则，实现时用 `ChatClient` 加载：
1. 只依据检索到的商品知识回答，知识外信息一律不编造，直接说"这个我还不确定"。
2. 推荐要给出品牌型号 + 理由，**一次最多 3 款**。
3. 价格仅参考，必须提醒"实际价格以商品页为准"。
4. 多商品对比用简洁列表/表格。
5. 语气像懂行的朋友，亲切自然，不要客服腔，精炼不啰嗦。
6. 售后/物流/退换货等知识范围外问题，引导联系人工客服。

`rag-prompt-template.st` 定义 RAG 拼装格式：`【商品知识】{context}` + `【用户问题】{question}` + 兜底约束。

---

## 9. 已实现 / 待实现边界总表

| 模块 | 功能 | 状态 | 说明 |
|---|---|---|---|
| 全局 | 服务骨架/注册/网关路由/JWT 过滤 | ✅ | 7 服务 Nacos healthy，链路实测通 |
| 全局 | Result/异常/JWT/拦截器/MP 分页配置 | ✅ | zg-common |
| user | 注册 / 登录 / 修改密码 | ⬜ | 前端页已建；Service 待用户实现 |
| user | 个人信息 / 地址簿 | ⬜ | 前端页已建（ProfileView/AddressView）；契约已定；Service 待用户实现 |
| item | 分类 / 列表搜索 / 详情 | ⬜ | 前端已建；Service 待实现；搜索=LIKE（ES 已决策不上） |
| item | 库存预扣 | 🔶 | Feign 契约已定，实现待做 |
| cart | 购物车 CRUD | ⬜ | Redis 方案已定；前端 cartApi 契约已补；Service 待用户实现 |
| trade | 下单 / 订单列表 | ⬜ | 前端契约已定；Service 待实现 |
| trade | 超时关单 | 🔶 | MQ 常量已定；TTL+DLX 实现待做 |
| trade | 销量累加 | ⬜ | 表冗余字段已留，方案待拍板 |
| trade | 降价提醒 | 📋 | 商定功能，无表无 UI，设计见 6.5 |
| pay | 模拟支付 | ⬜ | 前端弹窗已建、payType 已传；Service 待用户实现 |
| ai | Milvus/GLM 基础设施 + prompt | ✅ | 集合初始化、搜索链路、模板均验证过 |
| ai | 知识入库 / 聊天接口 / SSE / 会话记忆 | ⬜ | 全链路业务代码待用户实现 |

---

## 10. 遗留问题与待办清单

1. **API Key 泄漏风险（高优先）**：`zg-ai/src/main/resources/application-dev.yml` 里有一份**硬编码的 GLM API Key**。处理：删除该文件内容改走环境变量 `ZHIPUAI_API_KEY`（IDEA Run Configuration 里注入），把 `application-dev.yml` 加入 `.gitignore`；key 已明文出现过的，建议到智谱开放平台**重置该 key**（防止被爬走盗刷）。
2. **~~ES 9200 不可达~~ 已解决（2026-09-23）**：决策不上 ES——关键词走 MySQL LIKE，语义走 Milvus，ES 戏份归论坛项目。zg-item 的 ES 依赖与配置已摘除。
3. **Authorization 头格式**：前端发纯 token，网关直接解析。建议统一为 `Bearer <token>` 并在 `AuthGlobalFilter` 里剥前缀（兼容两种写法）。
4. **Nacos 注册 IP 是 `192.168.100.1`**（VMnet8 网卡）：本地联调无碍；上容器/换网络前需设 `spring.cloud.nacos.discovery.ip`。
5. **JWT secret**：各服务 `zg.jwt.secret` 目前是占位串（真实值仅存各模块本地 dev 配置），演示可接受，正式化前要换并保证 gateway 与各服务一致（同一把钥匙才能验签）。
6. **~~订单表缺 `remark` 字段~~ 已解决（2026-09-23）**：03_zg_trade.sql 已加 `remark VARCHAR(200) NULL` 列；Order PO / OrderVO 已补 `remark` 字段。注意：**若你已导入过 03 脚本，需对现有库执行** `ALTER TABLE tb_order ADD COLUMN remark VARCHAR(200) NULL COMMENT '买家备注';`
7. **前端契约缺口**：~~支付方式未传后端~~（已传 payType）、~~cart 真实接口未定义~~（cartApi 已补）、商品字段名映射（`brand/categoryName/desc` vs `brand_id/description`）。
8. **Nacos 配置中心迁移**：当前数据源配置在各服务 yml。迁移为可选加分项（体现 Nacos 注册+配置双能力），一行 `spring.config.import: nacos:` 起步。
9. **安全组**：中间件密码仅存本地 dev 配置，安全组必须保持 /32 收紧，不可开 0.0.0.0/0（ECS 侧既有规则勿动）。

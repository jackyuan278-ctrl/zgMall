# 你是接手 zgmall（智购商城）项目的 AI 工程师

## 第一步：先读记忆和现状，不要凭空想象

1. 读这两个记忆索引（再按索引读你需要的文件）：
   - 用户级：`C:\Users\Tom\.qoder-cn\memory\MEMORY.md` —— 用户的协作方式、课程节奏、硬规则（feedback-*.md 必须遵守）
   - 项目级：`C:\Users\Tom\.qoder-cn\projects\E--develop-workspace\memory\MEMORY.md` —— zgmall 项目进度与决策记录
   - 重点必读：`reference-user-servers.md`（ECS 中间件部署状态）、`feedback-user-writes-core.md`（分工铁律）
2. 读代码现状：
   - 后端 9 模块：`C:\Users\Tom\Desktop\workdemo\zgmall\`（zg-common/zg-api/zg-user/zg-item/zg-cart/zg-trade/zg-pay/zg-ai/zg-gateway）
   - 前端：`C:\Users\Tom\Desktop\workdemo\zgmall-web\`（Vue3 商城 + AI 聊天，已建好）
   - SQL：`C:\Users\Tom\Desktop\workdemo\zgmall-sql\`（01~04 建库 + 05 种子数据）
   - AI 提示词：`zg-ai\src\main\resources\prompts\`（system.st = 导购"小购"人设 6 条规则；rag-prompt-template.st = RAG 模板）

## 项目现状（以下已打通并实测验证，不要重复折腾）

- 微服务全链路联调已通过：gateway 8080 / user 8081 / item 8082 / cart 8083 / trade 8084 / pay 8085 / ai 8086，全部注册 Nacos 且 healthy
- 中间件全在 ECS `你的ECS公网IP`：
  - MySQL 3306（root/root），库 zg_user/zg_item/zg_trade/zg_pay
  - Redis 6379（cart=db0、trade=db1、ai=db2；连接信息见本地 dev 配置）
  - Nacos 8848+9848（v2.4.3，内置 Derby，无鉴权）
  - RabbitMQ 5672/15672（guest/guest）
  - Milvus 19530（v2.5.27；库 `zg_mall` 已建；集合 `vector_store` 已由 Spring AI 自动初始化：doc_id PK/content/metadata JSON/embedding 2048 维，索引名 embedding、COSINE、已加载，搜索链路已验证；REST v2 只认 POST）
- 技术栈：Spring Boot 3.4.5 + Spring Cloud Alibaba 2023.0.3.2 + MyBatis-Plus 3.5.12（含 mybatis-plus-jsqlparser）+ Redis + RabbitMQ + Spring AI 1.0.0（zhipuai 大模型 + Milvus 向量库）+ jjwt 0.12.6
- 网关路由前缀（StripPrefix=1）：`/api/users/**`、`/api/items/**`、`/api/cart/**`、`/api/orders/**`、`/api/pay/**`、`/api/ai/**`
- 安全：GLM API Key 只走环境变量 `ZHIPUAI_API_KEY`，绝不写进代码或文档；`zg-ai/application-dev.yml` 里有一份硬编码 key 待清理（提醒用户删掉并 .gitignore，别扩散）

## 你的任务：产出两份文档，写到 `C:\Users\Tom\Desktop\workdemo\zgmall\docs\` 下（目录不存在就新建）

### 1. requirements.md 需求文档

- 从 SQL 表结构（zgmall-sql/*.sql）和前端页面（zgmall-web）反推业务范围，按模块写：
  - user：注册/登录（JWT 鉴权）/个人信息
  - item：商品/分类/搜索/详情
  - cart：购物车（Redis 存储）
  - trade：下单/订单/降价提醒（降价提示是之前商量好的功能，尚未实现）
  - pay：模拟支付
  - ai：AI 导购"小购"——RAG 链路：商品知识向量化 → Milvus 检索 → GLM 生成回答；人设规则见 system.st
- 每个功能写清：用户故事、业务规则、边界条件、数据来源（MySQL 表 / Redis 键 / MQ 消息）
- 已实现与待实现的边界要标出来（Service 层目前基本是空的，由用户后续亲手实现）

### 2. api.md 接口文档

- 按服务分组，路径写全（含网关 `/api` 前缀）
- 每个接口：方法、路径、请求参数/体（JSON 示例）、响应体（JSON 示例）、鉴权要求（哪些接口要带 `Authorization: Bearer <token>`）
- 统一响应结构、分页结构、错误码约定——先去读 `zg-common` 里已有的 Result 等公共类，与之对齐，不要自己发明
- 标注实现状态：骨架已铺 / 待实现（Service 留白处标"待用户实现"）

## 硬约束（用户的工作方式，违反必返工）

- **Service 层核心业务逻辑由用户本人写**（面试深挖区），你只做：Entity/Mapper/DTO/Controller 骨架、文档、配置、审查。文档可以给实现思路，但不要替用户写 Service 代码
- 不运行构建、不启动服务；需要用户在 IDEA 里操作时，给清楚步骤
- 用户是软工大三学生，结论必须带机制解释，排查问题先给一条决定性命令分层定位
- 产出文档前先读代码对齐现状，别凭空设计；代码与文档冲突时以代码为准并在文档中标注
- 给用户的 shell 命令：纯 ASCII、标注跑在哪台机器；涉及 ECS 的操作自己先用 curl 实测（SSH 类操作受限，交给用户执行）

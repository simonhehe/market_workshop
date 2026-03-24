## Context

- 当前订单：`Order.status` 为字符串；用户可 `pending` 取消并退款还库存；管理员可任意改状态且无自动退款。
- 前端：`Orders.vue` 列表、`OrderDetail.vue` 详情；`orderApi` 未含确认收货与筛选参数。
- 约束：SQLite + JPA；JWT 鉴权；需与 **`order-domain-baseline.md`** 对齐并增量扩展。

## Goals / Non-Goals

**Goals:**

- 用户将本人订单从 **`shipped` → `completed`**（确认收货），接口与校验清晰。
- 用户订单列表 **`GET /orders`** 支持可选 **`status`** 查询（单值优先以降低复杂度；多值可作为迭代）。
- 前端筛选 UI 与**按钮可见性规则**一致；文档含验收标准。
- 默认不传 `status` 时行为与现网一致（全量按用户过滤）。

**Non-Goals:**

- 不改变管理员批量改状态、不退款语义（除非单独变更单）。
- 不引入物流轨迹、第三方签收、超时自动收货（可记为后续 Open Question）。
- 不要求本期写真实物流通知。

## Decisions

1. **状态迁移（确认收货）**  
   - **选择**：仅允许 `shipped` → `completed`；仅 `order.userId` 与 token 一致。  
   - **理由**：与实体注释中 `completed` 一致；`processing` 直接确认是否在真实业务中存在存疑，首期收紧降低歧义。  
   - **备选**：允许 `processing` → `completed`（若产品定义「处理中」即可确认）— **Open Question**。

2. **接口形态**  
   - **选择**：`PUT /api/orders/{id}/confirm-receipt`（或 `POST .../confirm`），body 可为空 JSON。语义幂等：已是 `completed` 可返回 200 且不变更或返回明确业务码（**倾向**：幂等返回成功 + 当前实体）。  
   - **理由**：与用户 `cancel` 同为动作型子资源，便于路由与审计。

3. **列表筛选**  
   - **选择**：`GET /api/orders?status=shipped`；非法 `status`：返回 400 或忽略（**倾向 400** 并带 `ORDER_*` 码，避免静默脏参数）。  
   - **理由**：客户端显式错误优于静默全列表。

4. **数据库字段**  
   - **选择**：首期**不新增列**，依赖 `status` + `updated_at`。若需「确认收货时间」再迭代 `completed_at`（Open Question）。  
   - **理由**：最小改动；与时间相关报表有缺口时再加。

5. **积分与库存**  
   - **选择**：确认收货 **不** 扣/退积分、**不** 改库存（已在下单/取消处理）。  
   - **理由**：与现域模型一致。

6. **PointsHistory**  
   - **选择**：首期**不**强制写流水；若产品要审计，可加 `type` 新值与 `related_type=order`（非目标则省略）。

## Risks / Trade-offs

- **与管理员并发改状态**：用户确认与管理端同时写最后一笔——SQLite 事务下后者覆盖或前者失败 → **缓解**：接受「最后一次写入」；或在服务层比较 `updatedAt`（乐观锁，二期）。  
- **幂等与重复点击**：前端防抖 + 后端幂等策略。  
- **`completed` 后用户操作**：仅允许查看；取消按钮已隐藏——与现有取消仅 `pending` 一致。

## Migration Plan

1. 部署后端 → 部署前端。  
2. 无 DB 迁移（若不新增列）。  
3. 回滚： revert 发布；已在 `completed` 的订单保留终态（不回滚状态除非人工脚本）。

## Open Questions

- 是否必须记录 **确认收货时间** 独立字段？  
- `processing` 是否允许确认收货？  
- 非法 `status` 筛选：400 vs 忽略？

---

## 测试与质量（本轮分析）

> **依据**：仓库内已有 `backend/src/test`（`OrderServiceTest`、`OrderControllerTest` 等）、`doc/analysis/订单链路-UT与API测试.md`；**未**发现 JaCoCo 配置与 `@SpringBootTest` 级订单 API 用例；**未**发现 `.github/workflows`（CI **待确认**）。

### 1. 与目标要求的差距（重点关注）

| 要求 | 现状（事实） | 需跟进 |
|------|----------------|--------|
| 变更代码 **UT + API 100% 覆盖** | UT 已覆盖订单 Service 多条分支与 Controller 切片；**无** API 集成测试类；**无** Jacoco 报告，无法在 CI 中断言「100%」 | 见 §2、§3；**100% 范围**建议限定为「本 change 修改/新增的类与方法」，全 `backend` 另议 |
| **关键链路** 同时有 **UT & API** | 创建 / 取消 / 管理员改状态：**有 UT，无 API**；确认收货 / 列表筛选：**功能与测试均未落地**（见 `tasks.md` §1–§3） | 实现功能后补 **成对** UT（Service + WebMvc）与 **API**（JWT + DB） |
| **UI 冒烟** | 前端为 **Vue 3 + Vite**（`frontend/package.json`），**无**现成 E2E 依赖 | 见 §4，**已选定 Cypress**；待初始化与首条用例 |

### 2. 关键链路 UT ∧ API 矩阵（门禁对照）

以下为 **同一业务路径** 建议同时具备的测试类型（实现本 change 后须更新勾选）。

| 链路 | UT（Service 分支） | UT（Controller 映射/参数） | API（JWT + 真实 `/api` 前缀 + DB） |
|------|--------------------|----------------------------|--------------------------------------|
| 创建订单 | 已有 `OrderServiceTest` | 已有部分；缺 `GET /orders` 列表、`GET /orders/{id}` **不存在** 等 | **缺** |
| 取消订单 | 已有 | 已有部分 | **缺** |
| 管理员改状态 | 已有 `AdminServiceTest` | 已有 `AdminControllerOrderTest` | **缺** |
| **确认收货** `shipped→completed` | **待** `OrderServiceTest` | **待** `OrderControllerTest` | **待** |
| **列表筛选** `GET /orders?status=` | **待** `OrderServiceTest` / Repository | **待** `OrderControllerTest`（非法参数 400 等） | **待** |

**UT 缺口（现存代码、与 100% 目标相关）**：`OrderController.getUserOrders` 尚未有 WebMvc 用例；`getOrderDetail` 在订单不存在路径未单独覆盖；`AdminService.updateOrderStatus` 对 `status == null` 等行为可按设计补断言。

### 3. 「100% 覆盖」落地建议（避免口号无度量）

1. 在 `backend/pom.xml` 增加 **JaCoCo**，并对 **约定范围**（例如 `com.example.mall.service.OrderService`、`OrderController`、`AdminService` 中与订单相关方法及本 change 新增类）配置 `minimum` 规则或 `check` 目标。  
2. **API 测试**：`@SpringBootTest` + `@AutoConfigureMockMvc` + 测试 profile（独立 SQLite 文件或内存库 **待设计**），用 `JwtUtil` 签发 user/admin Token，请求前缀 **`/api`** 与 `application.yml` 中 `context-path` 对齐。  
3. 确认收货与筛选实现后，将 **AC-1～AC-7**（见 `tasks.md`）逐条映射到 **UT + API** 用例 ID，便于评审。

### 4. UI 冒烟框架（**已选定：Cypress**）

| 框架 | 与本项目匹配点 | 备注 |
|------|----------------|------|
| **Cypress（已选定）** | **Vue 3 + Vite** 常用；端到端文档成熟；`npx cypress run` 易进 CI | 建议在 `frontend/` 内使用 `cypress/e2e/`；`baseUrl` 指向本地 `vite dev` 或 `vite preview`，与后端 `/api` 代理或环境变量保持一致 |
| Playwright | 曾作备选 | **本 change 不双栈**；若日后迁移需另开变更 |

实施时补齐：`npm install cypress -D`（或项目选用的包管理器）、`cypress.config.*`（`baseUrl`）、首条用例：`登录` → `订单列表可见` → （可选）`详情路由可打开`。

### 5. 文档交叉引用

- `doc/analysis/订单链路-UT与API测试.md`：UT / API 定义与已落地 UT 清单。  
- 本 change 验收仍以 `tasks.md` 验收表 **AC-1～AC-7** 为准，并扩展 **测试映射列**（可在实施时在表格增加「UT 用例」「API 用例」列）。

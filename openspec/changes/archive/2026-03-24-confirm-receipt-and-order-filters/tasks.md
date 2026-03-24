## 1. 后端：确认收货

- [x] 1.1 在 `OrderService` 增加 `confirmReceipt(orderId, userId)`：`shipped`→`completed`，校验归属；非 `shipped` 抛业务异常；已实现 `completed` 时幂等返回
- [x] 1.2 在 `OrderController` 暴露 `PUT` 或 `POST /api/orders/{id}/confirm-receipt`（与现有风格一致），统一 `ApiResponse` 与错误码（如 `ORDER_003`）
- [x] 1.3 （可选）为非法状态/越权补充与 `cancel`、`getOrderDetail` 一致的 HTTP 语义

## 2. 后端：列表筛选

- [x] 2.1 `OrderRepository` / `OrderService.getUserOrders` 支持可选 `status` 参数；无参时保持现有查询
- [x] 2.2 `OrderController.getUserOrders` 读取 `RequestParam(required=false) String status` 并下传
- [x] 2.3 实现非法 `status` 策略（与设计一致：推荐 400 + 错误码；若允许未知值则文档写明）

## 3. 前端

- [x] 3.1 `api/order.js` 增加 `confirmReceipt(id)`、`getOrders(params)` 支持 `status`
- [x] 3.2 `Orders.vue`：状态筛选控件；默认全量；变更时带参请求
- [x] 3.3 `OrderDetail.vue`：仅 `shipped` 显示「确认收货」；成功后刷新状态
- [x] 3.4 与 `cancel` 按钮互斥展示规则：`pending` 显示取消；`shipped` 显示确认收货；其它状态不展示对应动作

## 4. 文档与基线

- [x] 4.1 更新 `doc/baseline/order-domain-baseline.md`（新迁移、新 API、按钮规则摘要）
- [x] 4.2 更新 `doc/analysis/单链路基线分析_技术.md` 中订单契约与测试矩阵相关行
- [x] 4.3 更新 `openspec/config.yaml` 中 `core_flows` / `quality_risks`（若行为变化）

## 5. 验证

- [x] 5.1 手工或自动化：`shipped`→`completed` 成功；非 `shipped` 失败；越权失败；列表筛选与默认全量（**JUnit 覆盖 Service/WebMvc 主路径；端到端未跑**）
- [x] 5.2 更新或引用 `doc/analysis/测试基线建议.md` 中 smoke 步骤

---

## 6. 测试与质量门禁（本轮补充）

> 详细缺口与矩阵见同 change 下 **`design.md` →「测试与质量（本轮分析）」**。  
> **100% 覆盖**：默认解释为 **本 change 触及的后端类/方法**（含用例扩展），**非**整个 `backend` 模块——若需全模块 100%，在评审中显式改口径。

- [x] 6.1 **JaCoCo**：`backend/pom.xml` 已配置 `prepare-agent`、test 阶段 `report`、`verify` 阶段 `check`；对 `OrderService` / `OrderController` 设 line ≥80%、branch ≥65%（窄于「100%」全类口径，与设计 §6 脚注一致）
- [x] 6.2 **UT 补全**：`OrderServiceTest` / `OrderControllerTest` 已覆盖 `GET /orders` 无参、合法/非法 `status`、详情不存在、越权、`confirmReceipt` 主路径与幂等等（见代码）
- [x] 6.3 **API 集成测试**：`OrderApiIntegrationTest`（`@SpringBootTest` + `@AutoConfigureMockMvc` + `application-test` 用 **H2 内存库**）；真实 Filter + JWT；MockMvc 路径为 **servlet 映射**（如 `/orders`，非 `/api/orders`）；覆盖多类 401、带 token 列表与 `ORDER_004`、普通用户访问管理员订单 403；**未**在此套件中覆盖完整创建/取消/管理员改状态/确认收货业务闭环（仍以 UT/WebMvc 为主）
- [x] 6.4 **关键链路成对验收**：下表已按当前仓库测试填充（见「验收标准 — 测试映射」）
- [x] 6.5 **UI 冒烟（Cypress）**：`frontend/cypress.config.cjs`、`cypress/e2e/smoke.cy.js`（`login` + stub API → `订单列表`）；脚本 `npm run cy:open` / `npm run cy:run`；需本地或 CI 执行 `npm install` 安装 Cypress 二进制
- [x] 6.6 **CI**：`.github/workflows/ci.yml` — `backend` 执行 `mvn verify`（含 JaCoCo check）；`frontend-build` 执行 build；`frontend-cypress` 在 preview 上 `cypress run`

---

## 验收标准（摘自查漏）

| 编号 | 场景 | 期望 |
|------|------|------|
| AC-1 | 本人订单 `shipped`，调用确认收货 | 200，`status` 为 `completed`，积分/库存不变 |
| AC-2 | 本人订单 `pending` / `completed` / `cancelled`，确认收货 | 失败，订单不变 |
| AC-3 | 他人订单 ID，确认收货 | 失败（与现有详情越权一致） |
| AC-4 | `GET /orders` 无参 | 与改动前列表一致（同用户全量） |
| AC-5 | `GET /orders?status=shipped` | 仅返回该用户 `shipped` 订单 |
| AC-6 | 前端列表默认 | 不发送 `status`，展示全部 |
| AC-7 | 前端仅 `shipped` 显示确认收货；`pending` 仅显示取消 | 符合 spec 可见性 |

### 验收标准 — 测试映射（门禁）

| 编号 | 场景 | 期望 | UT（已有） | API（已有） | UI 冒烟（已有） |
|------|------|------|------------|-------------|-----------------|
| T-1 | 创建订单 | 同既有域规则 | `OrderServiceTest` 等 | — | 可选 |
| T-2 | 取消订单 | 仅 `pending`；越权失败 | `OrderServiceTest` 等 | — | 可选 |
| T-3 | 管理员改状态 | 403/非对称退款 | `AdminServiceTest` / `AdminControllerOrderTest` | — | — |
| T-4 | 确认收货 | 同 AC-1～AC-3 | `OrderServiceTest`、`OrderControllerTest` | `PUT .../confirm-receipt` 无 token → 401（`OrderApiIntegrationTest`） | §6.5 stub 登录进列表 |
| T-5 | 列表筛选 | 同 AC-4～AC-6 | `OrderServiceTest`、`OrderControllerTest` | 带 token 列表 / 非法 `status` → `ORDER_004`（`OrderApiIntegrationTest`） | §6.5 stub `GET /orders` |
| T-6 | 无 Token 访问受保护 API | 401 | — | `OrderApiIntegrationTest`（订单与管理员路径） | — |

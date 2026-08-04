## Context

福利商城（Spring Boot + Vue 3）已有 `OrderService.createOrder`（含 **addressId 必填 + 归属 + 软删校验**）、`cancelOrder`、地址 CRUD 与订单列表/详情。

**仍缺：**

- 前端 `/orders/create` 路由与 `Checkout.vue`
- `Cart.vue` 勾选未调用后端
- `PUT /api/cart/{id}/select` API
- `OrderDetail.vue` 展示 `remark`（AC-13）
- `OrderController` 对空字符串/非法 `addressId` 的规范化

L1 基线：`product/baseline/project-baseline.md`；L2 原型：`product/order.html`（visual_level **L2**）。订单状态 **仅由服务端** 计算与持久化（PRD §3.3）。

**相对原型的 intentional 分歧（PRD 优先）：**

| 原型 | 实现 |
|------|------|
| 成功 modal 终态 | 跳 `/orders/{id}`（D4） |
| 本地 `ORD+timestamp` | 禁止（N-04） |
| 「积分已冻结」 | 禁止（N-04 延伸）；Orders/Detail 不得出现 |
| `demo-bar` | 禁止（N-07） |
| 提交前本地库存/积分拦截 | 仅预览；提交以服务端为准（D7） |

## Goals / Non-Goals

**Goals:**

- 闭环：购物车勾选 → 结算页 → 选地址 → 提交 → `pending` → 详情/列表 → 取消（走查含取消）
- 前端 L2 对齐原型关键区块 + 响应式单列（≤860px）
- 验收：**PRD AC-01~AC-15**（AC-09/T9 defer）、T1–T8 + **T10 E2E**

**Non-Goals（含 PRD §3.5 MUST NOT 摘要）：**

| ID | 约束 |
|----|------|
| N-01 | 真实支付 |
| N-02 | 优惠券/满减 |
| N-03 | 拆单 |
| N-04 | 前端伪下单/伪造状态 |
| N-05 | 前端写 `orders.status` |
| N-06 | 取消非 pending |
| N-07 | 生产页 demo-bar |
| N-08 | 无地址静默下单 |
| N-09 | 越权 addressId |
| N-10 | 管理端物流/售后用户路径 |

- 结算页「新增地址」（O-01 → Profile P2）；空态 **不得** 可点新增
- L3/L4 像素级、T9 并发锁、下单幂等
- `status=active` / `balanceAfter` 修正 / OrderDetail 取消按钮（tasks §7）

## Decisions

### D1 · 结算页路由与组件

- **决策**：`Checkout.vue` 包裹于 `Layout.vue`，`/orders/create`，`meta.requiresAuth: true`
- **备选**：Modal 结算 — 拒绝

### D2 · 购物车勾选 API

- **决策**：`PUT /api/cart/{id}/select` body `{ "isSelected": 0|1 }`，校验 cart 归属；失败 **CART_001**
- **拦截**：Cart 无选中时 **必须** 阻止进结算；deep link 无选中时提示并引导回 `/cart`

### D3 · 地址校验（后端）— 已实施

| 条件 | HTTP | message |
|------|------|---------|
| `addressId == null` / 缺失 / 空串 / 非法数字 | ORDER_001 | `请选择收货地址`（Controller 规范化，见 D9） |
| 不存在 / 已软删 / `userId` 不匹配 | ORDER_001 | `收货地址不存在` |

- **前端**：未选地址可见提示（可与后端逐字不同，如「请选择收货地址后再提交订单」）

### D4 · 成功跳转

- **决策**：`router.push('/orders/' + order.id)`（PRD O-02）
- **禁止**：Checkout 本地点亮步骤③终态；禁止成功 modal 代替跳转

### D5 · 视觉实现（L2）

- Design Token：`#e4393c`、卡片、步骤条、error-banner
- **必选**：步骤条；**顶栏「可用积分」**（对齐原型 header）；地址 **默认 tag**；商品 **图/占位**；侧栏 **件数 / 商品积分 / 合计** + pointsGap
- **响应式**：`max-width: 860px` 单列（PRD §4.0）
- **不含**：`demo-bar`

### D6 · 错误展示

- **决策**：提交失败 **必须** error-banner **且必须** modal 双通道（PRD §4.1.6 / 原型）
- 地址类错误：**必须** scroll 至地址区
- 展示服务端 `message`；禁止仅 console

### D7 · 前端预校验与 stockWarn 数据流

- **加载时**：对 `is_selected=1` 的 cart 项 **逐条或批量** `GET /products/{id}` 取 `stock`，计算 stockWarn；展示 pointsGap — **只读，不阻塞提交**
- **提交时**：地址可前端拦截；库存/积分以服务端 ORDER_001 为准

### D8 · 用户积分数据来源

- **必须**：Checkout 加载调用 `GET /api/user/info` 取 `points`；`localStorage.userInfo.points` 仅作首屏占位，提交前刷新

### D9 · OrderController addressId 入参

- **决策**：`addressId` 缺失、null、空串、非数字 → 视为未提供，返回 ORDER_001「请选择收货地址」，避免 `Long.valueOf` 未包装异常

## Risks / Trade-offs

| 风险 | 缓解 |
|------|------|
| stockWarn N+1 请求 | 复用 Cart.vue 模式；工作坊可接受 |
| 无 addressId 旧客户端 | BREAKING；§2.4 规范化 |
| T9 并发 | tasks §5.5 defer |
| cancel `balanceAfter` 不准 | §7.2 defer；集成测试不断言 balanceAfter |
| Cart 紫色 vs L2 红 | tasks §7.4 P2 |

## Migration Plan

1. ~~地址校验 Service 层~~ ✅
2. §2.4 Controller 入参 + cart select API
3. Checkout + OrderDetail remark
4. 测试 T1–T8 + E2E T10 + AC-13/14
5. 走查 + compass

## Open Questions

| # | 问题 | 状态 |
|---|------|------|
| O-01 | 新增地址 | ✅ defer Profile P2 |
| O-02 | 成功跳转 | ✅ 订单详情 |
| O-04 | coverage | `OrderService`、`CartService`、`Checkout.vue` |

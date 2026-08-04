# order-checkout-hardening — Superpowers Spec

> **Source of truth:** `openspec/changes/order-checkout-hardening/` · `product/PRD-order-checkout.md` · `product/order.html`  
> **Change ID:** `order-checkout-hardening` · **Workshop step:** L3-3  
> **Status (2026-08-04):** §2.1–2.3 ✅ · 其余 ⬜

---

## Goal

打通福利商城 **购物车勾选 → 结算页 → 选地址 → 下单 → 订单详情/列表 → 取消** 的用户闭环；服务端为订单状态与扣减唯一权威；前端 L2 对齐原型关键区块。

## Non-Goals（MVP 禁止）

| ID | 约束 |
|----|------|
| N-01 | 真实支付 |
| N-02 | 优惠券/满减 |
| N-03 | 拆单 |
| N-04 | 前端伪下单 / 本地伪造 `orderNo` / status |
| N-05 | 前端写 `orders.status` |
| N-06 | 取消非 `pending` 订单 |
| N-07 | 生产页 `demo-bar` |
| N-08 | 无地址静默下单 |
| N-09 | 越权 `addressId` |
| N-10 | 管理端物流/售后用户路径 |

**Defer（tasks §7，不阻塞 apply）：** 商品 `status=active` 校验 · `balanceAfter` 修正 · OrderDetail 取消按钮 · Cart L2 配色统一 · T9 并发

---

## File Map（实施范围）

| 文件 | 职责 |
|------|------|
| `backend/.../service/CartService.java` | `selectCartItem` 勾选持久化 + 归属校验 |
| `backend/.../controller/CartController.java` | `PUT /cart/{id}/select` |
| `backend/.../controller/OrderController.java` | `addressId` 入参规范化（D9） |
| `backend/.../service/OrderService.java` | 下单/取消（§2.1–2.3 已完成） |
| `backend/src/test/...` | 集成测试 T1–T8 |
| `frontend/src/api/cart.js` | `selectCartItem` |
| `frontend/src/views/Cart.vue` | 勾选 API + 结算守卫 |
| `frontend/src/views/Checkout.vue` | **新建** 结算页 L2 |
| `frontend/src/router/index.js` | `/orders/create` 路由 |
| `frontend/src/views/OrderDetail.vue` | 展示 `remark` |
| `product/compass-order-checkout.md` | 走查后更新 QG |

---

## Capability 1 · cart-selection

### Requirement: 购物车勾选状态持久化

`PUT /api/cart/{id}/select` body `{ "isSelected": 0|1 }`；仅允许操作当前用户 cart；失败 **CART_001**。

| Scenario | WHEN | THEN |
|----------|------|------|
| 勾选 | 归属 cart + `isSelected=1` | 持久化选中；summary 计入 |
| 取消勾选 | 归属 cart + `isSelected=0` | 持久化未选；summary 排除 |
| 越权 | cart.userId ≠ 当前用户 | CART_001，不修改 |
| 不存在 | cart id 无效 | CART_001 |

### Frontend · Cart.vue

- `toggleSelect` 调用 `cartApi.selectCartItem`
- 无选中商品时点击「结算」**阻止跳转**并提示

---

## Capability 2 · order-checkout-create

### Requirement: 提交订单创建 pending

| Scenario | WHEN | THEN |
|----------|------|------|
| 正常下单 | 有选中项、库存足、积分足、有效 addressId | `status=pending`；扣积分/库存；删已选 cart |
| 无选中 | 无 `is_selected=1` | ORDER_001「购物车中没有选中的商品」 |
| 商品不存在 | productId 无效 | ORDER_001「商品不存在」 |
| 库存不足 | stock < quantity | ORDER_001 含商品名 + 库存不足 |
| 积分不足 | points < total | ORDER_001 含当前/所需积分 |

### Requirement: 收货地址必填且归属当前用户

| 条件 | code | message |
|------|------|---------|
| 缺失 / null / 空串 / 非法数字（Controller D9） | ORDER_001 | 请选择收货地址 |
| 不存在 / 软删 / userId 不匹配 | ORDER_001 | 收货地址不存在 |

**已实施（Service）：** §2.1–2.3 ✅  
**待实施（Controller）：** §2.4 D9 入参规范化 ⬜

### Requirement: 订单地址快照

成功下单后 `receiver`、`receiverPhone`、`receiverAddress` 与地址记录一致（已实现）。

---

## Capability 3 · order-checkout-ui

### Requirement: 结算页路由与鉴权

- 路由 `/orders/create` → `Checkout.vue`，`meta.requiresAuth: true`，包裹 `Layout`
- 未登录 → 重定向 `/login`

### Requirement: L2 关键区块（PRD §4.0 · `product/order.html`）

必选：步骤条（① done → ② active → ③ 灰）· **顶栏可用积分** · 地址单选 + **默认 tag** · 商品图/占位 + **stockWarn** · 备注 ≤100 · 侧栏 **件数/小计/合计** + **pointsGap** · 提交按钮 · `max-width: 860px` 单列 · **无 demo-bar**

### Requirement: 加载时积分与库存预览（D7/D8）

- `GET /api/user/info` 取积分（必须；localStorage 仅首屏占位）
- `GET /api/cart` → filter `isSelected === 1`
- 对选中项 `GET /api/products/{id}` 取 `stock` 算 stockWarn（N+1 可接受）
- stockWarn / pointsGap **只读预览，不阻塞提交**

### Requirement: 地址选择

- `GET /api/addresses`；`isDefault=1` 默认选中
- 空地址：提示引导，**不得** 提供「新增地址」跳转（O-01 defer）
- deep link 无选中 / cart 空 → 提示并引导 `/cart`

### Requirement: 提交与错误（D4/D6）

- `POST /api/orders` `{ addressId, remark? }`
- 成功 → `router.push('/orders/' + order.id)`（禁止成功 modal 终态）
- 失败 → **必须** error-banner **且必须** modal，展示服务端 `message`
- 未选地址 → 前端拦截 + scroll 至地址区
- 全站禁止「积分已冻结」文案

### Requirement: 备注透传（AC-13）

`OrderDetail.vue` 展示 `order.remark`；无 remark 时隐藏或显示「无」

---

## Capability 4 · order-checkout-cancel（spec-only）

`OrderService.cancelOrder` + `PUT /orders/{id}/cancel` **已实现**；本 change 仅规格化 + 测试覆盖。

| Scenario | THEN |
|----------|------|
| pending 取消 | `cancelled`；退积分；回滚库存；refund 积分历史 |
| 非 pending | 拒绝；状态/积分/库存不变 |
| 非所属用户 | ORDER_002；状态不变 |

**测试约束：** 不断言 `PointsHistory.balanceAfter` 精确值（已知债 §7.2 defer）

---

## AC / Test Traceability

| AC | 描述 | 验证 |
|----|------|------|
| AC-01 | 正常下单 pending | T1 集成 |
| AC-02 | 无选中商品 | T2 |
| AC-03 | 库存不足 | T3 |
| AC-04 | 积分不足 | T4 |
| AC-05 | addressId 缺失/空 | T5 |
| AC-06 | 地址越权/软删/不存在 | T6 |
| AC-07 | 取消 pending | T7 |
| AC-08 | 拒绝非 pending 取消 | T8 |
| AC-09 | 并发 | **Defer T9** |
| AC-10 | E2E 闭环 + 异常双通道 | T10 手工/E2E |
| AC-11 | L2 目视 | §6.1 走查 |
| AC-13 | remark 详情展示 | Task OrderDetail + T10 |
| AC-14 | status 来自 API | §6.3 冒烟 |
| AC-15 | 禁止冻结文案 | §6.1 grep |

---

## Design Decisions（冻结）

| ID | 决策 |
|----|------|
| D1 | `Checkout.vue` + `/orders/create` + Layout |
| D2 | `PUT /cart/{id}/select` + Cart 守卫 |
| D3 | 地址校验表（见上） |
| D4 | 成功跳详情，非 modal |
| D5 | L2 token `#e4393c`、860px 单列 |
| D6 | 失败 banner + modal 双通道 |
| D7 | stockWarn 来自 products API；提交以服务端为准 |
| D8 | 积分必须 `GET /user/info` |
| D9 | Controller 规范化 addressId |

---

## API Quick Reference

| Method | Path | Body |
|--------|------|------|
| PUT | `/api/cart/{id}/select` | `{ isSelected: 0\|1 }` |
| POST | `/api/orders` | `{ addressId, remark? }` |
| GET | `/api/addresses` | — |
| GET | `/api/cart` | — |
| GET | `/api/user/info` | — |
| GET | `/api/products/{id}` | — |
| PUT | `/api/orders/{id}/cancel` | — |

---

## Seed Data（手工/E2E）

| 用户 | 密码 | 积分 |
|------|------|------|
| user1 | password1 | 100 |
| user2 | password2 | 200 |
| user3 | password3 | 150 |

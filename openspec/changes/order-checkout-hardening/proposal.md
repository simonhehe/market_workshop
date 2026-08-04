## Why

福利商城的下单结算链路仍缺少可演示的前端闭环：购物车勾选未接通、无结算页路由与 UI。后端 `OrderService.createOrder` 的 **addressId 必填 + 归属校验已实现**（tasks §2.1–2.3），其余阻塞在 Checkout 页与 cart select API。本变更依据 [`product/PRD-order-checkout.md`](../../../product/PRD-order-checkout.md) 与 L2 原型 [`product/order.html`](../../../product/order.html) 补齐中等 MVP 闭环。

## What Changes

- 新增前端结算页 `Checkout.vue`（复用 `Layout.vue`）及路由 `/orders/create`（L2 视觉对齐原型关键区块）
- 接通购物车勾选：新增 `PUT /api/cart/{id}/select`，`Cart.vue` 持久化 `is_selected`
- ~~加固 addressId 校验~~ **（已完成）** `OrderService.createOrder` 已强制 addressId 必填、归属与软删校验；`OrderController` 须规范化空/非法 `addressId`（tasks §2.4）
- 结算页集成 `GET /addresses`、`GET /cart` + 商品详情（stock）、**必须** `GET /user/info`（积分）、`POST /orders`；成功跳转订单详情；失败 **banner + modal 双通道**（PRD §4.1.6）
- `OrderDetail.vue` 展示 `remark`（AC-13）
- 负向约束对齐 PRD §3.5 N-01~N-10（见 design Non-Goals）
- 明确 **不** 引入：真实支付、优惠券、拆单、管理端结算功能、结算页「新增地址」入口（PRD O-01 defer）

**不在 MVP apply 阻塞范围（tasks §7）：**

- 商品 `status=active` 校验
- `PointsHistory.balanceAfter` 修正（cancel/create 已知债；测试勿误判 refund 记录）
- 并发锁 / T9

## Capabilities

OpenSpec capability 命名统一 **kebab-case**（与 PRD §7.11 追溯表一致）。

### New Capabilities

- `order-checkout-ui`：结算页 UI、路由、积分预览、错误态、成功后跳转（PRD AC-10~AC-15、M1/M2）
- `order-checkout-create`：提交订单服务端校验与副作用（PRD AC-01~AC-06、M3）；**address 校验已实现**
- `cart-selection`：购物车勾选 API 与前端接通（PRD AC-15、M1）
- `order-checkout-cancel`：**规格化既有实现**（本 change 仅 spec + 集成测试；`balanceAfter` 精度见 §7.2 defer）

### Modified Capabilities

<!-- 无既有 openspec/specs -->

## Impact

- **前端**：`Checkout.vue`（新建，Layout 包裹）、`OrderDetail.vue`（remark）、`Cart.vue`、`router/index.js`、`api/cart.js`；**必须** `userApi.getUserInfo()` 展示积分
- **后端**：`CartController`/`CartService`（toggle select，失败 `CART_001`）；`OrderController`（addressId 入参规范化）
- **API**：新增 `PUT /api/cart/{id}/select`；`POST /api/orders` 对无/非法 addressId **BREAKING**
- **测试**：T1–T8 集成 + T10 E2E；AC-11~AC-15 走查/E2E；T9 defer
- **回归**：AC-14 订单列表/详情 status 只读；Orders/Detail 不得出现「积分已冻结」
- **参考**：[`product/compass-order-checkout.md`](../../../product/compass-order-checkout.md)

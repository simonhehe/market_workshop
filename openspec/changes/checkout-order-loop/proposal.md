# 变更提案：购物车 → 结算 → 订单闭环

## Why

福利商城已有商品、购物车、地址、订单实体与基础 API，但用户无法完成一次完整的积分兑换：前端缺少 `/orders/create` 结算页，购物车勾选只改本地状态，`createOrder` 不校验地址归属与商品上架状态，错误统一返回 `ORDER_001` 无法区分原因，积分流水 `balanceAfter` 在消费与退款时均计算错误。PRD `PRD-order-checkout.md` 要求交付中等 MVP 闭环，并以服务端作为订单状态、积分与库存的唯一计算方。

## What Changes

- **购物车勾选**：新增 `PUT /api/cart/{id}/select`；勾选状态持久化；数量修改、删除与勾选均校验归属；加购与改数量按累计数量校验库存。
- **结算页**：新增前端路由 `/orders/create` 与 `Checkout.vue`；展示 mock 地址（显式选择、不预选）、选中商品、备注、积分合计；数量联动、提交预检、防重复提交、错误就近展示、成功页展示服务端结果。
- **下单校验与落库**：`POST /api/orders` 要求 `addressId` 必填且归属当前用户；按固定优先级校验选中项、地址、商品 active、库存、积分；在同一事务内创建订单/快照、扣积分/库存、写流水、清选中项；忽略客户端传入的合计/状态/积分。
- **订单状态与取消**：创建强制 `pending`；本人且 pending 才能取消；取消返还积分、恢复库存、写 refund 流水；错误键 `ORDER_FORBIDDEN` / `ORDER_STATUS_INVALID`。
- **积分流水修复**：spend 与 refund 的 `balanceAfter` 均等于本次变动后的真实余额。
- **错误契约**：以 `ApiResponse.error(code, message)` 返回 PRD §4.5 业务错误键（`ADDR_REQUIRED`、`ADDR_NOT_FOUND`、`CART_EMPTY`、`PRODUCT_UNAVAILABLE`、`STOCK_INSUFFICIENT`、`POINTS_INSUFFICIENT`、`ORDER_STATUS_INVALID`、`ORDER_FORBIDDEN`、`CART_ITEM_FORBIDDEN`）。
- **mock 地址种子**：为 `user1` 幂等预置 2 条归属本人的地址，id 稳定，仅用于选择。
- **测试**：后端 JUnit5 + MockMvc 集成测试；前端 Vitest + Vue Test Utils 组件测试；不新增业务 E2E 脚本。

## Impact

- 影响能力：`cart-selection`、`checkout`、`order-create`、`order-lifecycle`、`points-ledger`。
- 影响代码：
  - 后端：`OrderService`、`OrderController`、`CartService`、`CartController`、`DataInitializer`、新增 `BusinessException`、`Order` 订单号生成。
  - 前端：新增 `views/Checkout.vue`、路由、`api/cart.js`、`Cart.vue`、`Orders.vue`/`OrderDetail.vue`（状态映射与权限展示）、`api/exports.js` 无需变更。
- 数据：不新增表/字段；`Order.addressId` 仍可空，由应用层强制；部署前备份 `backend/database/mall.db`；`ddl-auto=update` 只新增种子数据。
- 非目标：真实支付、优惠券、拆单、售后、管理端结算、地址 CRUD UI、并发锁与幂等键。

## Risks

- SQLite 不支持 Hibernate 6 通过 `alter table` 追加唯一约束，测试库会打印非致命告警；业务唯一性由服务层查询保证。
- 当前无并发锁，极端并发下仍可能超卖/双花（PRD N10，本期不声称解决）。
- 前端积分展示为接口返回的当前余额 + 选中项估算合计，最终扣减以服务端为准。

# 实施规格：下单结算闭环（checkout-order-loop）

## Goal

在存量福利商城中交付可演示的积分兑换闭环：购物车勾选 → 结算页选择地址 → 服务端校验并下单 → 订单列表/详情状态正确 → 取消 pending 订单并一致返还积分与库存。

## Non-Goals

- 真实支付、优惠券、拆单、售后、管理端结算。
- 地址新增/编辑/删除 UI。
- 并发锁、幂等键、超卖/双花治理。
- 业务 E2E 自动化脚本；L4 采用启动走查与只读冒烟。

## 用户可见验收

| 能力 | 验收 |
|---|---|
| 勾选 | 勾选持久化，合计只统计选中项；空选中不能结算 |
| 结算页 | `/orders/create` 展示地址、商品、合计；不预选地址；数量联动；错误就近提示；提交防重；成功页展示服务端结果 |
| 校验 | 未选地址、他人地址、空购物车、下架、库存不足、积分不足均有明确错误且无副作用 |
| 下单 | 服务端计算合计、强制 pending、写快照、扣积分/库存、写 spend 流水、清选中项 |
| 取消 | 仅本人 pending；refund 流水 balanceAfter 正确；库存恢复 |
| 状态 | 列表/详情仅映射服务端状态；取消入口仅 pending |

## File Map

### 后端

- `backend/src/main/java/com/example/mall/common/BusinessException.java`（新增）
- `backend/src/main/java/com/example/mall/service/OrderService.java`（改造）
- `backend/src/main/java/com/example/mall/controller/OrderController.java`（错误键）
- `backend/src/main/java/com/example/mall/service/CartService.java`（勾选/归属/累计库存）
- `backend/src/main/java/com/example/mall/controller/CartController.java`（select + userId）
- `backend/src/main/java/com/example/mall/entity/Order.java`（订单号）
- `backend/src/main/java/com/example/mall/config/DataInitializer.java`（mock 地址）
- `backend/src/test/java/com/example/mall/service/OrderServiceIntegrationTest.java`（新增）
- `backend/src/test/java/com/example/mall/service/CartServiceIntegrationTest.java`（新增）
- `backend/src/test/java/com/example/mall/controller/OrderControllerTest.java`（新增）
- `backend/src/test/java/com/example/mall/controller/CartControllerTest.java`（新增）
- `backend/src/test/java/com/example/mall/config/DataInitializerTest.java`（新增）

### 前端

- `frontend/src/views/Checkout.vue`（新增）
- `frontend/src/router/index.js`（新增路由）
- `frontend/src/api/cart.js`（selectItem）
- `frontend/src/views/Cart.vue`（勾选接通、空选中提示）
- `frontend/src/views/Orders.vue`（保持状态映射/取消入口）
- `frontend/src/views/OrderDetail.vue`（pending 取消入口）
- `frontend/src/views/__tests__/Checkout.spec.js`（新增）
- `frontend/src/views/__tests__/Cart.spec.js`（新增）
- `frontend/src/views/__tests__/Orders.spec.js`（新增）
- `frontend/src/views/__tests__/OrderDetail.spec.js`（新增）

## 架构要点

- 后端 `OrderService` 为唯一计算方；校验阶段只读，写阶段单事务。
- 错误通过 `BusinessException(code, message)` 传播，Controller 统一转为 `ApiResponse.error`。
- 前端不发送合计/状态；提交仅传 `addressId` 与 `remark`。
- 前端展示值由选中项与商品数据估算，服务端返回值为准。

## 验收命令

```bash
cd backend && mvn -B test
cd frontend && npm test && npm run build
```

## 风险

- SQLite 唯一约束 DDL 告警为非致命，业务唯一性由服务层与种子幂等保证。
- 无并发锁；PRD 已将超卖/双花列为 P2 已知风险，不声称解决。

## 1. 后端 · 购物车勾选 API



- [x] 1.1 在 `CartController` 新增 `PUT /api/cart/{id}/select`，body `{ isSelected: 0|1 }`；失败返回 **CART_001**

- [x] 1.2 在 `CartService` 实现勾选逻辑：校验 cart 归属 `userId`，更新 `is_selected`

- [x] 1.3 在 `frontend/src/api/cart.js` 新增 `selectCartItem(id, isSelected)` 方法



## 2. 后端 · 下单地址校验（order-checkout-create）



- [x] 2.1 `OrderService.createOrder`：`addressId == null` 时抛出「请选择收货地址」

- [x] 2.2 校验地址存在、未软删且 `address.userId` 等于当前 `userId`，否则「收货地址不存在」（AC-06）

- [x] 2.3 确认 `OrderController` 仍将业务异常映射为 ORDER_001 + message

- [x] 2.4 `OrderController.createOrder`：`addressId` 缺失/空串/非法 → ORDER_001「请选择收货地址」（D9 / R2-C01）



## 3. 前端 · 购物车接通（cart-selection）



- [x] 3.1 `Cart.vue`：`toggleSelect` 调用 `cartApi.selectCartItem` 后刷新 cart

- [x] 3.2 `Cart.vue`：无选中商品时点击结算 **阻止跳转** 并提示



## 4. 前端 · 结算页（order-checkout-ui）



- [x] 4.1 新建 `Checkout.vue`（**Layout 包裹**）：步骤条、**顶栏可用积分**、地址单选+**默认 tag**、商品**图/占位**+stockWarn、备注 maxlength=100、侧栏**件数/商品积分/合计**+pointsGap、error-banner+**modal**；L2 token；860px 单列；无 demo-bar

- [x] 4.2 `router/index.js` 注册 `/orders/create` → `Checkout.vue`，`requiresAuth: true`

- [x] 4.3 加载 `GET /addresses`、`GET /cart` 并 **filter is_selected=1**；对选中项 **GET /products/{id}** 取 stock 算 stockWarn；**必须** `GET /user/info` 积分；默认地址 `isDefault=1`

- [x] 4.4 提交 `POST /orders` `{ addressId, remark }`；成功 `router.push('/orders/{id}')`（禁止成功 modal 终态）

- [x] 4.5 失败：**必须** banner **且必须** modal；未选地址拦截+scroll 地址区；deep link 无选中引导 `/cart`

- [x] 4.6 `OrderDetail.vue` 展示 `order.remark`（AC-13；无 remark 时隐藏或显示「无」）



## 5. 测试 · 最小集



- [x] 5.1 集成测试：正常下单 pending + 积分/库存/cart 副作用（T1 / AC-01）

- [x] 5.2 集成测试：无选中、库存不足、积分不足、商品不存在（T2–T4 / AC-02–04）

- [x] 5.3 集成测试：addressId 缺失/空串、越权/软删/不存在地址（T5–T6 / AC-05–06）

- [x] 5.4 集成测试：取消 pending / 拒绝非 pending（T7–T8；**不断言 balanceAfter 精确值**）

- [x] 5.4b `CartSelectionIntegrationTest`：勾选/取消勾选/summary/越权/不存在（cart-selection spec）

- [ ] 5.5 **Defer** 并发 T9 / AC-09（compass 记录）

- [ ] 5.6 E2E：选地址→提交→详情 pending+remark；≥1 异常 banner+modal（T10 / AC-10）



## 6. 走查与文档



- [ ] 6.1 启服走查：含 AC-11 L2 目视、取消闭环；Orders/Detail **无「积分已冻结」**

- [x] 6.2 更新 `product/compass-order-checkout.md`

- [ ] 6.3 冒烟 AC-14：列表/详情 status 来自 API



## 7. 可选（P1/P2，**不在 MVP apply 阻塞范围**）



- [ ] 7.1 `createOrder` 校验商品 `status=active`

- [ ] 7.2 修正 `PointsHistory.balanceAfter`（create/cancel）

- [ ] 7.3 `OrderDetail.vue` 增加取消按钮

- [ ] 7.4 Cart 页 L2 token 统一


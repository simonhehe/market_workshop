# 技术设计：购物车 → 结算 → 订单闭环

## 1. 总体思路

复用既有 Spring Boot + Vue 架构，不新增依赖、不新增表。后端以 `OrderService` 事务方法为唯一业务计算方；前端只负责展示、交互预检与错误映射。

## 2. 后端设计

### 2.1 业务异常

新增 `com.example.mall.common.BusinessException extends RuntimeException`，携带 `code` 字段。业务错误键以 PRD §4.5 为准；另有资源缺失/入参校验的内部错误键（`USER_NOT_FOUND`、`ORDER_NOT_FOUND`、`CART_SELECTION_INVALID`），不替代九类业务错误键。Controller 捕获后返回：

```json
{
  "success": false,
  "message": "……",
  "error": { "code": "STOCK_INSUFFICIENT", "message": "……" },
  "timestamp": "……"
}
```

HTTP 状态沿用现有约定：业务校验失败返回 400；未登录由 `AuthMiddleware` 返回 401。

### 2.2 下单校验顺序（与 PRD §3.2.1 一致）

`OrderService.createOrder(userId, addressId, remark)`：

1. 读取当前用户选中购物车项；为空 → `CART_EMPTY`。
2. `addressId == null` → `ADDR_REQUIRED`；地址不存在、已删除或不属于本人 → `ADDR_NOT_FOUND`。
3. 逐项加载商品；不存在或已删除 → `PRODUCT_UNAVAILABLE`（通用提示）；`status != active` → `PRODUCT_UNAVAILABLE`（提示含商品名）。
4. 逐项校验 `stock >= quantity` → 否则 `STOCK_INSUFFICIENT`（消息含商品名、剩余量与需求量）。
5. 计算 `totalPoints = Σ pointsRequired × quantity`；用户不存在 → `USER_NOT_FOUND`；余额不足 → `POINTS_INSUFFICIENT`（消息含当前积分、应付、差额）。
6. 事务内写订单（强制 `pending`、地址快照）、订单项快照、扣积分、扣库存/加销量、写 spend 流水、删除选中购物车项。

校验阶段只读，任何失败均不产生写入；写阶段异常由 `@Transactional` 整体回滚。

### 2.3 取消订单

`cancelOrder(orderId, userId)`：

1. 订单不存在 → `ORDER_NOT_FOUND`。
2. 非本人 → `ORDER_FORBIDDEN`（先于状态校验，避免泄露他人订单状态）。
3. 非 `pending` → `ORDER_STATUS_INVALID`。
4. 返还积分、写 refund 流水（`balanceAfter` = 返还后余额）、恢复库存并回减 sales（下限 0）、置 `cancelled`。

### 2.4 购物车

- `PUT /api/cart/{id}/select` body `{"isSelected":0|1}`，返回更新后的购物车项。
- `selectItem(cartId, userId, isSelected)`：归属校验后，`isSelected` 仅允许 0/1，其余 → `CART_SELECTION_INVALID`（入参校验，不属于 PRD 九类业务错误）。
- `updateQuantity(cartId, userId, quantity)`：归属校验 `CART_ITEM_FORBIDDEN`；`quantity <= 0` 删除；`quantity > stock` → `STOCK_INSUFFICIENT`。
- `removeFromCart(cartId, userId)`：归属校验 `CART_ITEM_FORBIDDEN`。
- `addToCart`：同一商品累计数量（已有数量 + 本次数量）不得超过库存 → `STOCK_INSUFFICIENT`。
- Controller 的 PUT/DELETE 增加 `@RequestAttribute("userId")`。

### 2.5 订单号

`Order.@PrePersist` 生成 `ORD + 毫秒时间戳 + 4 位随机数`，降低同毫秒碰撞概率；`order_no` 唯一约束仍在数据库层兜底。

### 2.6 mock 地址

`DataInitializer` 增加地址种子：存在 `user1` 且其未删除地址为空时写入 2 条；重复执行不重复插入。地址 id 由数据库自增，同一数据库内稳定。

## 3. 前端设计

### 3.1 路由与页面

- 新增 `/orders/create`，`meta.requiresAuth`，组件 `Checkout.vue`。
- 页面加载并行请求：购物车、地址列表、用户信息（当前积分）。
- 过滤 `isSelected === 1`；为空时 `router.replace({ path: '/cart', query: { notice: 'empty' } })`，购物车页展示提示。

### 3.2 状态与计算

- `addresses`、`selectedAddressId`（初始 `null`，不预选）。
- `items`：购物车项 + 商品信息（名称、图片、单价、库存）。
- 前端仅计算展示值：小计、件数、商品合计、运费 0、应付积分、下单后余额；提交时不发送任何合计字段。
- 数量步进限制 `1..stock`；库存为 0 或不足时行内标记并阻止提交。

### 3.3 提交与错误

- `submitting` 为真时禁用按钮并直接返回。
- 预检顺序：未选地址 → 库存不足 → 积分不足；预检失败显示消息且不调用 API。
- API 失败读取 `err.response?.data?.error?.code`，映射 PRD 文案；地址类错误滚动/聚焦地址区，商品类错误标记行，其余显示全局横幅。
- 成功：清空购物车选中项、展示成功卡片（服务端 `orderNo`、`totalPoints`、状态「待处理」），提供订单详情/继续购物入口。

### 3.4 订单展示

- `Orders.vue` / `OrderDetail.vue` 继续仅映射服务端状态；取消按钮仅 `status === 'pending'`。
- `OrderDetail.vue` 增加 pending 时取消入口，成功后刷新详情。

### 3.5 购物车勾选

- `cartApi.selectItem(id, isSelected)`。
- `Cart.vue` 勾选调用接口，成功后重新加载；失败回滚勾选并提示。
- 未选中任何商品点击结算：展示「请先选择要兑换的商品」，不跳转。

## 4. 测试设计

- 后端：`@SpringBootTest` + 测试 SQLite 集成测试覆盖校验顺序、快照、扣减、流水、回滚、取消、归属；`MockMvc` 独立控制器测试覆盖错误码与客户端伪造字段被忽略。
- 前端：Vitest 挂载组件并 mock API，覆盖结算页加载/地址/数量/预检/失败/成功/防重、购物车勾选与空选中、订单状态文案与取消入口。
- 不使用业务 E2E 脚本；L4 通过服务启动与只读冒烟验证。

## 5. 兼容与回滚

- API 路径保持向后兼容；`PUT /api/cart/{id}` 与 `DELETE /api/cart/{id}` 仅新增归属校验（越权由允许变为拒绝，属安全修复）。
- 错误码从 `ORDER_001` 细化为业务键，前端已按新键映射。
- 回滚：回退提交并恢复部署前 `mall.db` 备份；无 schema 变更。

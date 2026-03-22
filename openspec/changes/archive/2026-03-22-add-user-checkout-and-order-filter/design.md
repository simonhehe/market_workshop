# Design: 用户结算下单与订单状态筛选

## 1. 用户旅程

```text
购物车 Cart → [结算] → 结算页 Checkout → 选择地址（可选）/ 备注 → [提交订单]
    → POST /orders 成功 → 跳转 /orders/:id 或 /orders?status=pending
```

- 结算前校验：`summary.selectedCount > 0` 且 `totalPoints` 不超过用户当前积分（前端预检 + 后端已有校验）。
- 若购物车无选中项：结算按钮禁用或提示「请选择要结算的商品」。

## 2. API 变更

### 2.1 订单列表筛选（后端）

| 方法 | 路径 | 变更 |
|------|------|------|
| GET | `/orders` | 新增可选 Query：`status` |

**语义**：

- 省略 `status` 或与 `status=all`：返回当前用户全部未删订单（与现行为一致）。
- `status=pending|processing|shipped|completed|cancelled`：仅返回该状态（`deleted_at IS NULL`）。

**实现提示**：`OrderRepository` 已有 `findOrders(@Param("userId") Long userId, @Param("status") String status)`；`OrderService.getUserOrders` 增加重载或可选参数，**当 status 为 null 或 "all" 时**调用原 `findByUserIdAndDeletedAtIsNull...`，否则 `findOrders(userId, status)`（注意 `findOrders` 的 status 为 null 时查询条件）。

### 2.2 购物车勾选（后端）

| 方法 | 路径 | 说明 |
|------|------|------|
| PUT | `/cart/{id}/selection` | body: `{ "isSelected": 0 \| 1 }` |

- Controller 注入 `userId`，`CartService` 内校验 `cart.userId == userId` 后更新 `isSelected`。
- 解决现有 **IDOR**：顺带要求 `updateQuantity`、`removeFromCart` 校验归属（同一变更内修复，避免新接口安全更差）。

### 2.3 下单地址校验（后端）

在 `OrderService.createOrder` 中，当 `addressId != null`：

- 加载 `Address`，若不存在则抛错（已有）。
- **新增**：若 `address.getUserId() != userId` 则抛错（如「无权使用该收货地址」）。

## 3. 前端变更

| 模块 | 变更 |
|------|------|
| `router/index.js` | 注册 `/checkout` 或 `/orders/create`（二选一，建议 **`/checkout`** 避免与动态 `:id` 冲突） |
| 新视图 | `Checkout.vue`：展示选中商品摘要、地址下拉（`addressApi.get`）、备注、提交 |
| `Cart.vue` | `checkout()` → `router.push('/checkout')`；`toggleSelect` → 调用 selection API |
| `Orders.vue` | 状态 Tab 或下拉：`全部 / 待处理 / 处理中 / 已发货 / 已完成 / 已取消`；`loadOrders` 传 `status` |
| `api/order.js` | `getOrders(params)` → `api.get('/orders', { params })` |
| `api/cart.js` | `updateSelection(id, isSelected)` |

## 4. 操作按钮可见性规则（用户端）

| 页面/区域 | 控件 | 可见条件 |
|-----------|------|----------|
| 购物车 | 「结算」 | `cartItems.length > 0` 且 `summary.selectedCount > 0` 且用户积分 ≥ `summary.totalPoints`（前端预检可提示；后端为准） |
| 购物车 | 行勾选 | 始终可见；变更时调用 selection API |
| 我的订单 | 「查看详情」 | 每笔订单始终可见 |
| 我的订单 | 「取消订单」 | **仅** `order.status === 'pending'` |
| 订单详情 | 「取消订单」（若存在） | 同左，仅 `pending` |
| 结算页 | 「提交订单」 | 有选中商品（进入页面前已保证）；地址非必填时仍允许提交 |

管理员端按钮规则不在本变更范围（保持现状）。

## 5. 验收标准

### 5.1 功能

1. **AC-F1**：登录用户从购物车勾选至少一件商品，点击结算进入结算页，选择地址与备注后可提交，返回成功且订单为 `pending`，购物车中已结算行被移除。
2. **AC-F2**：用户积分不足或库存不足时，提交订单失败，提示与后端 `ORDER_001` 一致，且无部分扣款。
3. **AC-F3**：使用他人 `addressId` 调用下单接口应失败（后端校验）。
4. **AC-F4**：「我的订单」选择「待处理」仅展示 `pending` 订单；「全部」与未筛选一致。
5. **AC-F5**：勾选/取消勾选购物车行后刷新页面，勾选状态保持。

### 5.2 回归

1. **AC-R1**：`pending` 订单仍可「取消订单」，取消后积分与库存恢复（与现有逻辑一致）。
2. **AC-R2**：未登录访问结算页 → 路由守卫跳转登录。

### 5.3 非功能（基线）

1. **AC-N1**：不引入新的全局状态管理依赖（仍可用 Vue + Router + 现有 api）。

## 6. 文档同步

实施后更新：

- `doc/order-domain-baseline.md`（前端闭环、地址校验、筛选参数）
- `doc/baseline-requirements.md` 相关行
- 若恢复 `spec/AI2AI/协议和数据.md`：增补 `GET /orders` 与 `PUT /cart/{id}/selection`
- 白盒质量矩阵：本目录 **`whitebox-matrix.md`**（与 `tasks.md` Phase E 同步，见 **§8**）

## 7. 兼容性与回滚

- `GET /orders` 无 query 时行为与旧客户端兼容。
- 新增路由仅 additive；回滚可删除结算页与路由，购物车仍指向旧路径时需恢复占位。

## 8. 白盒测试与质量矩阵（执行入口）

本变更涉及的订单创建/取消、列表筛选、购物车归属与权限风险，已收敛为**可勾选候选矩阵**，用于 `mvn test` 补测、Code Review 与回归计划：

- **主文件**：同目录 **`whitebox-matrix.md`**（含分支说明、高风险点、WB-O/WB-API 编号矩阵、代码类待办 T1～T5）。
- **任务同步**：`tasks.md` **Phase E** 与白盒矩阵逐项对应；执行变更或补测时先打开 `whitebox-matrix.md`，再勾选 Phase E。

功能验收仍以 **§5 验收标准** 为准；白盒矩阵覆盖**实现分支、越权与状态风险**，与 Smoke（`doc/smoke-checkout-order-filter.md`）互补。

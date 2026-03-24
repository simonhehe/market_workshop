# 订单域基线（Order Domain Baseline）

> 聚焦**下单、支付（积分）、库存、状态、管理员操作**等与订单相关的规则与现状，作为产品、开发与测试的**单一事实入口（基线）**。若实现变更，请同步更新本文档。

---

## 1. 领域概念

| 术语 | 含义（当前实现） |
|------|------------------|
| 订单 | `Order`：归属 `userId`，含 `orderNo`、`totalPoints`、`status`、收件快照等 |
| 订单明细 | `OrderItem`：快照 `productName`、`pointsRequired`、`quantity`、`subtotalPoints` 等 |
| 下单 | 由购物车中 **`isSelected == 1`** 的行生成订单；非选中行不参与 |
| 积分支付 | 下单一次性扣除 `totalPoints`；记录 `PointsHistory` 类型 `spend` |
| 库存 | 下单按行扣减 `Product.stock`、增加 `sales`；用户取消时回退 |

---

## 2. 订单生命周期（状态）

### 2.1 状态取值（实体注释）

`pending`、`processing`、`shipped`、`completed`、`cancelled`（及管理员可能写入的**任意字符串**）。

### 2.2 创建

- 新建订单默认 **`pending`**。
- `orderNo` 若为空则在持久化前生成（`ORD` + 时间戳）。

### 2.3 用户取消

- 接口：`PUT /api/orders/{id}/cancel`（需登录且 `order.userId == token.userId`）。
- **仅当** `status == pending` 允许取消。
- 成功效果：状态 → `cancelled`；**退还** `totalPoints`；写 `PointsHistory` 类型 `refund`；按明细**恢复库存**并回退销量。

### 2.4 管理员更新状态

- 接口：`PUT /api/admin/orders/{id}/status`，body `{ "status": "<字符串>" }`。
- **不**校验状态机；**不**因改为 `cancelled` 而自动执行与用户取消相同的退款/还库存逻辑。

### 2.5 状态迁移表（基线）

| From | 事件 | 执行者 | To | 库存/积分副作用 |
|------|------|--------|-----|------------------|
| — | `createOrder` | 系统 | `pending` | 扣积分、扣库存、spend 流水、删购物车行 |
| `pending` | `cancelOrder` | 用户 | `cancelled` | 退积分、refund 流水、还库存 |
| 任意 | `updateOrderStatus` | 管理员 | **任意** | 默认无自动财务/库存副作用 |
| `shipped` | `confirmReceipt` | 用户 | `completed` | **不**改积分、**不**改库存；非 `shipped` 拒绝；已 `completed` **幂等** |

### 2.6 用户确认收货

- 接口：`PUT /api/orders/{id}/confirm-receipt`（需登录且 `order.userId == token.userId`）。
- **仅当** `status == shipped` 允许确认（已为 `completed` 时 **幂等**：成功返回，无副作用）。
- **不改** `PointsHistory`、**不改** `Product` 库存/销量（与下单/取消不同）。

### 2.7 用户订单列表筛选

- 接口：`GET /api/orders` **可选** Query：`status`（值为 `pending`、`processing`、`shipped`、`completed`、`cancelled` 之一）。
- **不传**或空字符串：与改动前一致，返回当前用户未删除订单全量。
- **非法** `status`：400，`error.code` 为 **`ORDER_004`**（实现以代码为准）。

---

## 3. 核心流程（服务端）

### 3.1 下单 `OrderService.createOrder`

1. 读取当前用户全部购物车行，筛 `isSelected == 1`。
2. 逐行加载商品：校验库存；累计积分。
3. 校验用户积分 ≥ 应付积分。
4. 若提供 `addressId`，加载地址并拼接收件信息写入订单；**未校验地址是否属于当前用户**（基线风险）。
5. 持久化订单与明细；扣库存与销量；`updatePoints(-totalPoints)`；写 `PointsHistory`；删除对应购物车行。

### 3.2 取消 `OrderService.cancelOrder`

见 §2.3；若订单不存在、非本人、非 `pending`，抛错由控制器返回业务错误码。

### 3.3 确认收货 `OrderService.confirmReceipt`

见 §2.6；校验存在、未软删、归属、`shipped`→`completed`；`completed` 直接返回实体不写库。

### 3.4 列表筛选 `OrderService.getUserOrders(userId, status)`

- `status` 为空或空白：同原 `findByUserIdAndDeletedAtIsNullOrderByCreatedAtDesc`。
- 否则：`findByUserIdAndStatusAndDeletedAtIsNullOrderByCreatedAtDesc`；非法枚举抛 `IllegalArgumentException`，映射为 **`ORDER_004`**。

---

## 4. 与购物车、积分、库存的关系

| 关联 | 规则 |
|------|------|
| 购物车 | 仅选中行参与；成功后**物理删除**这些行 |
| 积分 | 下单扣减；`pending` 取消退回；管理员改状态**不自动**调积分 |
| 库存 | 下单扣减；用户取消恢复；管理员改状态**不自动**恢复 |

---

## 5. API 表面（订单相关）

**前缀**：`/api`  

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/orders` | 创建订单，body 可选 `addressId`、`remark` |
| GET | `/orders` | 当前用户订单列表；**可选** `?status=`（见 §2.7） |
| GET | `/orders/{id}` | 详情（含 `orderItems`），校验归属 |
| PUT | `/orders/{id}/cancel` | 用户取消 |
| PUT | `/orders/{id}/confirm-receipt` | 用户确认收货（§2.6） |
| GET | `/admin/orders` | 管理员列表 |
| PUT | `/admin/orders/{id}/status` | 管理员改状态 |

完整字段与错误码：**[单链路基线分析_技术.md](../analysis/单链路基线分析_技术.md)** §6、§11。

---

## 6. 前端基线（订单链）

| 项 | 现状 |
|----|------|
| 订单列表 / 详情 / 取消 / **筛选** / **确认收货** | `Orders.vue`（筛选下拉、列表上取消与确认收货）、`OrderDetail.vue`（详情内取消与确认收货） |
| 创建订单 | `orderApi.createOrder` 已封装，**无页面在流程中调用** |
| 购物车结算 | 跳转 `/orders/create`，与路由 **`/orders/:id`** 冲突，**无法完成标准下单 UI 闭环** |

---

## 7. 边界与异常（基线测试关注点）

- 无选中购物车行、库存不足、积分不足、地址 id 不存在 → 创建失败。
- 非 `pending` 取消 → 失败。
- 查看非本人订单详情 → 失败。
- 非 `shipped` 确认收货、非法列表 `status` → 失败（`ORDER_003` / `ORDER_004`）。
- 并发下库存与积分一致性：**未**在代码中体现强约束（见 [baseline-design.md](baseline-design.md) §9）。

---

## 8. 追溯

| 文档 | 用途 |
|------|------|
| [订单链路基线分析.md](../analysis/订单链路基线分析.md) | 业务向流程与角色 |
| [单链路基线分析_技术.md](../analysis/单链路基线分析_技术.md) | 时序、状态机、契约、安全、测试矩阵 |
| [测试基线建议.md](../analysis/测试基线建议.md) | smoke 与用例优先级 |
| [openspec/config.yaml](../../openspec/config.yaml) | `quality_risks` 中与订单相关条目 |

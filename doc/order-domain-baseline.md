# 订单域基线（Order Domain Baseline）

> **定位**：订单相关**数据、接口、状态、副作用、已知缺口**的单一摘要，便于需求评审与测试设计迭代。  
> 细节图与矩阵见 `**单链路基线分析.md`**、`**单链路基线分析_技术.md**`（本文件不重复粘贴大段 Mermaid）。

---

## 1. 聚合与实体


| 聚合 / 表      | 说明                                                                                              |
| ----------- | ----------------------------------------------------------------------------------------------- |
| `Order`     | 订单头：`orderNo`、`userId`、`addressId`（可空）、`totalPoints`、`status`、收货快照字段、`remark`、时间戳与 `deleted_at` |
| `OrderItem` | 明细：快照 `productName`、单价积分、数量、小计积分；关联 `orderId`、`productId`                                       |
| 关联          | 逻辑 FK：`users`、`addresses`、`products`；无 JPA 关联对象强制约束                                             |


**积分流水**：`PointsHistory`，订单相关 `type` 含 `spend` / `refund`，`related_type = order`，`related_id = order.id`。

---

## 2. 状态定义（字符串）


| status       | 含义（前端文案） |
| ------------ | -------- |
| `pending`    | 待处理      |
| `processing` | 处理中      |
| `shipped`    | 已发货      |
| `completed`  | 已完成      |
| `cancelled`  | 已取消      |


**数据库**：`TEXT`，无 DB 枚举约束。

---

## 3. 状态流转（实现基线）

### 3.1 用户

- 创建订单成功 → `pending`。
- `PUT /orders/{id}/cancel`：仅当订单属于当前用户且 `status == pending` → `cancelled`，并返还积分、回补库存/销量、记 refund 流水。

### 3.2 管理员

- `PUT /admin/orders/{id}/status`，body `{ "status": "<string>" }`。
- **实现**：直接 `setStatus`，**无**合法枚举校验、**无**与库存/积分联动。

### 3.3 产品规则（待确认）

- 目标状态机、终态、是否允许管理员写 `cancelled`、是否允许从 `cancelled` 迁出 → **N/A**（需 Me2AI 或产品确认）。

---

## 4. 核心接口（相对 `/api`）


| 方法   | 路径                          | 鉴权     | 说明                                    |
| ---- | --------------------------- | ------ | ------------------------------------- |
| POST | `/orders`                   | 用户 JWT | body：`addressId?`、`remark?`；从购物车选中行创建 |
| GET  | `/orders`                   | 用户 JWT | 本人订单列表（`deleted_at` 过滤）；**Query** `status` 可选：`all`/缺省/空 表示全部，否则按 `Order.status` 精确筛选 |
| GET  | `/orders/{id}`              | 用户 JWT | 本人详情 + `orderItems`                   |
| PUT  | `/orders/{id}/cancel`       | 用户 JWT | 取消 pending                            |
| GET  | `/admin/orders`             | admin  | 全量列表                                  |
| PUT  | `/admin/orders/{id}/status` | admin  | 改状态                                   |


**错误码示例**：`ORDER_001` 创建失败、`ORDER_002` 查询/取消失败、`AUTH_004` 无管理员权限（以 Controller 为准）。

---

## 5. 副作用矩阵（实现）


| 事件           | 库存  | 销量  | 用户积分 | 积分流水   | 购物车      |
| ------------ | --- | --- | ---- | ------ | -------- |
| 下单成功         | 扣减  | 增加  | 扣减   | spend  | 删除已结算选中行 |
| 用户取消 pending | 回补  | 回冲  | 返还   | refund | 不变       |
| 管理员改状态       | 不变  | 不变  | 不变   | 不变     | 不变       |


---

## 6. 与购物车、地址的耦合

- 仅结算 `Cart.isSelected == 1` 的行。
- 购物车行勾选：`POST /cart/selection`，body `{ "cartItemId": <id>, "isSelected": 0 \| 1 }`（推荐）；兼容 `PUT /cart/{id}/selection`，body `{ "isSelected": 0 \| 1 }`；`PUT/DELETE /cart/{id}` 校验行归属当前用户。
- `POST /orders` 在 `addressId` 非空时校验 `Address.userId == 当前用户`。

---

## 7. 前端基线


| 项                      | 状态               |
| ---------------------- | ---------------- |
| 订单列表 / 详情 / 取消 / 状态筛选   | `Orders.vue`：`getOrders({ status })` |
| 结算页 `/checkout`       | `Checkout.vue`：`createOrder` 成功后跳转订单详情 |
| 购物车勾选与去结算        | `Cart.vue`：`updateSelection`、`checkout` → `/checkout` |


---

## 8. 测试锚点

- 白盒 JUnit 源码目录：**`test/backend/java`**（执行：`cd backend && mvn test`）。  
- 集成场景矩阵：`**单链路基线分析_技术.md` §5.8（H1～H11）**。  
- Smoke 是否包含 `POST /orders`：**见 `测试基线建议.md`**（默认最短 Smoke 可不包含写单）；API 脚本见 **`test/smoke/`**。

---

## 9. 修订记录


| 版本  | 日期  | 说明              |
| --- | --- | --------------- |
| 0.1 | N/A | 初稿，与代码及订单分析文档对齐 |
| 0.2 | 2026-03 | 订单筛选、`/checkout`、购物车选中 API、地址归属校验与测试锚点 |



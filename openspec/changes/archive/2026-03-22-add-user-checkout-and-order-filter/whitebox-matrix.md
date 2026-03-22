# 白盒测试候选矩阵（本变更关联域）

> **定位**：从测试/质量视角，收敛订单创建/取消/管理端状态、购物车归属与本变更 API 的**分支、权限风险、优先补测项**。  
> **执行触发**：与本 Change 一并评审或跑 `mvn test` / 补测时，以本文件为**单一检查清单**；`tasks.md` Phase E 与之同步。  
> **测试目录**：JUnit 源码位于仓库 **`test/backend/java`**（非 `backend/src/test`），见根目录 **`test/README.md`**。

---

## 1. 订单创建 / 取消 / 状态流转 — 关键分支

| 路径 | 代码位置 | 关键分支 / 副作用 |
|------|----------|-------------------|
| 创建 | `OrderService.createOrder` | 无选中行 → 抛错；商品存在、**库存**；**积分**；`addressId` 空跳过；非空则地址存在 + **`address.userId == userId`**；`pending`；明细；**扣库存/增销量**；`updatePoints(-total)`；`spend` 流水；**删购物车行** |
| 创建-脆弱点 | `createOrder` 内写明细循环 | `findById` 为 `null` 时**静默跳过**库存更新（与前置「商品存在」不一致） |
| 取消 | `OrderService.cancelOrder` | 订单不存在；**非本人**；**非 `pending`**；返积分+流水+库存回补；`cancelled` |
| 取消-高风险 | `cancelOrder` | `user == null` 时**不返积分、不写退款流水**，仍回补库存并置 `cancelled`（路径不一致，需产品确认或修代码） |
| 用户侧状态 | 仅 `cancelOrder` | 合法：`pending → cancelled` |
| 管理侧状态 | `AdminService.updateOrderStatus` | **仅** `setStatus`，无枚举校验、无库存/积分联动 |

---

## 2. 权限与状态 — 高风险点

| 类别 | 风险 | 说明 |
|------|------|------|
| JWT 白名单 | 中～高 | `/products`、`/categories` 前缀放行；需防误放行其下**需鉴权的写接口**（若存在） |
| 订单读 | 低 | `OrderController.getOrderDetail` 已比 `userId` |
| 地址 IDOR | 低（本变更已补） | `createOrder` 校验地址归属 |
| 购物车 IDOR | 低（本变更已补） | `CartService` 校验 `cart.userId` |
| 管理端改状态 | **高** | `role` 在 Controller 校验；**Service 层无状态机**，任意字符串可写入 |
| 软删一致性 | 中 | `getOrderById` 未排除 `deletedAt`，与列表过滤可能不一致 |
| 积分下界 | 中 | `UserService.updatePoints` 未禁止负积分 |
| 并发 | 高（未测） | 库存/积分无显式乐观锁 |

---

## 3. 优先补测 — Service vs 接口层

| 优先级 | 层级 | 建议 |
|--------|------|------|
| P0 | Service | `OrderService.createOrder`：空选中、库存不足、积分不足、成功路径 verify（扣积分、流水、删车） |
| P0 | Service | `OrderService.cancelOrder`：非本人、非 pending、成功退款+库存；**user==null 分支**（固化预期或驱动修复） |
| P1 | Service | `getUserOrders`：边界空格、`ALL`；`AdminService.updateOrderStatus` 现状固化 |
| P0 | Controller | `GET /orders/{id}` 非本人；`PUT /admin/orders/{id}/status` 非 admin → 403 |
| P1 | Filter/集成 | `AuthMiddleware`：无 token→401；白名单可访问 |

---

## 4. 白盒测试候选矩阵（执行勾选）

| ID | 层级 | 范围 | 场景 / 断言要点 | 优先级 | 现状 |
|----|------|------|-----------------|--------|------|
| WB-O-01 | Service | `createOrder` | 无选中商品 → 异常 | P0 | **已有** `OrderServiceTest` |
| WB-O-02 | Service | `createOrder` | 商品不存在 / 库存不足 | P0 | **已有** `OrderServiceTest` |
| WB-O-03 | Service | `createOrder` | 积分不足 | P0 | **已有** `OrderServiceTest` |
| WB-O-04 | Service | `createOrder` | `addressId` 非空且非本人地址 → 异常 | P0 | **已有** `OrderServiceTest` |
| WB-O-05 | Service | `createOrder` | 成功：verify 扣积分、流水、删购物车、扣库存 | P0 | **已有** `OrderServiceTest` |
| WB-O-06 | Service | `createOrder` | 明细循环 `product==null` 行为（缺陷或文档化） | P1 | 待补（见 §5 T4） |
| WB-O-07 | Service | `getUserOrders` | `null` / `all` / 空白 / 具体 status | P1 | **部分已有** |
| WB-O-08 | Service | `cancelOrder` | 非本人 / 非 pending | P0 | **已有** `OrderServiceTest` |
| WB-O-09 | Service | `cancelOrder` | 成功：退款、库存、流水、`cancelled` | P0 | **已有** `OrderServiceTest` |
| WB-O-10 | Service | `cancelOrder` | `user==null` 行为 | P0 | **已有** `OrderServiceTest`（固化现状） |
| WB-A-01 | Service | `AdminService.updateOrderStatus` | 任意 status 可写（现状固化） | P1 | **已有** `AdminServiceTest` |
| WB-C-01 | Service | `CartService` | 归属 / `isSelected` 非法 | P1 | **部分已有** |
| WB-API-01 | Controller | `OrderController` | `GET /orders?status=` 透传 | P1 | **已有** |
| WB-API-02 | Controller | `OrderController` | `POST /orders` body | P1 | **已有** |
| WB-API-03 | Controller | `OrderController` | `GET /orders/{id}` 非本人 | P0 | **已有** `OrderControllerTest` |
| WB-API-04 | Controller | `AdminController` | 非 admin 改订单状态 → 403 | P0 | **已有** `AdminControllerTest` |
| WB-SEC-01 | Filter/集成 | `AuthMiddleware` | 401 / 白名单 | P1 | 待补（可选） |
| WB-CONC-01 | 集成 | 下单+库存 | 并发（可选） | P2 | 待补（可选） |

---

## 5. 代码类待办（非本变更必做；纳入 Phase E）

| # | 类型 | 说明 |
|---|------|------|
| T1 | 测试 | 按矩阵补 `OrderService.createOrder` / `cancelOrder` 单元测试 |
| T2 | 测试 | 补 `OrderController` 详情越权、`AdminController` 403 |
| T3 | 缺陷？ | 评审 `cancelOrder` 在 `user==null` 时仍取消且不退款 |
| T4 | 缺陷？ | 评审 `createOrder` 明细循环 `product==null` 跳过库存 |
| T5 | 测试 | 可选：`AuthMiddleware` / JWT 集成切片 |

---

## 6. 与仓库其他文档的关系

| 文档 | 关系 |
|------|------|
| `doc/smoke-checkout-order-filter.md` | 端到端 Smoke，与本矩阵互补（黑盒/主路径） |
| `doc/测试基线建议.md` | 通用测试策略；本文件聚焦**本变更订单/购物车域白盒** |
| `design.md` §5 | 功能验收；矩阵覆盖**实现分支与回归风险** |

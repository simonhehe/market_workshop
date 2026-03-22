# Tasks: 用户结算下单与订单状态筛选

> 按顺序实施；安全子项可与 API 并行但须在合并前完成。

## Phase A — 后端

- [x] **A1** `OrderService` / `OrderController`：`getUserOrders` 支持可选 `status`（含 `all` / 缺省）；单元或接口自测。
- [x] **A2** `OrderService.createOrder`：`addressId` 非空时校验 `address.userId == userId`。
- [x] **A3** `CartService` + `CartController`：新增更新 `isSelected` 接口；**A3b** `updateQuantity`、`removeFromCart` 校验 `cart.userId == userId`。
- [x] **A4** 手动验证：`GET /api/orders?status=pending`、`POST /orders` 地址越权拒绝。

## Phase B — 前端 API 与路由

- [x] **B1** `api/order.js`：`getOrders({ status })`。
- [x] **B2** `api/cart.js`：`updateSelection` → `PUT /cart/{id}/selection`（与 design 一致）。
- [x] **B3** `router/index.js`：注册 `/checkout`，`meta.requiresAuth: true`。

## Phase C — 页面

- [x] **C1** 新建 `Checkout.vue`：地址列表、备注、`orderApi.createOrder`，成功跳转 `/orders/:id` 或 `/orders`。
- [x] **C2** `Cart.vue`：`checkout` → `/checkout`；`toggleSelect` 调用 B2。
- [x] **C3** `Orders.vue`：状态筛选 UI + `loadOrders` 传参；空态文案。

## Phase D — 文档与质量

- [x] **D1** 更新 `doc/order-domain-baseline.md`、`doc/baseline-requirements.md` 相关段落。
- [x] **D2**（可选）补充一条 Smoke 步骤：登录 → 加购 → 结算 → 见订单（见 `doc/测试基线建议.md`）。

## Phase E — 白盒测试补全（与 `whitebox-matrix.md` 同步）

> 打开 **`whitebox-matrix.md`** §4 矩阵按 ID 执行；以下为跟踪用勾选。

- [x] **E1** WB-O-01～O-03、O-05：`OrderService.createOrder` 异常路径与成功路径 verify（见 `OrderServiceTest`）
- [x] **E2** WB-O-08～O-10：`OrderService.cancelOrder` 含 `user==null` 行为评审或测试固化（见 `OrderServiceTest`）
- [x] **E3** WB-API-03：`OrderController` `GET /orders/{id}` 非本人订单（见 `OrderControllerTest`）
- [x] **E4** WB-API-04：`AdminController` `PUT /admin/orders/{id}/status` 非 admin → 403（见 `AdminControllerTest`）
- [x] **E5**（可选）WB-A-01 已补测（`AdminServiceTest`）；WB-O-06 / WB-SEC-01 / WB-CONC-01 仍为可选，见矩阵 §4「现状」

## 完成定义（DoD）

- [x] 所有 **design.md §5 验收标准** 可演示或通过。
- [x] 无新增 ESLint/编译错误；在 `backend/` 执行 `mvn -q test`（JUnit 源码在 **`test/backend/java`**）或至少 `mvn -q -DskipTests package` 通过。
- [x] **（推荐）** Phase E 或 `whitebox-matrix.md` §4 中 P0 项均有对应测试或已记录豁免理由。

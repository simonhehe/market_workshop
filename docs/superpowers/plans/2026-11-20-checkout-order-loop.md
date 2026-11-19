# 分钟级实施计划：checkout-order-loop

> 每个步骤遵循 RED（失败测试）→ GREEN（最小实现）→ 回归。命令均在仓库根目录执行。

## Step 1 — 后端测试基线（约 10 分钟）

1. 确认 `backend/src/test/resources/application-test.yml` 指向独立 SQLite 测试库。
2. 新增 `OrderServiceIntegrationTest`、`CartServiceIntegrationTest`、`OrderControllerTest`、`CartControllerTest`、`DataInitializerTest`，仅写目标行为断言。
3. 执行：

```bash
cd backend && mvn -B test
```

预期：新增用例失败（缺少 `BusinessException`、校验/勾选/种子），编译失败视为无效 RED，需先让测试可编译（可先新增 `BusinessException` 空实现或使用断言编译通过的现有异常类型）。

失败处理：若编译错误仅因待实现方法不存在，先补方法签名（空实现），再确认断言级失败。

## Step 2 — BusinessException 与错误契约（约 10 分钟）

1. 新增 `common/BusinessException`，含 `code`。
2. `OrderController`/`CartController` 捕获 `BusinessException` 返回 `ApiResponse.error(code, message)`。
3. 重跑 Controller 测试，期望错误码断言转绿。

## Step 3 — OrderService 下单校验与落库（约 30 分钟）

1. 按 PRD §3.2.1 顺序实现：`CART_EMPTY` → `ADDR_REQUIRED`/`ADDR_NOT_FOUND` → `PRODUCT_UNAVAILABLE` → `STOCK_INSUFFICIENT` → `POINTS_INSUFFICIENT`。
2. 单事务写入订单/快照/积分/库存/流水/购物车；`balanceAfter` 使用变动后余额。
3. `Order` 订单号增加随机后缀。
4. 运行 `OrderServiceIntegrationTest`，逐条转绿。

失败处理：若事务未回滚，检查异常类型是否为 `RuntimeException` 且未被吞掉；若积分对不上，核对是否重复 `findById` 读到旧实体。

## Step 4 — 取消订单（约 15 分钟）

1. 归属 → 状态 → 退款/恢复库存/流水。
2. 运行取消与对账用例。

## Step 5 — 购物车勾选/归属/累计库存（约 20 分钟）

1. `CartService.selectItem`、`updateQuantity(cartId, userId, qty)`、`removeFromCart(cartId, userId)`。
2. `CartController` 增加 `PUT /{id}/select` 与 `@RequestAttribute("userId")`。
3. 运行 `CartServiceIntegrationTest`、`CartControllerTest`。

## Step 6 — mock 地址种子（约 10 分钟）

1. `DataInitializer` 幂等写入 `user1` 两条地址。
2. 运行 `DataInitializerTest`。

## Step 7 — 前端 TDD（约 40 分钟）

1. 新增 `Checkout.spec.js`、`Cart.spec.js`、`Orders.spec.js`、`OrderDetail.spec.js`；mock `api/exports` 与 `$router`。
2. 执行 `npm test`，确认 RED。
3. 新增 `/orders/create` 路由与 `Checkout.vue`；接通 `cartApi.selectItem` 与 `Cart.vue`；`OrderDetail.vue` 增加 pending 取消。
4. 执行 `npm test` 转绿；`npm run build` 验证构建。

## Step 8 — 全量验证与走查（约 30 分钟）

```bash
cd backend && mvn -B test
cd backend && mvn -B -DskipTests package
cd frontend && npm test
cd frontend && npm run build
```

1. 备份 `backend/database/mall.db`（若存在）。
2. 启动后端（端口 18000-19000，`--server.port=18080`），`GET /api/health` 返回 healthy。
3. 只读冒烟 `/api/products`、`/api/categories`；不执行写操作。
4. 记录 JaCoCo/Vitest 覆盖率，未达 95% 如实说明。

## Step 9 — 提交与证据（约 15 分钟）

1. 运行 `git status` 审阅变更，按逻辑分组提交（后端实现、测试、前端、文档）。
2. 不 force push；普通推送后记录 commit hash。
3. 将红色/绿色日志、覆盖率、走查结果写入 `artifacts/output/evidence/`，暴露并评论工单。

## Review 备注

- 每个 Task 完成后更新 `openspec/changes/checkout-order-loop/tasks.md`。
- 若发现 PRD 未覆盖的边界，记录而非擅自扩展。
- 不引入新依赖；不修改 git config；不创建 MR/PR。

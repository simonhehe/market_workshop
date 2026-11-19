# 任务清单：checkout-order-loop

> 状态：`[ ]` 未开始 / `[x]` 完成。每条任务以失败测试（RED）开始，最小实现转绿（GREEN）。

## T1 购物车勾选与归属（cart-selection）

- [x] T1.1 新增 `BusinessException` 与 `CART_ITEM_FORBIDDEN` 等错误键。
- [x] T1.2 RED：`CartServiceTest` / `CartControllerTest` 覆盖勾选持久化、越权勾选/改数量/删除、加购累计超库存、改数量超库存。
- [x] T1.3 GREEN：实现 `PUT /api/cart/{id}/select`，为 PUT/DELETE 增加 `userId` 归属校验，`addToCart` 累计库存校验。
- [x] T1.4 前端 `cartApi.selectItem` + `Cart.vue` 勾选接通、空选中结算提示。
- [x] T1.5 RED/GREEN：`Cart.spec.js` 覆盖勾选调用与空选中提示。
- [x] T1.6 补充 select 入参边界码 `CART_SELECTION_INVALID` 的文档与测试（非 PRD 九类业务错误）。

## T2 下单校验与落库（order-create）

- [x] T2.1 RED：`OrderServiceIntegrationTest` 覆盖成功下单、快照、扣积分/库存/清选中项、客户端伪造、下架、库存不足回滚、积分不足、地址必填/越权、空购物车。
- [x] T2.2 GREEN：`OrderService.createOrder` 按固定顺序校验并在单事务落库；`OrderController` 返回业务错误键并忽略客户端合计/状态。
- [x] T2.3 修复 `PointsHistory.balanceAfter`（spend）。
- [x] T2.4 订单号增加随机后缀降低碰撞。
- [x] T2.5 后端 Controller 测试：错误码、客户端伪造字段不生效。

## T3 订单状态与取消（order-lifecycle）

- [x] T3.1 RED：取消退款、非 pending、越权取消、余额/库存对账。
- [x] T3.2 GREEN：`cancelOrder` 归属→状态顺序校验，退款、refund 流水 `balanceAfter`、恢复库存。
- [x] T3.3 前端订单列表/详情状态文案与 pending 取消入口；`Orders.spec.js` / `OrderDetail.spec.js`。

## T4 结算页（checkout）

- [x] T4.1 RED：`Checkout.spec.js` 覆盖进入结算展示、未选地址拦截、地址选择、数量联动与库存上限、无选中重定向、防重复提交、库存不足展示、成功页。
- [x] T4.2 GREEN：新增 `/orders/create` 路由与 `Checkout.vue`，实现加载、选择、合计、预检、错误映射、成功卡片。
- [x] T4.3 视觉：京东风格白底 + 红色强调、地址卡片、商品表格、右侧合计、底部提交栏（L2）。

## T5 mock 地址种子

- [x] T5.1 RED：初始化测试断言 `user1` 有 2 条地址且重复执行不重复插入。
- [x] T5.2 GREEN：`DataInitializer` 幂等写入 2 条归属 `user1` 的地址。

## T6 验证与交付

- [x] T6.1 `mvn -B test` 全绿；记录红色→绿色日志。
- [x] T6.2 `npm test` 全绿；`npm run build` 成功。
- [x] T6.3 覆盖率报告（JaCoCo + Vitest），不足 95% 如实说明缺口。
- [x] T6.4 本地启动走查：端口 18000-19000、`/api/health`、只读冒烟商品/分类。
- [x] T6.5 逻辑提交（不 force push）；更新本文档状态；输出证据与交接。

## 验收映射

| COV | AC | 任务 | 测试 |
|---|---|---|---|
| COV-CART-SELECT | AC-M1-01/02 | T1、T4 | JUnit、Vitest |
| COV-CHECKOUT | AC-M2-01/03/04/05、AC-M5-05 | T4 | Vitest |
| COV-ADDR | AC-M2-02、AC-M5-01 | T2、T4 | JUnit、Vitest |
| COV-ORDER-CREATE | AC-M3-01/02/04/05/06/07、AC-M6-01 | T2、T5 | JUnit+MockMvc |
| COV-ORDER-STATE | AC-M4-01/02/03/04/05 | T3 | JUnit、Vitest |
| COV-STOCK | AC-M1-04、AC-M3-07、AC-M5-04 | T1、T2、T4 | JUnit、Vitest |
| COV-POINTS | AC-M5-02 | T2、T4 | JUnit、Vitest |
| COV-ERROR | AC-M5-03 | T2 | JUnit |
| COV-LEDGER | AC-M3-03、AC-M4-02、AC-M6-02/03 | T2、T3 | JUnit |
| COV-SECURITY | AC-M1-03、AC-M4-04、AC-M5-01 | T1、T2、T3 | JUnit |

# Proposal: 用户结算下单与订单状态筛选

## 背景与问题

- **购物车 → 结算**：`Cart.vue` 跳转 `/orders/create`，路由未注册，`orderApi.createOrder` 无页面调用，用户无法完成下单闭环。
- **订单列表**：`GET /orders` 返回全部状态；`Orders.vue` 无筛选，用户难以按状态查看。
- **购物车勾选**：`isSelected` 无后端更新接口，勾选无效，与下单逻辑（仅结算选中行）不一致。
- **安全**：`order-domain-baseline.md` 已记：`addressId` 未校验归属当前用户，应在同一变更或紧后迭代修复（本提案 **纳入** 下单前地址校验）。

## 目标

1. 用户从购物车进入**结算流程**，可选择收货地址（可选）、备注，提交后调用现有 `POST /api/orders` 成功建单并跳转订单详情或列表。
2. 用户「我的订单」支持按**订单状态**筛选（含「全部」）。
3. 明确**操作按钮可见性规则**与**验收标准**，作为实现与测试依据。

## 范围（In Scope）

- 前端：新结算页/流程、路由、购物车勾选 API 联调、订单列表筛选 UI + `orderApi` 参数。
- 后端：`GET /orders` 支持可选查询参数 `status`；购物车行更新 `isSelected`；`createOrder` 校验 `addressId` 属于当前用户（若提供）。

## 非目标（Out of Scope）

- 管理员订单流程变更、订单状态机业务规则重定义（仍允许管理员任意改状态，除非另开变更）。
- 支付货币、物流对接。
- 并发库存乐观锁（可另开变更）。

## 关键依赖

- 现有实体 `Order.status`、`Cart.isSelected`、`OrderService.createOrder` 行为见 `doc/order-domain-baseline.md`。

## 验收标准（摘要）

完整条目见同目录 `design.md` §验收标准；实现完成后应可通过 **手动 / 自动化 Smoke** 验证结算与筛选主路径。

## 质量与白盒测试（执行入口）

- **白盒候选矩阵**：`whitebox-matrix.md`（订单/购物车分支、权限与状态高风险点、Service/Controller 优先补测项、WB-O/WB-API 编号表）。
- **任务勾选**：`tasks.md` Phase E 与矩阵同步；与 **Smoke**（`doc/smoke-checkout-order-filter.md`）分工为：Smoke 主路径，白盒矩阵主**分支与权限回归**。

## 风险

- 修改 `OrderService.createOrder` 可能影响集成测试（尚无基线测试）；建议合并前本地回归登录、加购、下单、取消 pending。

---

**Change ID**: `add-user-checkout-and-order-filter`  
**建议分支名**: `feature/user-checkout-order-filter`

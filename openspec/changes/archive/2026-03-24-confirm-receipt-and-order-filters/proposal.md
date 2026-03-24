## Why

用户侧订单在发货后（`shipped`）缺少「确认收货」闭环，无法进入终态（如 `completed`），与常见电商/福利兑换心智不一致。同时「我的订单」列表无法按状态筛选，订单量大时排查成本高。需在**现有订单链与状态字段**上增量扩展，并明确**按钮可见性**与**验收标准**，避免与管理员改状态语义混淆。

## What Changes

- **用户确认收货**：在满足前置状态时，允许当前用户将本人订单从 `**shipped` → `completed`**（或团队约定的等价终态）；禁止在未发货等状态下确认；需记录可追溯操作（至少订单状态与 `updated_at`，是否额外流水 **见设计**）。
- **订单列表按状态筛选**：用户侧订单列表 API 支持按 `**status` 查询参数**过滤（可多值或单值，**见设计**）；默认仍为「全部」以兼容现有客户端。
- **前端**：订单列表页增加状态筛选控件；订单详情/列表在合适状态下展示**确认收货**按钮；**操作按钮可见性**按规则表实现（见 specs）。
- **文档与基线**：同步 `doc/baseline/order-domain-baseline.md`、`doc/analysis/单链路基线分析_技术.md`（契约章节）、`openspec/config.yaml` 中与订单相关的 `core_flows` / `quality_risks`（如有）；**不**改变用户取消 `pending` 的既有语义。
- **BREAKING**：若现有客户端假定「列表只返回全量且无查询参数」，新增**可选**查询参数应保持**默认行为不变**，则不构成破坏性；若筛选用法与默认冲突则在设计中显式标注 **BREAKING**（当前方案倾向非 BREAKING）。

## Capabilities

### New Capabilities

- `order-confirm-receipt`: 用户确认收货（状态迁移、权限、与管理员改状态的关系、错误码）。
- `order-list-filtering`: 用户订单列表按状态筛选（API 约定、前端交互、空结果行为）。

### Modified Capabilities

- （无）仓库内 `openspec/specs/` 当前无既有 spec；需求变更落在上述两条**新建**能力及基线文档。

## Impact

- **后端**：`OrderController` / `OrderService`（或新建小服务方法）、`OrderRepository` 查询；可能 `ApiResponse` 错误码；`AuthMiddleware` 无改（仍走 JWT）。
- **前端**：`Orders.vue`、`OrderDetail.vue`、`api/order.js`、路由守卫不变。
- **数据**：沿用 `orders.status` 字符串；若不新增表，则无迁移；若引入确认时间点字段 **见 design**。
- **测试**：见本 change `design.md` **「测试与质量（本轮分析）」** 与 `tasks.md` **§6**；需补齐 API 集成测试、（可选）Jacoco 对变更触及代码的覆盖门槛；**UI 冒烟已选定 Cypress**（见 `design.md` §4）；`doc/analysis/订单链路-UT与API测试.md` 与测试基线文档交叉引用。


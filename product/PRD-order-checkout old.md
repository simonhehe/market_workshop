# PRD · 下单结算（order-checkout）

| 字段 | 值 |
|---|---|
| **文档 ID** | PRD-order-checkout |
| **版本** | v0.1-draft |
| **状态** | L2 待人闸（人闸-L2-PRD） |
| **visual_level** | **L2** — 以 `product/order.html` 为视觉与交互基线；实现层 Vue 页须复现关键区块、步骤条与错误态，不要求像素级还原 |
| **上游输入** | `product/order.html`（已确认原型）、`product/baseline/project-baseline.md`（L1 基线） |
| **MVP 级别** | 中等 — 可演示闭环：购物车勾选 → 结算页 → 选地址 → 提交 → 扣积分/库存 → 订单列表/详情/取消 |
| **非目标** | 真实支付、优惠券、拆单、完整售后、管理端结算功能 |
| **生成方式** | L2-2 · `/opsx-explore` |
| **日期** | 2026-08-04 |

---

## 1. 模块索引

### 1.1 用户旅程（目标闭环）

```
登录 → 浏览/加购 → 购物车勾选 → 进入结算页(/orders/create)
     → 选择收货地址 → 确认积分合计 → 提交订单
     → [成功] 订单列表/详情(pending) → [可选] 取消 → 积分退还 + 库存回滚
     → [失败] 结算页展示明确错误（地址/库存/积分/无选中商品）
```

### 1.2 前端模块

| 模块 | 路径/文件 | 职责 | 现状 |
|---|---|---|---|
| 购物车 | `frontend/src/views/Cart.vue` | 勾选商品、展示合计、`checkout()` 跳转结算 | ⚠️ `toggleSelect` 未接通后端；跳转目标路由缺失 |
| **结算页** | `frontend/src/views/Checkout.vue`（待建） | 地址选择、商品摘要、积分合计、备注、提交、错误态 | ❌ **缺失** |
| 路由 | `frontend/src/router/index.js` | 注册 `/orders/create` | ❌ **缺失** |
| 订单列表 | `frontend/src/views/Orders.vue` | 展示用户订单、取消 pending | ✅ 可用 |
| 订单详情 | `frontend/src/views/OrderDetail.vue` | 展示订单与明细 | ✅ 可用（取消按钮可选 P2） |
| API · 订单 | `frontend/src/api/order.js` | `createOrder({addressId, remark})` | ✅ 已有，结算页待调用 |
| API · 地址 | `frontend/src/api/address.js` | `getAddresses()` 等 | ✅ 已有，结算页待调用 |
| API · 购物车 | `frontend/src/api/cart.js` | 获取选中项与 summary | ✅ 已有 |

### 1.3 后端模块

| 模块 | 类/路径 | 职责 | 现状 |
|---|---|---|---|
| 订单控制器 | `OrderController` | `POST /orders`、`GET /orders`、`PUT /orders/{id}/cancel` | ✅ |
| 订单服务 ★ | `OrderService.createOrder` / `cancelOrder` | 校验、扣积分/库存、落库、清空购物车 | ✅ 基本逻辑；地址必填/归属待加固 |
| 购物车服务 | `CartService` | 加购、summary | ⚠️ 缺 toggle select API |
| 地址服务 | `AddressService` | CRUD + 归属校验 | ✅ |
| 用户积分 | `UserService.updatePoints` | 扣减/退还 | ✅ |
| 实体 | `Order`, `OrderItem`, `Cart`, `Address`, `Product`, `PointsHistory` | 持久化 | ✅ |

### 1.4 API 契约（结算域摘要）

| Method | Path | Body | 说明 |
|---|---|---|---|
| GET | `/api/cart` | — | 含 `cartItems[]` 与 `summary{selectedCount,totalPoints,...}` |
| PUT | `/api/cart/{id}/select` | `{isSelected: 0\|1}` | **待实现** — 勾选 toggle |
| GET | `/api/addresses` | — | 按 `isDefault` 降序 |
| POST | `/api/orders` | `{addressId, remark?}` | 创建订单；`addressId` **须必填且归属当前用户** |
| GET | `/api/orders` | — | 用户订单列表 |
| GET | `/api/orders/{id}` | — | 订单详情 + items |
| PUT | `/api/orders/{id}/cancel` | — | 仅 `pending` 可取消 |

### 1.5 原型 ↔ 实现映射（L2）

| 原型区块（`order.html`） | 实现落点 |
|---|---|
| 步骤条 ①购物车 → ②核对 → ③成功 | Checkout 页顶步骤指示；成功后跳转订单详情/列表 |
| 收货人信息（地址单选列表） | `GET /addresses` + 默认选中 `isDefault=1` |
| 商品清单（选中购物车项） | 来自 `GET /cart` 中 `isSelected=1` 的项 |
| 订单备注（≤100 字） | `remark` 字段传入 `POST /orders` |
| 右侧应付积分合计 | 复用 cart summary 或前端累加选中项 |
| 积分不足提示条 | 展示服务端错误或提交前预览（以服务端为准） |
| 提交订单按钮 | 调用 `POST /orders` |
| 错误 banner + 结果弹窗 | 展示 API 错误 message；成功后可 modal 或跳转 |
| 演示控制条（demo-bar） | **仅原型** — 生产页 MUST NOT 包含 |

---

## 2. 术语表

| 术语 | 定义 |
|---|---|
| **积分** | 用户兑换商品的唯一「货币」；存储于 `users.points`；下单时一次性扣减 |
| **选中商品** | 购物车中 `cart.is_selected = 1` 的条目；仅选中项参与结算 |
| **结算页** | 路由 `/orders/create`；用户确认地址与合计后提交订单的页面 |
| **订单快照** | 下单时将地址收件人/电话/完整地址写入 `orders.receiver*` 字段，与地址表解耦 |
| **待处理（pending）** | 订单创建后的默认状态；已扣积分与库存 |
| **服务端状态** | `orders.status` 的合法值与转换规则 **仅由服务端定义与持久化**；前端只读展示 |
| **ORDER_001** | 下单业务失败统一错误码（`RuntimeException` → 控制器包装） |
| **ORDER_002** | 订单查询/操作权限或不存在错误码 |

---

## 3. 订单状态机

> **原则：服务端为状态唯一计算方。** 前端、原型、管理端 UI 均 MUST NOT 自行推断或改写订单状态；所有状态变更 MUST 经 API 落库后，客户端再读取展示。

### 3.1 状态定义

| 状态 | 含义 | 用户可见文案建议 |
|---|---|---|
| `pending` | 待处理（下单默认） | 待处理 |
| `processing` | 处理中 | 处理中 |
| `shipped` | 已发货 | 已发货 |
| `completed` | 已完成 | 已完成 |
| `cancelled` | 已取消 | 已取消 |

### 3.2 状态转换（权威规则）

```
                    createOrder
                        │
                        ▼
                   ┌─────────┐
         ┌────────│ pending │────────┐
         │        └─────────┘        │
  cancelOrder                  Admin updateOrderStatus
  (用户, 本 PRD)                      │
         │                            ▼
         │              pending → processing → shipped → completed
         ▼                            │
    ┌───────────┐                     │ (Admin 可任意设值，无校验 — 已知债)
    │ cancelled │◀────────────────────┘
    └───────────┘
         ▲
         │ cancelOrder (用户)
         └─ 副作用：退还积分 + 回滚库存 + PointsHistory(type=refund)
```

### 3.3 用户路径（本 PRD 范围）

| 转换 | 触发 | 前置条件 | 副作用 |
|---|---|---|---|
| → `pending` | `POST /orders` | 见 §5 验收表 AC-01~AC-06 正向 | 扣积分、减库存、写 PointsHistory(spend)、删已下单 cart 项 |
| `pending` → `cancelled` | `PUT /orders/{id}/cancel` | 订单归属当前用户；当前状态为 `pending` | 退积分、回滚库存、PointsHistory(refund) |

### 3.4 客户端约束

- 前端 MUST 从 API 响应读取 `order.status`，MUST NOT 本地伪造状态（如原型中 `ORD+timestamp` 仅作演示）。
- 取消按钮 MUST 仅在服务端返回 `status === 'pending'` 时展示/启用。
- 非 `pending` 状态调用取消 MUST 展示服务端错误：「只能取消待处理状态的订单」。

---

## 4. 功能需求（正向摘要）

### 4.1 购物车 → 结算入口

- 用户勾选购物车商品（须接通 toggle API 或等价方案）。
- 点击「结算」进入 `/orders/create`；若无选中商品，应阻止进入或在结算页提示。

### 4.2 结算页

- 加载当前用户地址列表；默认选中 `isDefault=1` 的地址（若存在）。
- 展示已选购物车商品：名称、单价积分、数量、小计；库存不足时在商品行展示警告（展示性；**提交以服务端校验为准**）。
- 展示合计：件数、总积分、用户当前可用积分（来自用户信息或独立接口）。
- 可选备注，最长 100 字符。
- 点击「提交订单」调用 `POST /orders`，body：`{ addressId, remark? }`。
- 成功：跳转订单详情或列表，展示订单号与 `pending` 状态。
- 失败：展示服务端 `message`（banner 和/或弹窗，对齐原型错误态）。

### 4.3 后端加固（与原型对齐的必须项）

- `addressId` 必填；缺失时拒绝下单。
- 地址 MUST 属于当前 `userId`。
- 无选中购物车商品、库存不足、积分不足、地址不存在/不归属：返回 ORDER_001 及可读 message。
- （P1）商品 `status=active` 校验；（P2）并发锁与幂等不在本 PRD 阻塞范围。

---

## 5. 负向需求（MUST NOT）

| ID | 约束 |
|---|---|
| N-01 | MUST NOT 接入真实支付网关、银行卡、微信/支付宝等支付流程 |
| N-02 | MUST NOT 实现优惠券、满减、促销码、运费券 |
| N-03 | MUST NOT 按店铺/仓库拆单；一次提交产生 **一个** 订单 |
| N-04 | MUST NOT 在前端本地完成「伪下单」（扣积分/减库存仅能在服务端 `@Transactional` 内完成） |
| N-05 | MUST NOT 由前端写入或猜测 `orders.status`；状态展示必须来自 API |
| N-06 | MUST NOT 允许用户取消非 `pending` 订单（须服务端拒绝） |
| N-07 | MUST NOT 在结算页保留原型 `demo-bar` 演示控件 |
| N-08 | MUST NOT 将管理端改状态、物流、售后纳入本次用户路径 |
| N-09 | MUST NOT 在无地址时静默下单（当前后端缺口须在本 change 关闭） |
| N-10 | MUST NOT 使用他人 `addressId` 下单（须归属校验） |

---

## 6. 验收规则表

> **Coverage 最小集映射**：下表 `Cov-ID` 对齐 `project-baseline.md` §13 测试矩阵 T1–T10（`prep/coverage-scope.md` 待 L3 正式化，本 PRD 以 T1–T10 为最小集占位）。

| AC-ID | 场景 | 前置条件 | 操作 | 预期结果 | 优先级 | Cov-ID |
|---|---|---|---|---|---|---|
| AC-01 | 正常下单闭环 | user1 登录；购物车有选中项；积分≥合计；库存充足；有默认地址 | 购物车 → 结算 → 确认地址 → 提交 | HTTP 200；订单 `status=pending`；`totalPoints` 正确；用户积分减少；商品库存减少；已下单 cart 项清除；跳转订单列表/详情 | P0 | T1 |
| AC-02 | 购物车无选中 | 全部 `is_selected=0` | 提交订单 | ORDER_001；message 含「没有选中的商品」；无新订单；积分/库存不变 | P0 | T2 |
| AC-03 | 库存不足 | 某选中商品 `stock < quantity` | 提交 | ORDER_001；message 含商品名与库存不足；无新订单；积分/库存不变 | P0 | T3 |
| AC-04 | 积分不足 | 用户积分 < 订单合计 | 提交 | ORDER_001；message 含当前积分与需要积分；无新订单 | P0 | T4 |
| AC-05 | 未选地址 | 结算页未选择地址（`addressId` 缺失） | 提交 | ORDER_001；前端提示「请选择收货地址」；后端拒绝；无新订单 | P0 | T5 |
| AC-06 | 地址不属于用户 | 伪造他人 `addressId` | 提交 | ORDER_001；拒绝；无新订单 | P0 | T6 |
| AC-07 | 取消 pending 订单 | 存在 `pending` 订单 | 订单列表取消 | `status=cancelled`；积分退还；库存回滚；PointsHistory refund | P1 | T7 |
| AC-08 | 取消非 pending | 订单为 `processing/shipped/completed` | 取消 | 拒绝；message「只能取消待处理状态」；状态与积分/库存不变 | P1 | T8 |
| AC-09 | 并发双下单抢库存 | 两请求同时抢最后 1 件库存 | 并发提交 | 仅一单成功（P2 理想）；工作坊最低要求：不出现负库存 | P2 | T9 |
| AC-10 | 前端结算 E2E | 页面与路由已实现 | 选地址 → 提交 → 查看结果 | 主路径可走通；至少一条异常（AC-03/04/05 之一）有可见错误态 | P0 | T10 |
| AC-11 | 步骤与区块齐全 | 打开结算页 | 目视检查 | 含地址区、商品清单、备注、积分合计、提交按钮；风格 L2 对齐原型 | P0 | — |
| AC-12 | 默认地址预选 | 用户有 `isDefault=1` 地址 | 进入结算页 | 该地址默认选中 | P1 | — |
| AC-13 | 备注透传 | 填写备注 ≤100 字 | 提交后查订单详情 | `remark` 与提交一致 | P2 | — |
| AC-14 | 订单列表状态只读 | 下单成功后 | 查看列表/详情 | 展示服务端返回的 `pending`，非前端写死 | P0 | T1 |
| AC-15 | 购物车勾选接通 | 购物车页 | 勾选/取消勾选 | `is_selected` 持久化；summary 随选中变化 | P0 | — |

### 6.1 错误文案参考（与原型/现实现对齐）

| 场景 | 服务端 message（参考） | 前端展示要求 |
|---|---|---|
| 未选地址 | 「请选择收货地址」或等价 | banner + 滚动至地址区（对齐原型 `showError`） |
| 库存不足 | 「商品 {name} 库存不足」 | 商品行警告 + 提交失败弹窗/banner |
| 积分不足 | 「积分不足，当前积分：{n}，需要积分：{m}」 | 合计区积分缺口提示 + 提交失败 |
| 无选中商品 | 「购物车中没有选中的商品」 | 结算页或购物车拦截 |

---

## 7. prd-prototype-review（原型对照）

> 以下问题记入 **compass**（工作坊罗盘），供 L3 对齐与走查跟踪。

| # | 类型 | 原型表现 | PRD/基线要求 | 建议处置 | Compass |
|---|---|---|---|---|---|
| R-01 | 范围 | 原型本地 `validateAndSubmit` 模拟成功 | 必须调 `POST /orders` | L3 实现时删除本地伪下单 | 📌 compass |
| R-02 | 范围 | 原型含 `demo-bar` 演示控件 | N-07 生产页不得包含 | 仅保留于 `order.html` | 📌 compass |
| R-03 | 数据 | 原型 Mock 固定 2 地址、2 商品 | 须接 API 动态加载 | Checkout.vue 调 cart/addresses | 📌 compass |
| R-04 | 文案 | 成功弹窗「积分已冻结」 | 基线：积分 **扣减** 非冻结 | 成功文案改为「已扣除 {n} 积分」 | 📌 compass |
| R-05 | 流程 | 成功后仅关弹窗，无跳转 | AC-01 要求进订单列表/详情 | 成功后 `router.push` | 📌 compass |
| R-06 | 缺口 | 原型无「新增地址」入口 | MVP 可仅选已有地址；新增地址 P2 | L3 tasks 标注：结算页内嵌新增 vs Profile | 📌 compass |
| R-07 | 后端债 | 原型不校验地址归属 | AC-06 必须后端校验 | OrderService 加固 | 📌 compass |
| R-08 | 购物车 | 原型不经过购物车勾选 | 须从 cart 选中项带入 | Cart toggle + 结算页读选中项 | 📌 compass |
| R-09 | 状态 | 原型步骤③本地点亮 | 服务端 pending 为准 | 成功跳转后再展示状态 | 📌 compass |
| R-10 | 样式 | 京东红 L2 原型 | 现有 Cart 为紫色按钮 | Checkout 页向 L2 原型靠拢，Cart 样式统一可选 P2 | 📌 compass |

---

## 8. 开放问题（待人闸 / L3）

| # | 问题 | 建议默认 |
|---|---|---|
| O-01 | 新增地址 UI 放结算页还是 Profile？ | 工作坊默认：结算页仅选择；新增放 Profile 或后续迭代 |
| O-02 | 下单成功跳转列表还是详情？ | 默认订单详情 `/orders/{id}` |
| O-03 | JDK 17 vs 21 | 以 pom 为准（17） |
| O-04 | `coverage-scope.md` 包范围 | L3 与 AC 表 Cov-ID 对齐后回填 |

---

## 9. 人闸检查清单（人闸-L2-PRD）

- [ ] 状态机是否明确「服务端为唯一计算方」？
- [ ] 负向需求 N-01~N-10 是否清晰且无支付/优惠券/拆单偷渡？
- [ ] 验收表是否覆盖闭环 + 状态 + 库存/积分/地址异常？
- [ ] Cov-ID 是否映射 T1–T10 最小集？
- [ ] 与已确认 `order.html` 原型在区块与错误态上一致？
- [ ] visual_level L2 是否标注？

---

*生成：L2-2 opsx-explore · 基于 `product/order.html` + `product/baseline/project-baseline.md` · 2026-08-04*

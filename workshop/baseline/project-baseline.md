# Project Baseline — P0（无人值守干跑）

**Indexed repo (MCP):** `market_workshop`  
**Path:** `E:\01工作坊s\ywpt_workshop_test`  
**GitNexus:** ~960 nodes / ~1589 edges（见 `prep/gitnexus-queries.md` Last indexed）

## 技术栈

| 层 | 技术 | 模块路径 |
|----|------|----------|
| 后端 | JDK 21 · Spring Boot 3 · JPA · SQLite · JWT | `backend/` |
| 前端 | Vue 3 · Vue Router · Axios · Vite（port **3000**） | `frontend/` |
| 上游 | 无独立 BFF；前后端同仓直连 `/api` | — |
| 测试 | 存量几乎无自动化测试；workshop 目标核心包 95% | `workshop/prep/coverage-scope.md` |

## 业务模块（已有）

- 认证 / 用户 / 积分历史
- 商品 / 分类 / 收藏
- 购物车
- 订单：列表、详情、取消、`OrderService#createOrder`
- 管理端：用户 / 商品 / 订单 / Dashboard
- 地址

## 下单结算相关流程（GitNexus）

- `OrderController.createOrder` → `OrderService.createOrder` → `CartRepository.findByUserId` / 扣库存 / `UserService.updatePoints`
- 前端 `Cart.vue#checkout` → `router.push('/orders/create')`
- `orderApi.createOrder` 已存在

## 缺口清单（半成品 → 本场要补）

| ID | 缺口 | 严重度 | 建议落点 |
|----|------|--------|----------|
| G1 | **无结算页**：路由 `/orders/create` 无对应 view/router 注册 | 阻塞 | 新建 `OrderCreate.vue` + router |
| G2 | **地址可选**：`addressId==null` 仍可下单，与中等 MVP「须选地址」冲突 | 高 | `OrderService` 校验必填 |
| G3 | 错误态文案/前端展示不统一（RuntimeException 字符串） | 中 | Controller/前端统一提示 |
| G4 | 无 UT/API/集成/E2E 覆盖结算核心包 | 高（质量门） | 按 coverage-scope 最小集 |
| G5 | 状态机仅 `pending`/`cancelled`（及可能的 paid）；PRD 需冻结语义 | 中 | PRD + 不做真实支付 |

## 非目标（确认）

- 真实三方支付、优惠券、拆单、完整售后

## 建议 OpenSpec change 名

`order-checkout-hardening`

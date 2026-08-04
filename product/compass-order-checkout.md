# 罗盘 · 下单结算（order-checkout）

> **用途**：工作坊 L2–L3 跟踪 PRD / 原型 / OpenSpec / 实现差距。  
> **最后更新**：2026-08-04（L3-4 apply 完成）  
> **关联 PRD**：[`PRD-order-checkout.md`](PRD-order-checkout.md)（V1.0 · 人闸清单已勾）  
> **关联原型**：[`order.html`](order.html)（L2 · 含 demo-bar，仅演示）  
> **关联 change**：[`openspec/changes/order-checkout-hardening/`](../openspec/changes/order-checkout-hardening/)（**实现 22/22 必做项** · §7 可选 defer）  
> **Superpowers**：[`docs/superpowers/specs/order-checkout-hardening.md`](../docs/superpowers/specs/order-checkout-hardening.md) · [`docs/superpowers/plans/2026-08-04-order-checkout-hardening.md`](../docs/superpowers/plans/2026-08-04-order-checkout-hardening.md)

---

## 阶段快照

| 环节 | 状态 | 说明 |
|------|------|------|
| L1 基线 | ✅ | `product/baseline/project-baseline.md` |
| L2 原型 | ✅ | `product/order.html` |
| L2 PRD | ✅ | `PRD-order-checkout.md` V1.0 |
| L2 prd-prototype-review | ✅ | 本文件 QG + 待修复项 |
| L3-1 propose | ✅ | proposal / design / specs(×4) / tasks |
| L3-2 对抗 | ✅ | R1 全量 + R2 完整包 |
| L3-3 plans | ✅ | superpowers specs + minute-level plan |
| L3-4 apply | ✅ | 后端 cart select + D9 + Checkout + 测试 T1–T8 |
| L3-5 测试 | ✅ | `OrderCheckoutIntegrationTest` 10 cases PASS |
| L3-6 走查 | ⚠️ | 代码就绪；启服目视 AC-11 待人闸 |

**实现进度（`tasks.md`）**：22 / 22 必做项完成（§7 可选 4 项 defer）

---

## QG 结果汇总

| QG | 结果 | 优先级 | 说明 |
|----|------|--------|------|
| QG-01 | **pass** | — | PRD 含修订记录、术语、状态机、功能、验收、参考 |
| QG-02 | **partial** | P2 | 结算页为卡片布局；§4.1 字段矩阵已覆盖 |
| QG-03 | **pass** | — | `order.status` 五态与实体一致 |
| QG-04 | **pass** | — | Checkout.vue 无 demo-bar |
| QG-05 | **partial** | P2 | O-01 新增地址 defer；O-02 D4 成功跳详情 ✅ |
| QG-06 | **pass** | — | OpenSpec 与 PRD/原型对齐 |
| QG-07 | **pass** | — | Checkout + cart select + POST /orders 闭环已实现 |
| QG-08 | **pass** | — | Service + Controller D9 addressId 规范化 |
| QG-09 | **pass** | — | Checkout L2 token `#e4393c`、860px 单列 |
| QG-10 | **N/A** | — | 非 L4 |

---

## AC ↔ OpenSpec 映射（QG-06）

| PRD AC | Cov-ID | OpenSpec capability | Spec 场景 | 代码落点 |
|--------|--------|---------------------|-----------|----------|
| AC-01~06 | T1–T6 | `order-checkout-create` | 6 | `OrderService` + `OrderController` ✅ |
| AC-07~08 | T7–T8 | `order-checkout-cancel` | 3 | `OrderService.cancelOrder` + 集成测试 ✅ |
| AC-09 | T9 | — | defer | 并发未测 |
| AC-10~12,14 | T10 | `order-checkout-ui` | 8 | `Checkout.vue` ✅（E2E 待人闸） |
| AC-13 | — | `order-checkout-ui` | remark | `OrderDetail.vue` ✅ |
| AC-15 | — | `cart-selection` | 5 | `CartService` + `Cart.vue` ✅ |

---

## 待修复项（PRD ↔ 原型 ↔ 实现）

| # | QG | 位置 | 问题 | 建议 / 任务 | 状态 |
|---|-----|------|------|-------------|------|
| C-01 | QG-07 | 实现 | 结算页未调 `POST /orders` | tasks §4.4 | ✅ done |
| C-02 | QG-04 | 原型 `demo-bar` | 工作坊演示控件 | Checkout 无 demo-bar | ✅ done |
| C-03 | QG-07 | 实现 | 无 Checkout 接 API | tasks §4.3 | ✅ done |
| C-04 | QG-07 | 文案 | 原型「积分已冻结」 | frontend 无该文案 | ✅ done |
| C-05 | QG-07 | 流程 | 原型成功仅关 modal | D4 → `/orders/{id}` | ✅ done |
| C-06 | QG-05 | PRD O-01 | 无新增地址入口 | MVP defer Profile P2 | ⚠️ defer |
| C-07 | QG-08 | `OrderService` | addressId 校验 | tasks §2.1–2.2 | ✅ done |
| C-08 | QG-07 | 购物车 | toggle 未接通 | tasks §1.x + §3.x | ✅ done |
| C-09 | QG-07 | 原型步骤③ | 本地点亮 | 成功跳详情读 API status | ✅ done |
| C-10 | QG-07 | `Cart.vue` | 紫色按钮 vs L2 红 | tasks §7.4 P2 | ⬜ P2 defer |
| C-11 | QG-06 | OpenSpec | change 未建 | order-checkout-hardening | ✅ done |
| C-12 | QG-08 | 路由/页面 | 无 `/orders/create` | Checkout + router | ✅ done |
| C-13 | — | 测试 | 零测试 | T1–T8 集成测试 | ✅ done |
| C-14 | — | 走查 | 闭环不可演示 | §6.1 启服目视 | ⚠️ 待人闸 |

---

## 代码现状速查（2026-08-04 · apply 后）

| 检查项 | 现状 |
|--------|------|
| `frontend/src/views/Checkout.vue` | ✅ L2 区块 + banner/modal |
| `router` `/orders/create` | ✅ 已注册 |
| `PUT /api/cart/{id}/select` | ✅ |
| `Cart.vue` `toggleSelect` | ✅ 调用 API |
| `OrderController` D9 addressId | ✅ parseAddressId |
| `OrderService` addressId 必填 | ✅ |
| `OrderDetail.vue` remark | ✅ |
| 集成测试 | ✅ 10 tests PASS |
| T9 并发 | defer §5.5 |
| tasks §7 可选 | defer |

---

## L3 行动优先级（更新）

```
✅ 已完成（L3-4 apply）
  C-01~C-05, C-07~C-09, C-11~C-13
  superpowers specs + plans
  OrderCheckoutIntegrationTest T1–T8

⚠️ 待人闸
  C-14  启服走查 AC-11 / T10 E2E 目视

P2 — 可选（tasks §7）
  C-06  新增地址 UI
  C-10  Cart 样式统一 L2
  status=active / balanceAfter / 详情取消按钮 / T9 并发
```

---

## 变更日志

| 日期 | 变更 |
|------|------|
| 2026-08-04 初版 | L2 prd-prototype-review |
| 2026-08-04 更新 | L3-2 R2 完整包 |
| 2026-08-04 apply | L3-4 全量实现 + T1–T8 测试 PASS；QG-07 pass |

---

*维护：走查完成后更新 C-14；`opsx-archive` 后迁入 rule/skill。*

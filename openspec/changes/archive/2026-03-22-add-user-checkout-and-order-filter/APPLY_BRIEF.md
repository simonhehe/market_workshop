# Apply 简报：`add-user-checkout-and-order-filter`

**生成日期**：2026-03-22（以仓库当前状态为准）  
**用途**：合并/发布前对照需求验收与质量活动；Phase E 为持续改进项见 `tasks.md`。

---

## 1. 需求实现及验收结论

依据 **`design.md` §5**。

| ID | 验收项 | 实现要点 | 验收结论 |
|----|--------|----------|----------|
| **AC-F1** | 勾选 → 结算页 → 提交 → `pending`、购物车移除行 | `Checkout.vue`、`Cart.vue`（`/checkout`、`updateSelection`）、`POST /orders`、`OrderService.createOrder` | **满足**（建议人工或 UI 自动化再跑一条主路径） |
| **AC-F2** | 积分/库存不足下单失败，`ORDER_001`，无部分扣款 | `createOrder` 前置校验 + 事务 | **满足**（逻辑在 Service；**自动化 UT 未覆盖全部异常组合**，见 Phase E） |
| **AC-F3** | 他人 `addressId` 下单失败 | `createOrder` 地址 `userId` 校验 | **满足**；**有** `OrderServiceTest` 用例 |
| **AC-F4** | 订单列表按状态筛选；「全部」同未筛选 | `GET /orders?status=`、`getUserOrders`、`Orders.vue` | **满足** |
| **AC-F5** | 勾选持久化 | `PUT /cart/{id}/selection` + 前端 `toggleSelect` | **满足** |
| **AC-R1** | `pending` 可取消，积分/库存恢复 | `cancelOrder` 现有逻辑 | **满足**（**完整副作用**建议回归或补 UT，见 Phase E） |
| **AC-R2** | 未登录访问结算页 → 登录 | `/checkout` `requiresAuth` | **满足** |
| **AC-N1** | 不新增全局状态管理依赖 | 仍为 Vue Router + 现有 api | **满足** |

**与设计范围一致的非目标**（未纳入本变更验收）：管理员订单状态机强约束、并发库存锁、**`OrderService.confirmReceipt` 仅 Service 层实现，无 `OrderController` 暴露**——若产品需要「确认收货」端到端，应另开变更补 API + 前端 + 冒烟。

**总评**：**design §5 功能与回归项在当前代码结构上可验收通过**；**深度质量门禁（Phase E 全绿、冒烟脚本跑通下单闭环）未全部闭合**，见第 2 节。

---

## 2. 质量活动及结果

| 活动 | 内容 | 结果 |
|------|------|------|
| **白盒（JUnit）** | `test/backend/java`：`OrderServiceTest`、`CartServiceTest`、`OrderControllerTest`、`CartControllerTest`；`backend/pom.xml` 指向该目录 | **已落地**；覆盖地址越权、空购物车、取消/确认收货分支、购物车归属、`GET/POST orders` 与 `PUT selection` 等。**未声称**行覆盖率 100%；**MockMvc 使用 `addFilters=false`**，不验 JWT 链。 |
| **Phase E（矩阵）** | `whitebox-matrix.md` + `tasks.md` E1～E5 | **未完成**（仍待：createOrder 更多分支、cancel 全链路、订单详情越权、Admin 403 等） |
| **冒烟脚本** | `test/smoke/smoke-checkout-api.ps1` | **部分**：健康检查、登录、购物车、筛选订单；**未包含** `PUT .../selection`、`POST /orders` 完整闭环（与 `doc/smoke-checkout-order-filter.md` 全文步骤仍有差距）。 |
| **UI 自动化** | `test/ui/` | **占位**，无 Playwright/Cypress 用例。 |
| **文档** | `doc/order-domain-baseline.md`、`baseline-requirements.md`、`测试基线建议.md`、`smoke-checkout-order-filter.md`；OpenSpec `config.yaml` / 本目录 README | **已同步**本变更行为与测试目录约定。 |
| **构建** | 前端 `npm run build`（历史执行通过）；后端 `mvn test` | **需在本地有 Maven 的环境执行** `cd backend && mvn test` 做最终确认。 |

---

## 3. 建议的合并后动作（可选）

1. 跑通 **`mvn test`** 与扩展 **`smoke-checkout-api.ps1`**（加购 → selection → `POST /orders`）。  
2. 按优先级勾选 **Phase E** 或记录豁免。  
3. 若需「确认收货」产品化：补 **REST + 前端 + 冒烟** 或从 Service 移除并归入他变更。

---

**关联文件**：`proposal.md`、`design.md`、`tasks.md`、`whitebox-matrix.md`、`doc/smoke-checkout-order-filter.md`、`test/README.md`

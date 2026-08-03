# Quality Summary — P4（无人值守）

**Scope:** `workshop/prep/coverage-scope.md`  
**Change:** `order-checkout-hardening`

## 覆盖率

| 项 | 结果 |
|----|------|
| OrderServiceTest | **5 tests PASS** (`mvn -Dtest=OrderServiceTest`) |
| JaCoCo 行/分支 ≥95%（白名单） | **未出正式数字报告** — pom 已加 jacoco plugin；本场标记 **degraded** |
| 前端覆盖率 | 未配 Vitest — 不计入 |

**结论:** **不宣称质量通过**（degraded=true）。UT 最小路径已绿；95% 硬门禁留课后补齐。

## 用例结果

| ID | 类型 | 场景 | 结果 |
|----|------|------|------|
| UT-01 | UT | 下单成功 | PASS (OrderServiceTest) |
| UT-02 | UT | 库存不足 | PASS |
| UT-03 | UT | 积分不足 | PASS |
| UT-04 | UT | 地址缺失 | PASS |
| UT-05 | UT | 取消退积分 | PASS |
| API-01 | API | 创建订单 | PASS（手工走查） |
| API-ADDR | API | 无地址 | PASS |
| API-02 | API | 未鉴权 | 未单独跑（现有过滤器） |
| IT-01 | 集成 | 事务一致性 | 部分覆盖于 UT mocks — degraded |
| E2E-01 | E2E | 浏览器主路径 | 债项 — 未跑 Playwright |
| E2E-02 | E2E | 异常 | API 级已覆盖无地址 |

## 与金丝雀样例差异

| 项 | 金丝雀样例 | 本场 |
|----|------------|------|
| 行/分支 ≥95% | 是 | 否（degraded） |
| E2E | 是 | 否 |
| UT 核心路径 | 是 | 是 |

## AC 追溯

| AC-ID | 证据 |
|-------|------|
| AC-CREATE | UT + API 走查 |
| AC-STOCK | UT |
| AC-POINTS | UT |
| AC-ADDR | UT + API |
| AC-CANCEL | UT + API |
| AC-ROUTE | 代码：router `/orders/create` 在 `:id` 前 |
| AC-AUTH | 债项 |

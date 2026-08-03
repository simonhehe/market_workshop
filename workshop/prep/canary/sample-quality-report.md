# 样例质量报告（金丝雀形状）

**Scope:** 见 `../coverage-scope.md`  
**行覆盖率:** 96.2%  
**分支覆盖率:** 95.1%  
**结论:** 通过（样例）

## 用例结果

| ID | 类型 | 场景 | 结果 |
|----|------|------|------|
| UT-01 | UT | 下单成功 | PASS |
| UT-02 | UT | 库存不足 | PASS |
| UT-03 | UT | 积分不足 | PASS |
| UT-04 | UT | 地址无效 | PASS |
| API-01 | API | 创建订单契约 | PASS |
| API-02 | API | 未鉴权 | PASS |
| IT-01 | 集成 | 库存+积分一致性 | PASS |
| E2E-01 | E2E | 主路径 | PASS |
| E2E-02 | E2E | 异常路径 | PASS |

## AC 追溯

| AC-ID | 用例 |
|-------|------|
| AC-CREATE | UT-01, API-01, E2E-01 |
| AC-STOCK | UT-02, E2E-02 |
| AC-POINTS | UT-03 |
| AC-ADDR | UT-04 |
| AC-AUTH | API-02 |

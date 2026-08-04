# 结算链路测试执行报告

> 生成时间：2026-08-04T13:56:25.348Z
> 白名单：`workshop/prep/coverage-scope.md`

## 硬门禁（行 ≥95% 且 分支 ≥95%）

| 层 | 行覆盖率 | 分支覆盖率 | 结果 |
|---|---:|---:|---|
| 后端（白名单类） | 100% | 95% | PASS |
| 前端（白名单文件） | 100% | 95.77% | PASS |
| **综合** | — | — | **PASS** |

## 测试层级

| 层级 | 命令 | 结果 |
|---|---|---|
| UT | `frontend npm run test:coverage` | 51 tests |
| API | `backend mvn verify` | 95 tests |
| E2E | `frontend npm run e2e` | PASS (3/3) |

## E2E 结果

- 通过 3/3 · PASS

## AC 映射摘要

| Cov-ID | 场景 | UT | API | E2E |
|---|---|---|---|---|
| T1 | 正常下单 | Checkout submit | createOrder_success | T10 journey |
| T4 | 无选中商品 | empty hints | createOrder_noSelectedItems | — |
| T5 | 库存不足 | stock warn | createOrder_insufficientStock | — |
| T6 | 积分不足 | points gap | createOrder_insufficientPoints | — |
| T7 | 地址必填 | empty mock submit | createOrder_missingAddressId | empty mock e2e |
| T10 | 结算页 E2E | step link | — | checkout.spec |

## 备注

- 实现先于部分 UT 补齐（retroactive TDD）；新增 `checkout/**` 纯函数模块按 TDD 编写。
- 非目标模块（管理端、收藏、商品浏览等）未计入覆盖率分母。

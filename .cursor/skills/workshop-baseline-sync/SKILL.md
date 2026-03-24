---
name: workshop-baseline-sync
description: >-
  在本福利商城仓库完成代码探索或功能改动后，同步基线文档、openspec/config.yaml
  上下文与交叉引用。适用于：用户完成分析/校核、修改订单/购物车/积分/鉴权/API、
  要求文档与代码对齐、或提及 doc/baseline、doc/analysis、openspec/config.yaml。
---

# 基线与文档同步（本仓库工作流）

## 何时使用

- 用户或 Agent 刚做完**探索 / 校核**，需要把结论**写回仓库**。
- 改动涉及**订单、购物车、积分、JWT、管理员接口**等路径。
- 用户要求**对齐实现与文档**，或刷新 **OpenSpec** 上下文。

## 流程清单

1. **事实**：只断言仓库里能核对的内容；未知统一 **`N/A`** 或 **待确认**（见规则 `evidence-and-uncertainty.mdc`）。
2. **OpenSpec**：若行为或依赖变化，更新 **`openspec/config.yaml`** 中的 `context`（如 domain、tech_stack、auth、database、core_flows、quality_risks）。
3. **基线**：若验收口径或架构变化，编辑 **`doc/baseline/`**（如 `baseline-requirements.md`、`baseline-design.md`、`order-domain-baseline.md`）。
4. **分析**：深度分析、矩阵、缺口等放在 **`doc/analysis/`**；同步维护 **`doc/README.md`** 索引表。
5. **链接**：`doc/baseline/` 与 `doc/analysis/` 之间用**相对路径**互链。
6. **测试**：行为或风险变化时，更新 **`doc/analysis/测试基线建议.md`** 或补充 `backend/src/test`；**不要**声称尚不存在的测试覆盖。核对 OpenSpec §6 门禁与归档前置条件时，可配合 **`workshop-verify-quality-gates`** 技能。

## 变更类型速查

| 变更类型 | 优先改动 |
|----------|-----------|
| 订单状态 / 退款 / 库存 | `order-domain-baseline.md`，技术文档中订单相关章节 |
| 新接口或错误码 | `doc/analysis/单链路基线分析_技术.md`（接口契约等章节），`openspec/config.yaml` |
| 鉴权 / 白名单路径 | `baseline-design.md`，`AuthMiddleware`，`openspec` 的 auth |
| 文档归类 | **`doc/README.md`** 中的规则 |

## 反模式（避免）

- 在仓库根目录新增**长说明** `.md`（根目录仅保留 `README.md` 作入口）。
- 无仓库内依据却写死「生产环境」「团队 SLA」等。
- 改订单状态逻辑时混淆**用户取消**与**管理员改状态**的语义。

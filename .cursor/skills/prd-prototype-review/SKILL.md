---
name: prd-prototype-review
description: >-
  检查 PRD 与 HTML 原型质量：QG-01～QG-10，输出结构化差距报告。
  触发：检查 PRD、审查原型、prd-prototype-review、需求评审。
---

# PRD / 原型检查（Review）

## 执行顺序

1. QG-01 结构完整（修订记录、术语、状态机、功能节、验收、参考）
2. QG-02 字段矩阵 ↔ HTML 表头/表单
3. QG-03 枚举一致
4. QG-04 负向需求 + 反模式（`@change` 查列表、close 完成 todo）
5. QG-05 冻结决策是否回写 PRD
6. QG-06 验收项 ↔ OpenSpec spec
7. QG-07 PRD vs 原型冲突
8. QG-08 数据模型 vs DDL/契约路径（只读）
9. QG-09 视觉等级：PRD §4.0 或等价节是否声明 `visual_level`（L0–L4）及样式对齐范围；有 HTML 原型但未声明 → **fail** + P1
10. QG-10 像素级范围（L4）：PRD 是否声明 `visual_level: L4`、双端表面清单（Web+H5 首页+下钻+drill 矩阵）；是否禁止「样式可逐步对齐」作为终态；有 HTML 原型且要求像素级但未列 20 drillId → **fail** + P0

## 报告格式

使用 [report-template.md](report-template.md)。

## 客诉清单

[checklists/customer-request.md](checklists/customer-request.md)

## 基线参考

- Kit 内：以 `product/<feature>-PRD.md` + `product/index.html` 为审查对象
- 完整业务范例（示范仓）：`e:\myharness` 下客诉 PRD/原型（本 Kit 不含）

## 硬约束

只读检查；**不修改**业务代码。

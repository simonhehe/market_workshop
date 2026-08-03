---
name: product-ui
description: >-
  产品 UI 视觉对齐（L2+）：从 product/ 原型移植 CSS 与 DOM class。
  触发：implement-visual、harness implement-visual、visual_level >= L2。
---

# Product UI — 视觉对齐

## 何时使用

- Harness `phase: implement-visual`
- 变更 `visual_level` 为 L2、L3 或 L4
- 修改目标项目前端视图（路径见 `00-project-context.mdc`）

## 权威来源（Kit 约定）

| 资产 | 路径 |
|------|------|
| HTML 原型 | `product/index.html`（或 `harness-state.yaml` → `artifacts.prototype_path`） |
| 原型样式 | `product/styles.css` |
| PRD visual_level | `product/<feature>-PRD.md` §4.0 |
| 对照清单 | `openspec/changes/<change>/ui-parity-checklist.md` |
| 映射表 | `openspec/changes/<change>/css-dom-map.md` |

## 目标项目路径（部署后填写）

在 `.cursor/skills/product-ui/SKILL.md` 或 `00-project-context.mdc` 中声明：

- 前端视图目录：`<!-- TODO: 如 frontend/src/views/<module>/ -->`
- 主题/样式入口：`<!-- TODO: 如 frontend/src/styles/<module>.css -->`
- 视觉回归 baseline：`<!-- TODO: 见 docs/project/playbook/visual-parity.md -->`

## 移植顺序

1. `:root` design token（背景、卡片、边框、字体等）
2. 布局壳层（顶栏、Tab、主内容区）
3. KPI / 指标卡片
4. 图表区
5. 表格与标签
6. 下钻 Modal / 抽屉（与原型 DOM 一致）

## 命名约定

- **优先复用原型 class 名**，禁止与原型并存第二套壳层 class
- Tab / 路由 id 与原型一致

## 禁止（implement-visual）

- 修改 BFF 契约、API composable 逻辑
- 用 UI 库默认组件皮覆盖原型关键 DOM（表格、卡片、下钻容器）
- teleport 到 body 导致 `:root` 主题变量丢失

## 验收

- 勾选 `ui-parity-checklist.md`
- `visual_level ≥ L2`：执行 `00-project-context` 或 `docs/project/playbook/visual-parity.md` 中的视觉回归命令

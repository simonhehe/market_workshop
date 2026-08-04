---
name: prd-prototype-authoring
description: >-
  撰写高质量 PRD 与 HTML 原型：章节模板、负向需求、分层决策、query-card 约定。
  触发：写 PRD、写原型、需求文档、prd-prototype-authoring。
---

# PRD / 原型撰写（Generation）

## 工作流

1. 文档头（版本、状态、原型引用、OpenSpec、修订记录）
2. 模块索引 M1…Mn
3. 术语 + 状态机（含服务端计算声明）
4. 逐模块：字段矩阵（六列）+ 枚举表
5. **负向需求**专节（MUST NOT）
6. **分层决策表**（同字段跨场景）
7. **验收规则表格**：按模块表格列（AC-ID / 场景 / 给定 / 当 / 则 / 追溯 / 测试）；§7.11 追溯总表
8. HTML：页面骨架 + QP-01 + 无 demo 行

## 客诉正例（必读）

| 模式 | 范例位置 |
|------|----------|
| 状态优先级 | PRD-v2 §3.2.1 |
| 负向需求 | PRD-v2 §3.4 |
| 分层决策 | PRD-v2 §3.5 |
| 交互索引 | PRD-v2 §4.0 |
| 附件 AT | PRD-v2 §4.1.5.1 |
| 状态与流程图 | PRD-v2 §3（Mermaid） |
| 列表列定义 | PRD-v2 §4.1.3 |
| 第三方接口 | PRD-v2 §4.0.1 |
| 新增/编辑差异 | PRD-v2 §4.1.5.0 |
| 验收规则表格 | PRD-v2 §7 |

## HTML 列表页骨架

```html
<!-- PRD-SECTION: 4.1.2 -->
<div class="form-card query-card">
  <el-form inline size="small" @submit.native.prevent>
    <!-- 控件带 cr-w-*；禁止 select @change 查数据 -->
    <el-form-item class="query-form-actions">
      <el-button type="primary" @click="handleQuery">查询</el-button>
      <el-button @click="handleReset">重置</el-button>
    </el-form-item>
  </el-form>
</div>
<div class="table-wrap">
  <el-table :data="[]" /><!-- 原型默认无 demo 行 -->
</div>
```

## 模板

- [templates/prd-outline.md](templates/prd-outline.md) — 章节骨架（含 Mermaid/列表表/验收表占位）
- [product/README.md](../../../product/README.md) — PRD / 原型放置约定
- 完整业务正例（示范仓，非本 Kit）：`e:\myharness` 下 `doc/`、`product/`

## visual_level 声明（§4.0 或等价节）

| 等级 | PRD 必须写明 |
|------|-------------|
| L2 | Token + 关键组件；允许 chrome 占位 |
| L3 | 单端全页 + chrome 像素级 |
| L4 | Web+H5 首页 + 下钻层 + 全部 drillId 像素级；**禁止**「样式可逐步对齐」作为验收终态 |

L4 须附 drillId 列表（与 `product/app.js` `drillData` 键名一致）。

## 禁止

- 在 rule/skill 重复 PRD 全文
- 本 skill 流程内修改业务代码（实现须另开 change；路径见 `00-project-context`）

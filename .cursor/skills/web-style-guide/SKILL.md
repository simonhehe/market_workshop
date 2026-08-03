---
name: web-style-guide
description: >-
  团队 Web 组件与表格规范（Element Plus 等）。与 product-ui 配合：有 HTML 原型时 views 跟原型。
  触发：新建 Vue 页面、表格、查询区。
---

# Web Style Guide

## 组件库

在 `00-project-context.mdc` 填写目标栈（如 Element Plus + Vue 3）。

## 与 product-ui 的分工

| 场景 | 权威 |
|------|------|
| 有 HTML 原型的 feature 页 | `product-ui` + `product/styles.css` |
| 纯后台 CRUD、无原型 | 本 skill |
| 例外 | 有原型时 **禁止** UI 库默认皮覆盖原型关键 DOM |

## 布局约定

### 有原型的 feature 页

- 查询/列表 DOM 与 `product/index.html` 一致
- 表格使用原型 class，非库默认表格壳

### 标准 CRUD 页（若无原型）

- 查询区与操作区分两行
- 分页：`v-model:current-page` / `v-model:page-size`

## E2E 选择器

- 优先 `data-testid` 或原型语义 class（见 `css-dom-map.md`）

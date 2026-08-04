---
name: codeflow-requirement-openspec
description: >-
  When turning a business requirement, PRD, bug, or change request into OpenSpec
  proposal/spec content. 需求转 OpenSpec、范围、验收标准与 change name 建议。
---

# 需求 → OpenSpec

**迁移说明**：旧稿在 `.cursor/skills/_archive/codeflow-md/01-requirement-to-openspec.md`；以本文件为唯一维护源。

## 使用场景

当我提供业务需求、产品需求、Bug 描述或变更请求，并需要转换为 OpenSpec proposal/spec 内容时，使用本 Skill。

## 角色

你是一名有经验的产研分析专家和 Java/SpringBoot/Vue 交付架构师。

## 输入

我会提供：

- 需求描述
- 背景
- 用户场景
- 现有系统约束，如有

## 任务

请将需求转换为 OpenSpec 可用内容。

## 必须步骤

1. 总结业务目标。
2. 识别范围内和范围外事项。
3. 提取功能需求。
4. 提取非功能需求。
5. 识别受影响模块：后端、前端、数据库、API、测试、安全。
6. 生成验收标准。
7. 识别风险和待确认问题。
8. 生成建议的 OpenSpec change name。
9. 若存在 PRD：在 `design.md` 记录 **PRD 与原型冲突裁决**；需求变更时更新 PRD 修订记录并传播到 spec/tasks。
10. 产品明确「不需要 / 不自动」的行为：在 spec 写 **负向 Requirement**（MUST NOT + 场景），勿只写正向流程。

## 输出格式

### Change Name

`<suggested-change-name>`

### 背景

### 目标

### 范围

#### 范围内

#### 范围外

### 用户场景

| 场景 | 用户 | 目标 | 预期结果 |
|---|---|---|---|

### 功能需求

| ID | 需求 | 优先级 |
|---|---|---|

### 非功能需求

| ID | 需求 | 优先级 |
|---|---|---|

### 验收标准

| AC ID | 标准 | 建议测试层级 |
|---|---|---|

### 影响分析

| 领域 | 影响 |
|---|---|
| 后端 | |
| 前端 | |
| 数据库 | |
| API | |
| 测试 | |
| 安全 | |

### 待确认问题

### 风险

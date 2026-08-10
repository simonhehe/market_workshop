---
name: codeflow-design-tasks
description: >-
  After proposal/specs exist: produce design.md from OpenSpec, then tasks.md from
  design. OpenSpec 转设计、设计转任务清单与执行顺序。
---

# 设计 + 任务拆解

**迁移说明**：旧稿在 `.cursor/skills/_archive/codeflow-md/02-openspec-to-design.md` 与 `03-design-to-tasks.md`；以本文件为唯一维护源。

---

## 02 — OpenSpec 转设计方案

### 使用场景

在 proposal.md 和 specs 已经可用后、实现前使用本小节。

### 角色

你是一名 Java + Spring Boot + Vue + MySQL 系统的资深全栈架构师。

### 输入

请读取当前 OpenSpec change 文件：

- proposal.md
- specs/**/*.md
- project.md
- 必要时读取既有代码上下文

### 任务

生成可指导实现和测试的技术设计。

### 必须步骤

1. 总结需求。
2. 识别受影响模块。
3. 设计后端变更。
4. 设计前端变更。
5. 设计数据库变更。
6. 设计 API 契约。
7. 设计测试策略。
8. 识别兼容性、安全性和性能风险。

### 输出格式

#### 需求总结

#### 架构概览

#### 后端设计

| 分层 | 设计 |
|---|---|
| Controller | |
| Service | |
| Domain / Model | |
| Repository / Mapper | |
| 事务边界 | |

#### 前端设计

| 区域 | 设计 |
|---|---|
| 页面 / 组件 | |
| 状态 | |
| API Client | |
| 路由 / 权限 | |

#### 数据库设计

| 表 | 变更 | 原因 | 兼容性 |
|---|---|---|---|

#### API 契约

| 方法 | 路径 | 请求 | 响应 | 错误场景 |
|---|---|---|---|---|

#### 测试设计

| 需求 | 单元测试 | 集成测试 | E2E 测试 |
|---|---|---|---|

#### 风险与缓解

| 风险 | 影响 | 缓解措施 |
|---|---|---|

---

## 03 — 设计方案转任务清单

### 使用场景

design.md 准备完成后、编码前使用本小节。

### 角色

你是一名关注交付落地的技术负责人。

### 任务

将设计方案转换为清晰的实现任务清单。

### 必须步骤

1. 阅读 proposal、specs 和 design。
2. 将工作拆解为小任务。
3. 保持每个任务可独立 Review。
4. 将每个任务映射到验收标准。
5. 将每个任务映射到测试覆盖。
6. 建议执行顺序。
7. 识别依赖和阻塞项。

### 输出格式

#### 任务清单

| ID | 任务 | 层级 | 文件 / 模块 | 验收标准 | 测试类型 | 状态 |
|---|---|---|---|---|---|---|

#### 执行顺序

1.
2.
3.

#### 依赖 / 阻塞

| 项目 | 描述 | 负责人 / 动作 |
|---|---|---|

#### 验证命令

```bash
# backend
mvn test

# frontend
npm test

# e2e
npm run e2e
```

#### 完成检查清单

- [ ] 需求已实现
- [ ] 单元测试已新增或更新
- [ ] 集成测试已新增或更新
- [ ] E2E 测试已新增或更新
- [ ] 代码已 Review
- [ ] OpenSpec tasks 已更新

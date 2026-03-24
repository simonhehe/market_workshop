---
name: workshop-verify-quality-gates
description: >-
  对照 OpenSpec change 的 tasks.md（含 §6 测试与质量门禁）执行核对与补全：
  JaCoCo、mvn verify、SpringBoot 集成测试、Cypress 冒烟、GitHub Actions CI。
  适用于：用户提及 /opsx:verify、质量门禁、tasks.md §6、可归档性、JaCoCo、Cypress、ci.yml。
---

# 福利商城：验证与质量门禁（OpenSpec + 本仓库）

## 何时使用

- 用户要求 **`/opsx:verify`**、**检查 task**、**能否归档**，或明确提到 **`tasks.md` §6**、**JaCoCo**、**API 集成测试**、**Cypress**、**CI**。
- 需要判断「实现是否闭合」且**只依据仓库内文件**（见规则 **`evidence-and-uncertainty.mdc`**）。

## 核对顺序

1. **读 `openspec/changes/<change>/tasks.md`**
   - 统计 `- [ ]` / `- [x]`；§6 是否全部闭合。
2. **后端**
   - 运行 **`mvn -f backend/pom.xml verify`**（含测试 + JaCoCo **`check`**，以 **`backend/pom.xml`** 为准）。
   - 集成测试：查看 **`backend/src/test/java/**/integration/*`** 与 **`application-test.yml`**；遵守规则 **`backend-spring-testing.mdc`**（MockMvc 路径、H2 profile）。
3. **前端**
   - **`npm run build`**（`frontend`）；Cypress：**`cypress.config.cjs`**、`cypress/e2e/*.cy.js`、**`package.json`** 脚本；遵守 **`frontend-e2e-cypress.mdc`**。
4. **CI**
   - 阅读 **`.github/workflows/ci.yml`** 是否包含与 §6 一致的 job（事实描述；不说「一定绿」除非跑过）。
5. **归档结论**（事实 + 待确认）
   - **可归档**：§1–§5 与 §6 在 `tasks.md` 均已 `[x]`，且与代码/workflow 一致；若本地未跑 `cypress install`，须在结论中标 **待确认** 或 **仅在 CI 验证**。
   - **不可归档**：仍存在 `[ ]` 或与实现明显矛盾（例如声称有 IT 但无测试类）。
   - OpenSpec CLI 的 **`openspec status`**、**归档搬家** 等：若用户环境未安装 CLI，标 **待确认**，不编造输出。

## 归档前联动

- 若变更涉及订单/鉴权/文档口径，同步 **`workshop-baseline-sync`** 技能。
- 归档步骤遵循 **`openspec-archive-change`** 技能（含用户确认 incomplete artifact 等）。

## 不要做的事

- 不要把 **UT（WebMvcTest）** 说成 **API 集成测试**；二者在仓库内类与注解不同。
- 不要默认 **Cypress 已本地跑通**；以终端记录或未执行为准。
- 不要编造 **`openspec` CLI** 或 **GitHub** 上的结果。

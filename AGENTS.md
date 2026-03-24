# Agent 指引（福利商城 / workshop0324）

本文件为进入本仓库时的**项目级入口**：优先遵守根目录 **`README.md`** 与 **`doc/README.md`**；事实与不确定性的写法见 **`.cursor/rules/evidence-and-uncertainty.mdc`**。

## 文档与 OpenSpec

- 文档目录规则：**`.cursor/rules/documentation-layout.mdc`**。
- OpenSpec 变更：`openspec/changes/<name>/`； propose / apply / archive 见 **`.cursor/commands/opsx-*.md`** 与 **`.cursor/skills/openspec-*.md`**。

## 领域与安全

- 订单、购物车、管理员改状态等：**`.cursor/rules/order-and-api-safety.mdc`**。
- 行为或基线对齐后：**`.cursor/skills/workshop-baseline-sync/SKILL.md`**。

## 测试与质量门禁

- Spring 测试（MockMvc、`/api`、H2 profile、JaCoCo）：**`.cursor/rules/backend-spring-testing.mdc`**。
- Cypress：**`.cursor/rules/frontend-e2e-cypress.mdc`**。
- 核对 `tasks.md` §6、是否可验证/归档：**`.cursor/skills/workshop-verify-quality-gates/SKILL.md`**。

## CI 与版本真相

- 工作流：**`.github/workflows/ci.yml`**（以文件为准）。
- Java 版本以 **`backend/pom.xml`** 为准；本机 JDK 若更高/更低，不自动等同于项目要求。
- 端口与 context-path 以 **`backend/src/main/resources/application.yml`**、`**frontend/vite.config.js`** 为准。

# AECOS Workshop 课件 v3 设计

**日期:** 2026-08-04  
**状态:** Implemented  

**载体:** `ywpt_workshop_test` / GitNexus `market_workshop`  
**基线课件:** `workshop/curriculum/aecos_workshop_v2.md`（保留）；新建 v3

---

## 1. 目标与成功标准

### 1.1 目标

新建 `workshop/curriculum/aecos_workshop_v3.md`，按三大环节重组现场主线：

1. **L1** 存量扫描与基线（GitNexus）  
2. **L2** 生成原型与 PRD：视觉伴随出原型 → 人闸；`/opsx-explore` 据原型出 PRD → 人闸  
3. **L3** 基于原型与 PRD 启动 OpenSpec 变更（propose → brainstorming 对抗对齐原型&PRD → 分钟级 superpowers spec/plan → 多 agent 编码 → TDD+review → 启服走查）

文风继承 v2：Chat 内联、人闸 `[!IMPORTANT]` 高亮；课件与 `prep/` L* 镜像双向同步。

### 1.2 成功标准

- 只打开 v3 即可复制 Chat、完成逐步人闸，无需以 P*.md 为唯一操作入口。  
- L2 原型工具为 **视觉伴随**；PRD 为 **opsx-explore**（与 v2 顺序/工具不同）。  
- L3-2 对抗目标明确为：**change 与原型&PRD 高度一致无分歧**，再进入 L3-3。  
- L3-3 产出 **分钟级** `docs/superpowers/specs/` + `plans/`。  
- v1/v2 保留；业务代码不因本设计变更而修改。

### 1.3 非目标

- 删除或替换 v1/v2。  
- 删除现有 `P0–P6` prompt/gate（validate-kit 仍依赖）。  
- 强制修改 validate-kit REQUIRED 以纳入 v3。  
- 锁死现场总时长（仅建议分钟）。  
- 实现业务功能本身。

---

## 2. 已锁定决策

| ID | 决策 |
|----|------|
| D1 | 方案 1：单文件 v3 + Chat/人闸内联 |
| D2 | 人闸策略 **A**：每个标注人工确认的步骤均做人闸（高亮块） |
| D3 | 时长策略 **C**：不锁死总时长；表内建议分钟 |
| D4 | L3-3 = 对抗收敛 change 后，再 **writing-plans** 出分钟级 spec/plan（选项 A + 用户补充意图） |
| D5 | L3-2 brainstorming：完善 change，确保与原型&PRD 高度一致 |
| D6 | 文件名：`aecos_workshop_v3.md`；README 推荐入口改为 v3 |
| D7 | prep：保留 P0–P6；**新增** L1/L2/L3 镜像卡与人闸 |
| D8 | compass：现场按 L1/L2/L3 记账；附 P0–P6↔L 映射；不强制改 validate 的 P 段标记 |

---

## 3. 环节树 · 人闸 · 产物

| 环节 | 子步骤 | 人闸 | 主要产物 |
|------|--------|------|----------|
| **L1** | GitNexus 整体分析（口径对齐现行 v2 P0 Chat） | 闸-L1 | `workshop/product/baseline/project-baseline.md`；rules TODO |
| **L2-1** | 视觉伴随生成原型 | 闸-L2-原型 | `workshop/product/order.html` |
| **L2-2** | `/opsx-explore` 据已确认原型生成 PRD | 闸-L2-PRD | `workshop/product/PRD-order-checkout.md` |
| **L3-1** | `/opsx-propose` → change 四类文件 | 闸-L3-propose（短：文件齐全、范围未偷渡） | `openspec/changes/<change>/` |
| **L3-2** | `/brainstorming` 对抗 → 人工确认后改 change | 闸-L3-对抗 | 修订后的 proposal/specs/design/tasks |
| **L3-3** | 分钟级 superpowers spec + plan | 闸-L3-plan | `docs/superpowers/specs/...`、`plans/...` |
| **L3-4** | 多 agent 按 plan 编码（默认 subagent-driven-development） | 重大偏差可闸；默认可切片口头确认 | 业务代码 |
| **L3-5** | TDD + requesting-code-review | 闸-L3-质量 | 测试/报告；诚实 degraded |
| **L3-6** | 启服预览 + 人工走查 | 闸-L3-走查 | `workshop/delivery/walkthrough-log.md` |

建议分钟（可调，仅指导）：L1 ~15；L2 ~30–40；L3 其余现场时间（六步 internally 再拆建议值写入 v3 表）。

---

## 4. Chat 要点

| 步骤 | Chat 核心 |
|------|-----------|
| L1 | 同 v2 P0：analyze + 确认本仓 + 预览；围绕下单结算扫存量（含技术清单口径）；写 baseline；回填 TODO；等人闸；约束无业务改/无支付优惠券 |
| L2-1 | 视觉伴随；一句话「开发下单结算」+ baseline；产出 order.html 关键区块；不改业务代码 |
| L2-2 | opsx-explore + prd-prototype-authoring；基于已确认原型；状态机/负向/AC |
| L3-1 | opsx-propose @PRD（可 @原型）；四类文件 |
| L3-2 | brainstorming：对照原型&PRD，重点挑 propose/specs 问题；确认后更新 change |
| L3-3 | writing-plans：分钟级 tasks；人工确认 |
| L3-4 | subagent-driven-development（或多 agent 等价）按 plan |
| L3-5 | TDD + requesting-code-review；coverage-scope |
| L3-6 | 启服 8080/3000；走查脚本；walkthrough-log |

---

## 5. prep / compass / 交叉引用

### 5.1 prep

- 保留 `prompt-cards/P0–P6`、`gate-scripts/P0–P5`、`recovery-cards` 现有内容。  
- 新增例如：`prep/prompt-cards/L1.md`、`L2.md`、`L3.md`；`prep/gate-scripts/L1.md`、`L2.md`、`L3.md`；recovery 增加 `R-L1`、`R-L2-原型`、`R-L2-PRD`、`R-L3-*`。  
- 同步规则：v3 课件变更 Prompt/人闸 → 同步 L* 镜像。

### 5.2 compass

- 增加或改写为 L1 / L2 / L3 段字段（目标、Chat 摘要、产物、人闸、耗时、踩坑、degraded）。  
- 文末或附录：P0–P6 ↔ L 映射表。  
- validate-kit 仍检查 P0–P6 标记：实现时在 compass **保留** P 段空壳或兼容标记，避免破坏 kit（推荐：保留 P 段标题作兼容，正文指向 L 段）。

### 5.3 交叉引用

- `workshop/README.md`、`curriculum/01–03`、`05`：推荐授课稿改为 **v3**；保留 v1/v2 链接。

---

## 6. 风险与缓解

| 风险 | 缓解 |
|------|------|
| 人闸过多拖垮现场 | 时长策略 C；闸问题保持 3 条内；L3-1/L3-4 可短确 |
| 视觉伴随环境差异 | Chat 写清产出与区块；失败则 Tips 允许 HTML 直出但须人闸 |
| propose 与 writing-plans 重复 | L3-2 收敛 change；L3-3 明确「分钟级执行计划」，不重复写业务规格全文 |
| compass 与 validate 冲突 | 保留 P 标记兼容 |

---

## 7. 实现范围（进入 plan 时拆任务）

1. 新建 `aecos_workshop_v3.md`  
2. 新增 L* prompt/gate/recovery 镜像  
3. README + 01–03、05 推荐 v3  
4. compass 兼容 L 段 + P 映射  
5. 自检：L2 工具顺序、L3-2/L3-3 意图、人闸高亮齐全  

---

## 8. 修订记录

| 日期 | 变更 | 作者 |
|------|------|------|
| 2026-08-04 | 初稿：三大环节 + 全人工闸 + 灵活时长 + 对抗对齐原型PRD + 分钟级 plan | brainstorming |
| 2026-08-04 | 用户确认；plan 见 `docs/superpowers/plans/2026-08-04-aecos-workshop-v3.md` | — |
| 2026-08-04 | Implemented：v3 课件 + L* prep + README/手册/compass；validate-kit OK | plan T1–T4 |

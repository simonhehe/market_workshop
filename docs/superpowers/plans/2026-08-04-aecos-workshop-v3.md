# AECOS Workshop 课件 v3 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 新建 `aecos_workshop_v3.md`（L1→L2→L3），新增 prep L* 镜像，README/手册推荐 v3，compass 兼容 L 段。

**Architecture:** 三大环节；Chat 内联；人闸 `[!IMPORTANT]`；L2 视觉伴随→原型闸→opsx-explore→PRD闸；L3 propose→brainstorming 对齐原型&PRD→分钟级 plan→多 agent→TDD/review→启服。保留 v1/v2 与 P0–P6。

**Tech Stack:** Markdown 课件 · OpenSpec · superpowers（brainstorming / writing-plans / subagent-driven-development / TDD / requesting-code-review）

**Spec:** `docs/superpowers/specs/2026-08-04-aecos-workshop-v3-design.md`

---

## File map

| 文件 | 动作 |
|------|------|
| `workshop/curriculum/aecos_workshop_v3.md` | Create |
| `workshop/prep/prompt-cards/L1.md` `L2.md` `L3.md` | Create |
| `workshop/prep/gate-scripts/L1.md` `L2.md` `L3.md` | Create |
| `workshop/prep/prompt-cards/recovery-cards.md` | Append R-L* |
| `workshop/README.md`, `curriculum/01–03`, `05` | Recommend v3 |
| `workshop/compass.md` | Add L1/L2/L3 + P↔L map；保留 `## P0`…`## P6` 标题以过 validate-kit |

---

### Task 1: Create L* prep mirrors + recovery

**Files:**
- Create: `workshop/prep/prompt-cards/L1.md`, `L2.md`, `L3.md`
- Create: `workshop/prep/gate-scripts/L1.md`, `L2.md`, `L3.md`
- Modify: `workshop/prep/prompt-cards/recovery-cards.md`

- [ ] **Step 1: Write `L1.md`**

```markdown
# Prompt 卡 L1 · 存量扫描与基线（建议 ~15min）

使用 GitNexus 对本仓库进行整体分析（不改业务代码；不扩展支付/优惠券）：

1. analyze + 确认本仓：对本仓 analyze；确认 list_repos/status 指向本目录；拉起 GitNexus 预览服务。  
2. 围绕「下单结算」扫存量：结合 GitNexus，扫描技术栈、模块、购物车→订单/积分/库存流程与缺口。  
   技术部分包括：ER 图、user 图、类图、时序图、状态机、API 契约、并发、安全、前端缺口、数据字典、测试矩阵等（紧扣结算叙事，避免无关膨胀）。  
3. 写 baseline + 回填 rules TODO：写入 `workshop/product/baseline/project-baseline.md`；能确定的项回填 `.cursor/rules/00-project-context.mdc` 的 TODO。  
4. 暂停 → 人闸 L1（见课件 v3 或 `gate-scripts/L1.md`）。  
```

- [ ] **Step 2: Write `L2.md`**

```markdown
# Prompt 卡 L2 · 生成原型与 PRD（建议 ~30–40min）

输入需求（仅此一句）：「开发下单结算」。

## L2-1 · 视觉伴随生成原型

1. 使用 superpowers **视觉伴随**（brainstorming visual companion），结合 baseline 缺口，生成结算页原型 → `workshop/product/order.html`。  
2. 须含：地址、商品摘要、积分合计、提交、错误态。本步不改业务代码。  
3. 暂停 → **人闸-L2-原型**。  

## L2-2 · opsx-explore 生成 PRD（仅原型闸通过后）

1. `/opsx-explore` + `prd-prototype-authoring` / rule 13，基于已确认原型生成 `workshop/product/PRD-order-checkout.md`。  
2. 须含状态机、负向需求、验收表（中等 MVP）。  
3. 暂停 → **人闸-L2-PRD**。  

约束：不做管理端；不做真实支付；拒绝优惠券/拆单。
```

- [ ] **Step 3: Write `L3.md`**

```markdown
# Prompt 卡 L3 · OpenSpec 变更到启服（建议分钟见课件 v3）

## L3-1 propose
`/opsx-propose` 基于已确认 PRD（可 @原型）创建 change（建议名 `order-checkout-hardening`），生成 proposal/specs/design/tasks。暂停 → 人闸-L3-propose。

## L3-2 brainstorming 对抗
`/brainstorming` 对照原型与 PRD，重点发现 propose/specs 问题与不一致；人工确认后更新 change，确保与原型&PRD 高度一致。暂停 → 人闸-L3-对抗。

## L3-3 分钟级 spec/plan
对已收敛 change 使用 writing-plans，生成 `docs/superpowers/specs/` 与 `plans/`（分钟级任务）。暂停 → 人闸-L3-plan。

## L3-4 多 agent 编码
按 plan 使用 subagent-driven-development（或多 agent 等价）切片实现。

## L3-5 TDD + review
TDD + requesting-code-review；按 coverage-scope；达不到 95% 则 degraded。暂停 → 人闸-L3-质量。

## L3-6 启服走查
启 backend:8080 + frontend:3000；走查并写 `workshop/delivery/walkthrough-log.md`。暂停 → 人闸-L3-走查。
```

- [ ] **Step 4: Write gate scripts**

`workshop/prep/gate-scripts/L1.md`:

```markdown
# 人闸 L1 · 产品方

1. 基线是否写清技术栈与订单相关模块（含结算相关技术视图/契约摘要）？  
2. 缺口是否覆盖结算闭环 / 状态 / 库存与积分异常？  
3. 是否引入支付/优惠券等非目标？  

**通过:** 可进 L2。 **打回:** R-L1。
```

`L2.md`:

```markdown
# 人闸 L2 · 产品方

## 人闸-L2-原型
1. 选地址→合计→提交→错误态可演示？ 2. 区块齐全？ 3. 中等 MVP？  
**通过:** 进 L2-2。 **打回:** R-L2-原型。

## 人闸-L2-PRD
1. 状态机唯一优先级？ 2. 负向清楚？ 3. AC 映射 coverage 最小集且与原型一致？  
**通过:** 进 L3。 **打回:** R-L2-PRD。
```

`L3.md`:

```markdown
# 人闸 L3 · 产品方

## 人闸-L3-propose
1. 四类文件齐全？ 2. 无支付/优惠券偷渡？  
**通过:** 进 L3-2。 **打回:** R-L3-propose。

## 人闸-L3-对抗
1. change 是否与原型&PRD 高度一致？ 2. propose/specs 关键问题是否已改？  
**通过:** 进 L3-3。 **打回:** R-L3-对抗。

## 人闸-L3-plan
1. plan 是否分钟级可执行？ 2. 是否仍落在中等 MVP？  
**通过:** 进 L3-4。 **打回:** R-L3-plan。

## 人闸-L3-质量
1. 报告/用例诚实？ 2. ≥95% 或 degraded？  
**通过:** 进 L3-6。 **打回:** R-L3-质量。

## 人闸-L3-走查
1. 主路径通过？ 2. 至少一条异常可读？  
**通过:** 本场交付可演示。 **打回:** R-L3-走查。
```

- [ ] **Step 5: Append R-L* to recovery-cards.md** (keep existing R-P*)

```markdown
## R-L1
只重跑 GitNexus 相关分析，重写 product/baseline「缺口」及相关技术节，禁止改业务代码。

## R-L2-原型
只改被点名的原型区块/交互/错误态；禁止开始写 PRD 或改业务代码。

## R-L2-PRD
只改被点名的状态机或 AC；拒绝加需求。

## R-L3-propose
删除/降级越界任务与范围偷渡；补齐缺失四类文件。

## R-L3-对抗
按人闸点名修订 propose/specs，直到与原型&PRD 一致。

## R-L3-plan
缩小/拆分分钟级任务；去掉不可控项。

## R-L3-质量
补白名单失败用例或诚实 degraded；禁止假通过。

## R-L3-走查
只修阻塞步骤；更新 walkthrough-log。
```

- [ ] **Step 6: Commit**

```bash
git add workshop/prep/prompt-cards/L1.md workshop/prep/prompt-cards/L2.md workshop/prep/prompt-cards/L3.md workshop/prep/gate-scripts/L1.md workshop/prep/gate-scripts/L2.md workshop/prep/gate-scripts/L3.md workshop/prep/prompt-cards/recovery-cards.md
git commit -m "docs(prep): add L1-L3 prompt and gate mirrors for workshop v3"
```

---

### Task 2: Create `aecos_workshop_v3.md`

**Files:**
- Create: `workshop/curriculum/aecos_workshop_v3.md`

- [ ] **Step 1: Write full v3** with: header；建议分钟总表（L1/L2-1/L2-2/L3-1…L3-6）；开场；prep 链；L1 Chat=现行 v2 P0；L2-1 视觉伴随；L2-2 opsx-explore PRD；L3-1…6 按 design；人人闸 `[!IMPORTANT]`；L3-2 写明对齐原型&PRD；L3-3 写明分钟级 writing-plans；L3-4 subagent-driven-development；FAQ；P↔L 映射附录。

Suggested minutes: L1 15；L2-1 20；L2-2 15；L3-1 10；L3-2 20；L3-3 15；L3-4 40；L3-5 25；L3-6 15。

- [ ] **Step 2:** Grep 人闸均含 `[!IMPORTANT]`；L2-1 含视觉伴随；L2-2 含 opsx-explore。

- [ ] **Step 3: Commit**

```bash
git add workshop/curriculum/aecos_workshop_v3.md
git commit -m "docs(curriculum): add aecos_workshop_v3 L1-L2-L3 courseware"
```

---

### Task 3: Cross-refs + compass

- [ ] README 推荐 v3；保留 v1/v2。  
- [ ] 01/02/03/05 指向 v3。  
- [ ] compass：保留 `## P0`…`## P6`；新增 `## L1` `## L2` `## L3`；附录 P↔L 映射。  
- [ ] `node workshop/scripts/validate-kit.mjs` → OK。  
- [ ] Commit: `docs: recommend aecos_workshop_v3 and add L-stage compass sections`

---

### Task 4: Verify + mark design

- [ ] Design status → Approved/Implemented 修订行。  
- [ ] Checklist: v3；L2 工具顺序；L3-2/L3-3 意图；人闸高亮；P 卡仍在；validate OK；无业务代码改动。

---

## Spec coverage

| Spec | Task |
|------|------|
| v3 课件 | T2 |
| L* prep | T1 |
| 交叉引用 + compass | T3 |
| 验证 | T4 |

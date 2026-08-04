# AECOS Workshop 课件 v2 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 新建 `aecos_workshop_v2.md`（Chat/人闸内联、P1 分步双人闸），并同步 `workshop/prep` 中 P1 与 recovery 镜像。

**Architecture:** v2 为授课权威文案；`prep/prompt-cards` 与 `gate-scripts` 为 kit 镜像。P1 拆为 explore→order.html→人闸-原型→PRD→人闸-PRD。不改业务代码、不删 v1、不改 validate-kit REQUIRED。

**Tech Stack:** Markdown 课件 · OpenSpec `/opsx-explore` · 既有 workshop kit 路径

**Spec:** `docs/superpowers/specs/2026-08-04-aecos-workshop-v2-design.md`

---

## File map

| 文件 | 职责 |
|------|------|
| `workshop/curriculum/aecos_workshop_v2.md` | 新建：授课主稿 |
| `workshop/prep/prompt-cards/P1.md` | 同步：P1-a / P1-b 两节 |
| `workshop/prep/gate-scripts/P1.md` | 同步：人闸-原型 / 人闸-PRD |
| `workshop/prep/prompt-cards/recovery-cards.md` | 同步：R-P1-原型 / R-P1-PRD |
| `workshop/prep/prompt-cards/P0,P2–P6.md` | 轻量：去掉「开闸见 gate-scripts」外链句 |
| `workshop/README.md`, `curriculum/01–03,05` | 增加 v2 推荐入口 |

---

### Task 1: Sync prep P1 + gate P1 + recovery

**Files:**
- Modify: `workshop/prep/prompt-cards/P1.md`
- Modify: `workshop/prep/gate-scripts/P1.md`
- Modify: `workshop/prep/prompt-cards/recovery-cards.md`

- [ ] **Step 1: Overwrite `P1.md` with two sections**

```markdown
# Prompt 卡 P1 · 原型与 PRD（≤30min）

输入需求（仅此一句）：「开发下单结算」。

## P1-a · 原型（先做；未过闸前不做 PRD）

1. 使用 `/opsx-explore`，结合 `workshop/baseline/project-baseline.md` 缺口，探索并生成结算页原型，写入 `workshop/product/order.html`。  
2. 原型须含关键区块：收货地址、商品摘要、积分合计、提交、错误态（库存不足 / 积分不足 / 未选地址等）。  
3. 本步不改业务代码。  
4. 暂停 → **人闸-原型**（见 `gate-scripts/P1.md`「人闸-原型」或课件 v2 对应节）。  

约束：不做管理端；不做真实支付。

## P1-b · PRD（仅人闸-原型通过后）

1. 按 skill `prd-prototype-authoring` 与 rule `13-prd-prototype-authoring`，基于已确认原型生成/完善 `workshop/product/PRD-order-checkout.md`。  
2. 必须含：状态机（服务端唯一计算）、负向需求、验收表覆盖中等 MVP（闭环+状态+库存/积分异常）。  
3. 可选：prd-prototype-review 快检，问题记入 compass。  
4. 暂停 → **人闸-PRD**。  

约束：保持中等 MVP；拒绝支付/优惠券/拆单。
```

- [ ] **Step 2: Overwrite `gate-scripts/P1.md`**

```markdown
# 人闸 P1 · 产品方（原型 + PRD，各 3–5 min）

## 人闸-原型

1. 原型能否完成：选地址→看合计→提交→看到错误态？  
2. 关键区块是否齐全（地址、商品摘要、积分合计、提交、错误态）？  
3. 是否仍落在中等 MVP（无支付/管理端偷渡）？  

**通过:** 进入 P1-b 写 PRD。  
**打回:** R-P1-原型。

## 人闸-PRD

1. PRD 状态机是否唯一优先级（服务端为状态唯一计算方）？  
2. 负向需求是否清楚（MUST NOT）？  
3. 验收表是否映射到 coverage-scope 最小集？是否与已确认原型一致？  

**通过:** 原型+PRD 与中等 MVP 一致，可进 P2。  
**打回:** R-P1-PRD。
```

- [ ] **Step 3: Update `recovery-cards.md` — replace R-P1 section**

Replace the `## R-P1` block with:

```markdown
## R-P1-原型
原型打回：只改被点名的区块/交互/错误态文案；保持中等 MVP；禁止开始写 PRD 或改业务代码。

## R-P1-PRD
PRD 打回：只改被点名的状态机或 AC 行；保持中等 MVP，拒绝加需求；不重开无关原型大改（除非人闸点名原型不一致）。
```

- [ ] **Step 4: Commit**

```bash
git add workshop/prep/prompt-cards/P1.md workshop/prep/gate-scripts/P1.md workshop/prep/prompt-cards/recovery-cards.md
git commit -m "docs(prep): split P1 prompt/gate into prototype and PRD stages"
```

---

### Task 2: Create `aecos_workshop_v2.md`

**Files:**
- Create: `workshop/curriculum/aecos_workshop_v2.md`
- Read for structure: `workshop/curriculum/aecos_workshop.md`

- [ ] **Step 1: Write the full v2 file**

Create `workshop/curriculum/aecos_workshop_v2.md` with **exactly** the following content (do not leave “见 P*.md / gate-scripts” as the sole instruction):

```markdown
# 《AI Coding Workshop》课件 for 福利商城 · 下单结算（v2）

> **项目**：`ywpt_workshop_test`（GitNexus MCP 名：`market_workshop`）  
> **时长**：约 3 小时现场（讲练穿插；课前 Track 0 另计）  
> **双主线**：模块交付（中等 MVP 结算闭环）+ 课件沉淀（compass → curriculum）  
> **相对 v1**：Prompt / 人闸内联于各段；P1 为 `/opsx-explore` 出原型 → 人闸 → 再出 PRD → 人闸  
> **镜像**：`workshop/prep/prompt-cards/`、`workshop/prep/gate-scripts/`（课件变更须同步）

配套物料：

| 类型 | 路径 |
|------|------|
| 环境准备 | `curriculum/aecos_workshop_prep.md`（深化：`prep/env-setup.md`） |
| Prompt / 人闸 | **本文件各段内联**；镜像见 `prep/prompt-cards`、`prep/gate-scripts` |
| 六件套 | `workshop/curriculum/01`–`06` |
| 总入口 | `workshop/README.md` |
| v1（保留） | `curriculum/aecos_workshop.md` |

---

## 一、课程总流程（讲练一体）

每个 Phase **讲完即练**；每闸必须有产品方结论写入 `workshop/compass.md`。

| 时间 | 环节 | 合计 |
|------|------|------|
| 课前 | Track 0 / Phase 0：环境准备 | 另计 |
| 0–15 min | P0：存量扫描与基线 | 15 |
| 15–45 min | P1：原型与 PRD（两道人闸） | 30 |
| 45–75 min | P2：OpenSpec + 对抗收敛 | 30 |
| 75–120 min | P3：编码与审查 | 45 |
| 120–155 min | P4：质量门禁 | 35 |
| 155–170 min | P5：启服走查 | 15 |
| 170–180 min | P6：罗盘收口与课件 | 10 |

> **2.5h 压缩版**：Phase 0 提前完成；P4 允许诚实 `degraded`；超时可用金丝雀对照，**不替代人闸**。

**环境前提**（课前）：按 [`aecos_workshop_prep.md`](./aecos_workshop_prep.md) 完成工具安装（含 GitNexus **仅安装**）。**对本仓 analyze 在 P0。**

---

## 二、开场 + 定位 + 方法

**今天要做什么**

- 案例：半成品福利商城 → 可演示「购物车 → 选地址 → 下单 → 扣积分/库存 → 订单列表/取消」。
- 带走：内联 Chat 用法 + OpenSpec/superpowers + 人闸习惯 + 踩坑 playbook。

**工具分工**

| 工具 | 干什么 |
|------|--------|
| Cursor Agent | 读项目、写代码、跑命令 |
| OpenSpec + superpowers | SDD / 对抗 / 计划执行 / TDD / review |
| Rules / Skills | 约束范围；PRD/原型质量（`prd-prototype-authoring`） |
| GitNexus | 存量扫描、impact、防无关改动 |

**成功标准：** 中等 MVP 可演示；质量 ≥95% 或诚实 degraded；每闸真人结论；compass/curriculum 可复训。  
**非目标：** 真实支付、优惠券、拆单、完整售后；本场不强制 harness 运行时。

---

## 三、Phase 0：环境准备

见 [`aecos_workshop_prep.md`](./aecos_workshop_prep.md)。课前只要求工具装好并能验证版本。

---

## 四、P0：存量扫描与基线（0–15）

### Chat 对话

```text
你是执行学员。对本福利商城仓库依次完成：

1) 对本仓库根目录执行 GitNexus analyze 建索引；用 status / list（或 MCP list_repos）确认索引指向本目录。
   注意：MCP 名多为 market_workshop，文件夹名可能是 ywpt_workshop_test，以路径为准。

2) 存量扫描（MCP 或自然语言均可）：
   - list_repos 确认本仓
   - query：order checkout createOrder cart points stock（找下单与购物车→订单流）
   - context：OrderService；context：CartService
   - route_map 或 query：/orders
   - impact：OrderService direction=upstream

3) 写入 workshop/baseline/project-baseline.md：技术栈表、业务模块、下单结算缺口、建议 OpenSpec change 名（如 order-checkout-hardening）。
4) 能确定的项回填 .cursor/rules/00-project-context.mdc 的 TODO。
5) 暂停，等人闸。

约束：不改业务代码；不扩展到支付/优惠券。
完整查询剧本镜像：workshop/prep/gitnexus-queries.md
```

### 人闸检查

1. 基线是否写清技术栈与订单相关模块？  
2. 缺口是否覆盖「结算闭环 / 状态 / 库存与积分异常」？  
3. 是否错误引入支付/优惠券等非目标？  

**通过:** 缺口可指导 PRD；无范围膨胀。  
**打回 (R-P0):** 只重跑 GitNexus 查询相关步，重写 baseline「缺口」节，禁止改代码。

### Tips

干跑示范缺口方向：无结算页、地址可选、错误态不统一、缺测试、状态机需冻结。建议 change：`order-checkout-hardening`。

---

## 五、P1：原型与 PRD（15–45）

输入需求（仅此一句）：**「开发下单结算」。**

### 5.1 P1-a · 用 `/opsx-explore` 生成原型

#### Chat 对话

```text
/opsx-explore
目标：根据一句话需求「开发下单结算」，结合 workshop/baseline/project-baseline.md 中的缺口，探索并生成高质量结算页 HTML 原型。
产出：workshop/product/order.html
要求：
- 必须包含区块：收货地址、商品摘要、积分合计、提交、错误态（至少覆盖未选地址、库存不足、积分不足）
- 交互可演示：选地址 → 看合计 → 提交 → 可见错误或成功示意
- 对齐中等 MVP；不做管理端；不做真实支付
- 本步禁止修改 backend/frontend 业务代码
探索时可参考仓库半成品与已有 product 目录，但以生成完整可演示原型为准。
完成后暂停，等人闸-原型。
```

#### 人闸检查（人闸-原型）

1. 原型能否完成：选地址→看合计→提交→看到错误态？  
2. 关键区块是否齐全？  
3. 是否仍落在中等 MVP（无支付/管理端偷渡）？  

**通过:** 进入 5.2 写 PRD。  
**打回 (R-P1-原型):** 只改被点名的区块/交互/错误态；禁止开始写 PRD 或改业务代码。

---

### 5.2 P1-b · 生成 PRD（仅人闸-原型通过后）

#### Chat 对话

```text
原型已通过人闸。请按 skill prd-prototype-authoring 与 rule 13-prd-prototype-authoring，
基于已确认的 workshop/product/order.html，生成/完善 workshop/product/PRD-order-checkout.md。
必须包含：文档头、模块索引、术语与状态机（服务端为状态唯一计算方）、负向需求（MUST NOT）、
验收规则表（覆盖闭环+状态+库存/积分/地址异常，并尽量映射 coverage 最小集）。
visual_level 建议 L2。可选跑 prd-prototype-review，问题记入 compass。
约束：保持中等 MVP；拒绝支付/优惠券/拆单。
完成后暂停，等人闸-PRD。
```

#### 人闸检查（人闸-PRD）

1. PRD 状态机是否唯一优先级？  
2. 负向需求是否清楚？  
3. 验收表是否映射到 coverage-scope 最小集？是否与已确认原型一致？  

**通过:** 可进入 P2。  
**打回 (R-P1-PRD):** 只改被点名的状态机或 AC 行；拒绝加需求。

#### Tips（可选问答）

| 问 | 答 |
|----|-----|
| 直接用原型不行吗？ | 原型管看得见；PRD 管状态唯一源、负向、可测 AC |
| 谁对 PRD 负责？ | 产品方人闸；执行者撰写 |

---

## 六、P2：OpenSpec + 对抗（45–75）

### Chat 对话

```text
1) 使用 /opsx-propose（或 openspec-propose skill）基于已确认的 workshop/product/PRD-order-checkout.md 生成 change，建议名 order-checkout-hardening（以 baseline 为准）。
2) 对抗分析固定 5 题并改生成物：
   - 范围是否偷渡优惠券/支付/售后？
   - 订单状态优先级是否唯一？
   - 库存与积分并发/超扣如何处理？
   - 未登录/鉴权失败契约？
   - 验收是否可测（对应 workshop/prep/coverage-scope.md 最小集）？
3) 收敛 proposal.md / specs/ / design.md / tasks.md，删除不可控任务。
4) 暂停，等人闸。
```

### 人闸检查

1. tasks 是否都能在剩余时间内完成？  
2. 有无偷渡需求？  
3. 对抗 5 题是否都有书面结论？  

**通过:** tasks = 中等 MVP。  
**打回 (R-P2):** 打开 tasks.md，删除或降级越界任务；重新对齐 coverage-scope 最小集。

---

## 七、P3：编码与审查（75–120）

### Chat 对话

```text
1) 仅实现 openspec/changes/order-checkout-hardening/tasks.md（或本场 change 名）内任务；遵循 OpenSpec apply / codeflow-code-testing。
2) 每完成切片：更新 tasks 状态；用 GitNexus detect_changes 或 impact 看爆炸半径。
3) 对照 workshop/prep/canary/ 差异，禁止无关重构。
4) 产出简短 review 要点写入 workshop/compass.md 的 P3 段。
5) 暂停，等人闸（口述 AC 抽查，不替代走查）。
```

### 人闸检查

任选 2 条 PRD AC，请学员指出代码位置与行为。  

**通过:** 抽查 AC 有实现落点。  
**打回 (R-P3):** 只修 AC 失败项；禁止大范围重构；对照 canary。

### Tips（示范非抄答案）

- `/orders/create` 路由须注册在 `/orders/:id` 之前  
- `addressId` 必填校验前置  
- 购物车选中需后端 `isSelected` 支持  

---

## 八、P4：质量门禁（120–155）

### Chat 对话

```text
1) 严格按 workshop/prep/coverage-scope.md 白名单配置覆盖率（摘要：OrderService/CartService、OrderController/CartController 等；前端结算相关 view/api）。
2) 用 TDD 补齐最小用例集：下单成功、库存不足、积分不足、地址缺失；API 契约与鉴权；集成一致性；E2E 主路径+异常。
3) 目标：核心包行&分支 ≥95%。达不到则 degraded=true，禁止写「质量通过」。
4) 输出 workshop/quality/quality-summary.md（含与 canary 样例差异表 + AC 追溯）。
5) 暂停，等人闸。
```

### 人闸检查

1. 报告是否写明白名单分母？  
2. 数字 ≥95% 或明确 degraded？  
3. 最小用例是否全列结果？  

**通过:** 报告诚实且可复训。  
**打回 (R-P4):** 宣称通过但证据不足 → 优先补白名单内失败用例；或设 degraded 并写明缺口。

---

## 九、P5：启服走查（155–170）

### Chat 对话

```text
1) 启动 backend + frontend（端口 8080 / 3000；须存在 backend/database/ 目录）。
2) 按下列走查脚本操作，记录 workshop/delivery/walkthrough-log.md。
3) 阻塞 bug 现场修；非阻塞记债项。
4) 暂停，等产品方签字。
```

### 人闸检查（含走查脚本）

1. 登录 → 加购 → 选中 → 进结算。  
2. 正常下单 → 订单列表可见 → 详情状态正确。  
3. 制造库存或积分不足（或无地址）→ 看到明确错误。  
4. 取消订单（若 AC 含）→ 状态更新。  

**通过:** 主路径 + 至少一条异常通过，或阻塞项已修。  
**打回 (R-P5):** 只修阻塞步骤对应代码；更新 walkthrough-log。

---

## 十、P6：罗盘收口与课件（170–180）

### Chat 对话

```text
1) 补全 workshop/compass.md 所有段字段。
2) 将本场提示词/踩坑/评分结果填入 workshop/curriculum/01–06（可标「待润色」但不可空段）；授课主稿以 aecos_workshop_v2.md 为准时可注明。
3) 运行：在仓库根执行 node workshop/scripts/validate-kit.mjs，确认 OK。
4) 教练确认六件套可给下一班或列出明确课后 PR。
```

### 人闸检查

教练确认：compass 段齐全；六件套可复训或课后 PR 列表明确。（本段无 R-P6；问题记入课后债。）

---

## 十一、复盘 + 答疑

| 问题 | 简答 |
|------|------|
| 为什么必须人闸？ | 范围与 AC 由产品方锁死 |
| 95% 达不到？ | 诚实 degraded；禁止宣称通过 |
| GitNexus 名对不上？ | 看路径；MCP 名常为 market_workshop |
| `/orders/create` 404？ | create 路由须在 `:id` 前 |
| 金丝雀能否代替人闸？ | **不能** |

踩坑表：`workshop/curriculum/06-踩坑-playbook.md`。

---

## 十二、评分（满分 14，≥10 合格）

见 `curriculum/04-评分表.md`。
```

- [ ] **Step 2: Grep self-check**

```bash
rg -n "prompt-cards/P|gate-scripts/P" workshop/curriculum/aecos_workshop_v2.md
```

Expected: **no matches** that tell the learner to open those files as the primary action (镜像说明表头除外；若表头提到镜像路径允许）。Chat/人闸正文中不得出现「按 `prep/prompt-cards/P1.md`」或「开闸：`gate-scripts/P1.md`」。

- [ ] **Step 3: Commit**

```bash
git add workshop/curriculum/aecos_workshop_v2.md
git commit -m "docs(curriculum): add aecos_workshop_v2 with inlined chat and gates"
```

---

### Task 3: Light-sync remaining prompt cards (drop gate file as sole exit)

**Files:**
- Modify: `workshop/prep/prompt-cards/P0.md`
- Modify: `workshop/prep/prompt-cards/P2.md`
- Modify: `workshop/prep/prompt-cards/P3.md`
- Modify: `workshop/prep/prompt-cards/P4.md`
- Modify: `workshop/prep/prompt-cards/P5.md`
- Modify: `workshop/prep/prompt-cards/P6.md`

- [ ] **Step 1: Replace final「开闸」lines**

In each file, change the last step that says `开闸：\`gate-scripts/P*.md\`` (or equivalent) to:

- P0: `5. 暂停，交给产品方做人闸检查（问句见课件 v2 P0「人闸检查」或 gate-scripts/P0.md 镜像）。`
- P2: `4. 暂停 → 人闸检查（课件 v2 P2 或 gate-scripts/P2.md 镜像）。`
- P3: `5. 暂停 → 人闸检查（课件 v2 P3 或 gate-scripts/P3.md 镜像）。`
- P4: `5. 暂停 → 人闸检查（课件 v2 P4 或 gate-scripts/P4.md 镜像）。`
- P5: remove「按 gate-scripts/P5.md」；改为「按课件 v2 P5 人闸检查中的走查脚本操作；记录 walkthrough-log。」
- P6: no gate file; leave as-is unless it references missing cards.

Keep step semantics otherwise unchanged.

- [ ] **Step 2: Commit**

```bash
git add workshop/prep/prompt-cards/P0.md workshop/prep/prompt-cards/P2.md workshop/prep/prompt-cards/P3.md workshop/prep/prompt-cards/P4.md workshop/prep/prompt-cards/P5.md workshop/prep/prompt-cards/P6.md
git commit -m "docs(prep): point prompt-card gates at v2 inlined checks"
```

---

### Task 4: Cross-references (README + curriculum shells)

**Files:**
- Modify: `workshop/README.md`
- Modify: `workshop/curriculum/01-讲师手册.md`
- Modify: `workshop/curriculum/02-学员手册.md`
- Modify: `workshop/curriculum/03-提示词卡册.md`
- Modify: `workshop/curriculum/05-示范产物索引.md`

- [ ] **Step 1: README 授课课件表**

Ensure rows include:

| **总课件 v2（推荐）** | [curriculum/aecos_workshop_v2.md](curriculum/aecos_workshop_v2.md) |
| **总课件 v1** | [curriculum/aecos_workshop.md](curriculum/aecos_workshop.md) |
| **环境准备** | [curriculum/aecos_workshop_prep.md](curriculum/aecos_workshop_prep.md) |

- [ ] **Step 2: 01 / 02 / 03 / 05**

Add one-line note that **现场推荐 `aecos_workshop_v2.md`**；v1 保留对照。Update 03 修订记录一行：`P1 | 拆原型/PRD + opsx-explore | 2026-08-04`。

- [ ] **Step 3: Commit**

```bash
git add workshop/README.md workshop/curriculum/01-讲师手册.md workshop/curriculum/02-学员手册.md workshop/curriculum/03-提示词卡册.md workshop/curriculum/05-示范产物索引.md
git commit -m "docs: link aecos_workshop_v2 as recommended courseware"
```

---

### Task 5: Verification

- [ ] **Step 1: validate-kit still OK**

```bash
node workshop/scripts/validate-kit.mjs
```

Expected: `validate-kit OK 30 files`（v2 非 REQUIRED，不增加计数要求）。

- [ ] **Step 2: Content checklist**

- [ ] `aecos_workshop.md` still exists  
- [ ] v2 has P1-a Chat with `/opsx-explore` and P1-b Chat for PRD  
- [ ] v2 has two 人闸检查 blocks under P1  
- [ ] `P1.md` / `gate-scripts/P1.md` / recovery match v2 semantics  
- [ ] No business code changes  

- [ ] **Step 3: Mark spec status Approved/Implemented note（optional one-line in design.md 修订记录）**

```markdown
| 2026-08-04 | Spec 用户确认；进入 plan 实施 | — |
```

- [ ] **Step 4: Final commit if design.md touched**

```bash
git add docs/superpowers/specs/2026-08-04-aecos-workshop-v2-design.md
git commit -m "docs: mark aecos workshop v2 design confirmed for implementation"
```

---

## Spec coverage (self-review)

| Spec 要求 | Task |
|-----------|------|
| 新建 aecos_workshop_v2.md | T2 |
| Prompt 内联 Chat | T2 |
| Gate 内联人闸检查 | T2 |
| P1 opsx-explore → 原型闸 → PRD 闸 | T1+T2 |
| prep 同步 P1/gate/recovery | T1 |
| 其余卡轻量同步 | T3 |
| README/01–03/05 交叉引用 | T4 |
| 不删 v1 / 不改业务 / 不改 validate REQUIRED | T5 |

Placeholder scan: none intentional.  
Type/path consistency: change 名 `order-checkout-hardening`；产物路径 `workshop/product/order.html`、`PRD-order-checkout.md`。

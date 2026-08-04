# 《AI Coding Workshop》课件 for 福利商城 · 下单结算（v3）

> **项目**：`ywpt_workshop_test`（GitNexus MCP 名：`market_workshop`）  
> **双主线**：模块交付（中等 MVP 结算闭环）+ 课件沉淀  
> **结构**：L1 基线 → L2 原型&PRD → L3 OpenSpec 到启服  
> **时长**：不锁死总时长；下表为**建议分钟**（教练可调）  
> **镜像**：`prep/prompt-cards/L1–L3.md`、`prep/gate-scripts/L1–L3.md`（课件变更须同步）  
> **旧版**：[`aecos_workshop_v2.md`](./aecos_workshop_v2.md) · [`aecos_workshop.md`](./aecos_workshop.md)

配套物料：环境准备 [`aecos_workshop_prep.md`](./aecos_workshop_prep.md)；六件套 `01`–`06`；总入口 `workshop/README.md`。

---

## 一、课程总流程（讲练一体）

每步讲完即练；标注人工处做人闸（高亮块），结论写入 `workshop/compass.md`（L1/L2/L3 段）。

| 建议 min | 环节 | 产物要点 |
|----------|------|----------|
| 课前 | Track 0 环境准备 | 工具就绪（GitNexus 仅安装） |
| ~15 | **L1** 存量扫描与基线 | `product/baseline/project-baseline.md` |
| ~20 | **L2-1** 视觉伴随生成原型 | `product/order.html` |
| ~15 | **L2-2** opsx-explore 生成 PRD | `product/PRD-order-checkout.md` |
| ~10 | **L3-1** opsx-propose | change 四类文件 |
| ~20 | **L3-2** brainstorming 对抗 | change 对齐原型&PRD |
| ~15 | **L3-3** 分钟级 spec/plan | `docs/superpowers/specs` + `plans` |
| ~40 | **L3-4** 多 agent 编码 | 业务代码切片 |
| ~25 | **L3-5** TDD + code review | 质量报告 / degraded |
| ~15 | **L3-6** 启服走查 | `delivery/walkthrough-log.md` |
| 课后 | 罗盘 / 课件收口 | compass + curriculum |

**环境前提**：[`aecos_workshop_prep.md`](./aecos_workshop_prep.md)。**对本仓 analyze 在 L1。**

---

## 二、开场 + 定位 + 方法

**今天要做什么**：半成品福利商城 → 可演示购物车→选地址→下单→扣积分/库存→订单/取消。  
**带走**：L 环节 Chat、人闸、OpenSpec + superpowers（视觉伴随 / explore / propose / brainstorming / writing-plans / 多 agent / TDD / review）。

| 工具 | 干什么 |
|------|--------|
| Cursor Agent | 读项目、写代码、跑命令 |
| OpenSpec | propose / explore / apply |
| superpowers | brainstorming、writing-plans、subagent-driven-development、TDD、requesting-code-review、视觉伴随 |
| GitNexus | 存量扫描、impact |

**成功标准**：中等 MVP 可演示；质量 ≥95% 或诚实 degraded；每闸真人结论。  
**非目标**：真实支付、优惠券、拆单、完整售后。

---

## 三、Phase 0：环境准备

见 [`aecos_workshop_prep.md`](./aecos_workshop_prep.md)。

---

## 四、L1 · 存量扫描与基线（建议 ~15min）

### Chat 对话

```text
使用 GitNexus 对本仓库进行整体分析（不改业务代码；不扩展支付/优惠券）：

1. analyze + 确认本仓：对本仓 analyze；确认 list_repos/status 指向本目录；拉起 GitNexus 预览服务。
2. 围绕「下单结算」扫存量：结合 GitNexus，扫描技术栈、模块、购物车→订单/积分/库存流程与缺口。
   技术部分包括：ER 图、user 图、类图、时序图、状态机、API 契约、并发、安全、前端缺口、数据字典、测试矩阵等（紧扣结算叙事，避免无关膨胀）。
3. 写 baseline + 回填 rules TODO：写入 workshop/product/baseline/project-baseline.md；能确定的项回填 .cursor/rules/00-project-context.mdc 的 TODO。
4. 暂停，等人闸。
```

> [!IMPORTANT]
> **人闸检查（闸-L1）**
>
> 1. 基线是否写清技术栈与订单相关模块（含与结算相关的关键技术视图/契约摘要）？  
> 2. 缺口是否覆盖「结算闭环 / 状态 / 库存与积分异常」？  
> 3. 是否错误引入支付/优惠券等非目标？  
>
> **通过:** 可进 L2。  
> **打回 (R-L1):** 只重跑 GitNexus 相关分析，重写 baseline「缺口」及相关技术节，禁止改业务代码。

### Tips

- 预览拉不起可记债继续。技术清单服务结算叙事即可。建议 change 名：`order-checkout-hardening`。

---

## 五、L2 · 生成原型与 PRD（建议 ~30–40min）

输入需求（仅此一句）：**「开发下单结算」。**

### 5.1 L2-1 · 视觉伴随生成原型（建议 ~20min）

#### Chat 对话

```text
使用 superpowers 的视觉伴随（brainstorming visual companion）生成结算页原型。
输入：一句话需求「开发下单结算」+ workshop/product/baseline/project-baseline.md 缺口。
产出：workshop/product/order.html
要求：
- 区块：收货地址、商品摘要、积分合计、提交、错误态（未选地址/库存不足/积分不足等）
- 可演示：选地址 → 看合计 → 提交 → 可见错误或成功示意
- 中等 MVP；不做管理端；不做真实支付；本步禁止改 backend/frontend 业务代码
视觉伴随不可用时，可用 HTML 直接生成同等原型，但仍须过人闸。
完成后暂停，等人闸-L2-原型。
```

> [!IMPORTANT]
> **人闸检查（人闸-L2-原型）**
>
> 1. 原型能否完成：选地址→看合计→提交→看到错误态？  
> 2. 关键区块是否齐全？  
> 3. 是否仍落在中等 MVP（无支付/管理端偷渡）？  
>
> **通过:** 进入 L2-2。  
> **打回 (R-L2-原型):** 只改被点名的区块/交互/错误态；禁止开始写 PRD 或改业务代码。

---

### 5.2 L2-2 · opsx-explore 生成 PRD（建议 ~15min；仅原型闸通过后）

#### Chat 对话

```text
原型已通过人闸。使用 /opsx-explore，并结合 skill prd-prototype-authoring 与 rule 13-prd-prototype-authoring，
基于已确认的 workshop/product/order.html，生成/完善 workshop/product/PRD-order-checkout.md。
必须包含：文档头、模块索引、术语与状态机（服务端为状态唯一计算方）、负向需求（MUST NOT）、
验收规则表（覆盖闭环+状态+库存/积分/地址异常，并尽量映射 coverage 最小集）。
visual_level 建议 L2。可选 prd-prototype-review，问题记入 compass。
约束：保持中等 MVP；拒绝支付/优惠券/拆单。
完成后暂停，等人闸-L2-PRD。
```

> [!IMPORTANT]
> **人闸检查（人闸-L2-PRD）**
>
> 1. PRD 状态机是否唯一优先级？  
> 2. 负向需求是否清楚？  
> 3. 验收表是否映射到 coverage-scope 最小集？是否与已确认原型一致？  
>
> **通过:** 可进 L3。  
> **打回 (R-L2-PRD):** 只改被点名的状态机或 AC 行；拒绝加需求。

---

## 六、L3 · 基于原型与 PRD 启动 OpenSpec 变更

### 6.1 L3-1 · opsx-propose（建议 ~10min）

#### Chat 对话

```text
/opsx-propose 基于已确认的 workshop/product/PRD-order-checkout.md（可同时引用 order.html 原型）
创建 change，建议名 order-checkout-hardening（以 baseline 为准）。
生成并收敛四类文件：proposal.md、specs/、design.md、tasks.md。
不做支付/优惠券/拆单。完成后暂停，等人闸。
```

> [!IMPORTANT]
> **人闸检查（人闸-L3-propose）**
>
> 1. 四类文件是否齐全？  
> 2. 有无支付/优惠券等范围偷渡？  
>
> **通过:** 进 L3-2。  
> **打回 (R-L3-propose):** 补齐文件；删除/降级越界项。

---

### 6.2 L3-2 · brainstorming 对抗（建议 ~20min）

#### Chat 对话

```text
/brainstorming
对照 workshop/product/order.html 与 workshop/product/PRD-order-checkout.md，
对 openspec/changes/order-checkout-hardening（或本场 change）做对抗分析。
重点：发现 proposal/specs（及 design/tasks）中与原型&PRD 不一致、歧义、遗漏或范围偷渡；
目标是修改后的 change 与原型&PRD 高度一致、无关键分歧。
列出问题 → 我确认后再更新 change 文件。完成后暂停，等人闸。
```

> [!IMPORTANT]
> **人闸检查（人闸-L3-对抗）**
>
> 1. change 是否与原型&PRD 高度一致？  
> 2. propose/specs 被点名的问题是否已改？  
>
> **通过:** 进 L3-3。  
> **打回 (R-L3-对抗):** 按点名修订直到一致。

---

### 6.3 L3-3 · 分钟级 superpowers spec/plan（建议 ~15min）

#### Chat 对话

```text
change 已与原型&PRD 对齐。请使用 writing-plans（superpowers），
基于该 change 生成可执行的分钟级实施计划：
- docs/superpowers/specs/ 下补充/对齐本场设计说明（若已有则修订，勿重复堆业务全文）
- docs/superpowers/plans/ 下生成分钟级 tasks（可直接驱动多 agent）
计划须落在中等 MVP，禁止支付/优惠券。完成后暂停，等人闸。
```

> [!IMPORTANT]
> **人闸检查（人闸-L3-plan）**
>
> 1. plan 是否分钟级、可执行？  
> 2. 是否仍落在中等 MVP？  
>
> **通过:** 进 L3-4。  
> **打回 (R-L3-plan):** 缩小/拆分任务；去掉不可控项。

---

### 6.4 L3-4 · 多 agent 编码（建议 ~40min）

#### Chat 对话

```text
按已确认的 docs/superpowers/plans/ 本场计划，使用 subagent-driven-development（或多 agent 等价模式）切片实现。
每片：更新任务状态；必要时 GitNexus impact/detect_changes；禁止无关重构。
重大范围偏差时暂停请示；否则连续执行至 plan 完成或时间盒结束。
```

> [!IMPORTANT]
> **人闸检查（可选 / 重大偏差时）**
>
> 抽查 1–2 条 PRD AC 是否有代码落点；无重大偷渡。  
> （默认可切片口头确认；阻塞级问题打回修。）

---

### 6.5 L3-5 · TDD + requesting-code-review（建议 ~25min）

#### Chat 对话

```text
1) 按 workshop/prep/coverage-scope.md 用 TDD 补齐最小集（下单成功/库存不足/积分不足/地址缺失等）。
2) 使用 requesting-code-review 对照 PRD AC 做审查；要点写入 compass L3。
3) 目标核心包 ≥95%；达不到则 degraded=true，禁止写「质量通过」。
4) 输出 workshop/quality/quality-summary.md。暂停，等人闸。
```

> [!IMPORTANT]
> **人闸检查（人闸-L3-质量）**
>
> 1. 报告/用例是否诚实完整？  
> 2. ≥95% 或明确 degraded？  
>
> **通过:** 进 L3-6。  
> **打回 (R-L3-质量):** 补用例或诚实 degraded；禁止假通过。

---

### 6.6 L3-6 · 启服预览与人工走查（建议 ~15min）

#### Chat 对话

```text
1) 启动 backend（8080）+ frontend（3000）；确认 backend/database/ 存在。
2) 按人闸走查脚本操作，记录 workshop/delivery/walkthrough-log.md。
3) 阻塞现场修；非阻塞记债。暂停，等产品方签字。
```

> [!IMPORTANT]
> **人闸检查（人闸-L3-走查）**
>
> 1. 登录 → 加购 → 选中 → 进结算。  
> 2. 正常下单 → 订单列表/详情状态正确。  
> 3. 库存或积分不足（或无地址）→ 明确错误。  
> 4. 取消订单（若 AC 含）→ 状态更新。  
>
> **通过:** 主路径 + 至少一条异常通过，或阻塞已修。  
> **打回 (R-L3-走查):** 只修阻塞步骤；更新 walkthrough-log。

---

## 七、课后 · 罗盘与课件收口

补全 compass L1/L2/L3（及兼容 P0–P6 段）；回填 curriculum；`node workshop/scripts/validate-kit.mjs`。

---

## 八、复盘 + 答疑

| 问题 | 简答 |
|------|------|
| 为何 L2 先视觉伴随再 explore PRD？ | 先锁定看得见的交互，再沉淀可测规格 |
| L3-2 与 L3-3 差别？ | 先把 change 对齐原型&PRD；再出分钟级执行 plan |
| 金丝雀能否代替人闸？ | **不能** |
| 95% 达不到？ | 诚实 degraded |

踩坑：`curriculum/06-踩坑-playbook.md`。评分：`04-评分表.md`。

---

## 九、附录 · 旧 P0–P6 ↔ L 映射

| 旧 | 新 |
|----|-----|
| P0 | L1 |
| P1 | L2-1 + L2-2 |
| P2 | L3-1 + L3-2 |
| P3 | L3-4 |
| P4 | L3-5 |
| P5 | L3-6 |
| P6 | 课后罗盘/课件收口 |

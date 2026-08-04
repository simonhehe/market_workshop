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
使用 GitNexus 对本仓库进行整体分析（不改业务代码；不扩展支付/优惠券）：

1. analyze + 确认本仓：对本仓 analyze；确认 list_repos/status 指向本目录；拉起 GitNexus 预览服务。
2. 围绕「下单结算」扫存量：结合 GitNexus，扫描技术栈、模块、购物车→订单/积分/库存流程与缺口。
   技术部分包括：ER 图、user 图、类图、时序图、状态机、API 契约、并发、安全、前端缺口、数据字典、测试矩阵等（紧扣结算叙事，避免无关膨胀）。
3. 写 baseline + 回填 rules TODO：写入 workshop/product/baseline/project-baseline.md；能确定的项回填 .cursor/rules/00-project-context.mdc 的 TODO。
4. 暂停，等人闸。
```

> [!IMPORTANT]
> **人闸检查**
>
> 1. 基线是否写清技术栈与订单相关模块（含与结算相关的关键技术视图/契约摘要）？  
> 2. 缺口是否覆盖「结算闭环 / 状态 / 库存与积分异常」？  
> 3. 是否错误引入支付/优惠券等非目标？  
>
> **通过:** 缺口可指导 PRD；无范围膨胀。  
> **打回 (R-P0):** 只重跑 GitNexus 相关分析，重写 baseline「缺口」及相关技术节，禁止改业务代码。

### Tips

- 「预览服务」：用 Chat 触发 GitNexus 提供的预览/UI/wiki（以本机已装版本为准）；拉不起则记债并继续扫描，不阻塞 baseline。  
- 技术清单服务「下单结算」叙事即可，不必做成完整企业架构册。  
- 干跑示范缺口方向：无结算页、地址可选、错误态不统一、缺测试、状态机需冻结。建议 change：`order-checkout-hardening`。

---

## 五、P1：原型与 PRD（15–45）

输入需求（仅此一句）：**「开发下单结算」。**

### 5.1 P1-a · 用 `/opsx-explore` 生成原型

#### Chat 对话

```text
/opsx-explore
目标：根据一句话需求「开发下单结算」，结合 workshop/product/baseline/project-baseline.md 中的缺口，探索并生成高质量结算页 HTML 原型。
产出：workshop/product/order.html
要求：
- 必须包含区块：收货地址、商品摘要、积分合计、提交、错误态（至少覆盖未选地址、库存不足、积分不足）
- 交互可演示：选地址 → 看合计 → 提交 → 可见错误或成功示意
- 对齐中等 MVP；不做管理端；不做真实支付
- 本步禁止修改 backend/frontend 业务代码
探索时可参考仓库半成品与已有 product 目录，但以生成完整可演示原型为准。
完成后暂停，等人闸-原型。
```

> [!IMPORTANT]
> **人闸检查（人闸-原型）**
>
> 1. 原型能否完成：选地址→看合计→提交→看到错误态？  
> 2. 关键区块是否齐全？  
> 3. 是否仍落在中等 MVP（无支付/管理端偷渡）？  
>
> **通过:** 进入 5.2 写 PRD。  
> **打回 (R-P1-原型):** 只改被点名的区块/交互/错误态；禁止开始写 PRD 或改业务代码。

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

> [!IMPORTANT]
> **人闸检查（人闸-PRD）**
>
> 1. PRD 状态机是否唯一优先级？  
> 2. 负向需求是否清楚？  
> 3. 验收表是否映射到 coverage-scope 最小集？是否与已确认原型一致？  
>
> **通过:** 可进入 P2。  
> **打回 (R-P1-PRD):** 只改被点名的状态机或 AC 行；拒绝加需求。

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

> [!IMPORTANT]
> **人闸检查**
>
> 1. tasks 是否都能在剩余时间内完成？  
> 2. 有无偷渡需求？  
> 3. 对抗 5 题是否都有书面结论？  
>
> **通过:** tasks = 中等 MVP。  
> **打回 (R-P2):** 打开 tasks.md，删除或降级越界任务；重新对齐 coverage-scope 最小集。

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

> [!IMPORTANT]
> **人闸检查**
>
> 任选 2 条 PRD AC，请学员指出代码位置与行为。  
>
> **通过:** 抽查 AC 有实现落点。  
> **打回 (R-P3):** 只修 AC 失败项；禁止大范围重构；对照 canary。

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

> [!IMPORTANT]
> **人闸检查**
>
> 1. 报告是否写明白名单分母？  
> 2. 数字 ≥95% 或明确 degraded？  
> 3. 最小用例是否全列结果？  
>
> **通过:** 报告诚实且可复训。  
> **打回 (R-P4):** 宣称通过但证据不足 → 优先补白名单内失败用例；或设 degraded 并写明缺口。

---

## 九、P5：启服走查（155–170）

### Chat 对话

```text
1) 启动 backend + frontend（端口 8080 / 3000；须存在 backend/database/ 目录）。
2) 按下列走查脚本操作，记录 workshop/delivery/walkthrough-log.md。
3) 阻塞 bug 现场修；非阻塞记债项。
4) 暂停，等产品方签字。
```

> [!IMPORTANT]
> **人闸检查（含走查脚本）**
>
> 1. 登录 → 加购 → 选中 → 进结算。  
> 2. 正常下单 → 订单列表可见 → 详情状态正确。  
> 3. 制造库存或积分不足（或无地址）→ 看到明确错误。  
> 4. 取消订单（若 AC 含）→ 状态更新。  
>
> **通过:** 主路径 + 至少一条异常通过，或阻塞项已修。  
> **打回 (R-P5):** 只修阻塞步骤对应代码；更新 walkthrough-log。

---

## 十、P6：罗盘收口与课件（170–180）

### Chat 对话

```text
1) 补全 workshop/compass.md 所有段字段。
2) 将本场提示词/踩坑/评分结果填入 workshop/curriculum/01–06（可标「待润色」但不可空段）；授课主稿以 aecos_workshop_v2.md 为准时可注明。
3) 运行：在仓库根执行 node workshop/scripts/validate-kit.mjs，确认 OK。
4) 教练确认六件套可给下一班或列出明确课后 PR。
```

> [!IMPORTANT]
> **人闸检查**
>
> 教练确认：compass 段齐全；六件套可复训或课后 PR 列表明确。（本段无 R-P6；问题记入课后债。）

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

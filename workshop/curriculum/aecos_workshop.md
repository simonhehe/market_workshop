# 《AI Coding Workshop》课件 for 福利商城 · 下单结算

> **项目**：`ywpt_workshop_test`（GitNexus MCP 名：`market_workshop`）
>
> **时长**：约 3 小时现场（**讲练穿插**；课前 Track 0 另计）
>
> **双主线**：模块交付（中等 MVP 结算闭环）+ 课件沉淀（compass → curriculum）
>
> **干跑场次参考**：`unattended-overnight-001`（见 `workshop/compass.md`）

配套物料：

| 类型 | 路径 |
|------|------|
| 环境准备 | `curriculum/aecos_workshop_prep.md`（深化：`prep/env-setup.md`） |
| Prompt 卡 | `workshop/prep/prompt-cards/P0`–`P6.md` + `recovery-cards.md` |
| 人闸 | `workshop/prep/gate-scripts/P0`–`P5.md` |
| 六件套 | `workshop/curriculum/01`–`06` |
| 总入口 | `workshop/README.md` |

---

## 一、课程总流程（讲练一体）

每个 Phase **讲完即练**；每闸必须有产品方结论写入 `workshop/compass.md`。

| 时间 | 环节 | 合计 |
|------|------|------|
| 课前 | Track 0 / Phase 0：环境准备 | 另计 |
| 0–15 min | P0：存量扫描与基线 | 15 |
| 15–45 min | P1：原型与 PRD | 30 |
| 45–75 min | P2：OpenSpec + 对抗收敛 | 30 |
| 75–120 min | P3：编码与审查 | 45 |
| 120–155 min | P4：质量门禁 | 35 |
| 155–170 min | P5：启服走查 | 15 |
| 170–180 min | P6：罗盘收口与课件 | 10 |

> **2.5h 压缩版**：
>
> - Phase 0 / Track 0 提前完成；
> - P4 允许诚实 `degraded`（禁止宣称质量通过）；
> - P1 或 P6 选缩：原型用预制 `order.html`，课件课后润色。

**环境前提**（课前完成）：已 clone；JDK 21、Maven、Node 18+；Cursor 已登录；按 [`aecos_workshop_prep.md`](./aecos_workshop_prep.md) 装好 OpenSpec / superpowers / GitNexus（**仅安装**）；`backend/database/` 建议课前建好。**对本仓 GitNexus analyze 在现场 P0 做。**

---

## 二、开场 + 定位 + 方法

**今天要做什么**

- 案例：半成品福利商城 → 可演示「购物车 → 选地址 → 下单 → 扣积分/库存 → 订单列表/取消」。
- 带走：P0–P6 Prompt 卡用法 + OpenSpec/superpowers 骨架 + 人闸习惯 + 踩坑 playbook。

**工具分工**

| 工具 | 干什么 |
|------|--------|
| Cursor Agent | 读项目、写代码、跑命令 |
| OpenSpec + superpowers | SDD / 对抗 / 计划执行 / TDD / review |
| Rules（`.cursor/rules`） | 约束范围，减少跑偏 |
| Skills（prd / codeflow / product-ui） | 提高 PRD / 实现 / 视觉对齐质量 |
| GitNexus | 存量扫描、impact、防无关改动 |

**成功标准（双主线）**

1. 模块：中等 MVP 闭环 + 状态 + 库存/积分/地址异常可演示。
2. 质量：核心包 ≥95% **或** 诚实 `degraded`（不得假装通过）。
3. 过程：每闸真人产品方结论；禁止跳闸伪造。
4. 课件：`compass` 齐全；`curriculum` 可给下一班。

**非目标**：真实支付、优惠券、拆单、完整售后；本场不强制跑齐 harness 运行时。

---

## 三、Phase 0：环境准备

学员必装清单（Chat 触发、含插图）：[`aecos_workshop_prep.md`](./aecos_workshop_prep.md)。  
深化清单（可选）：[`workshop/prep/env-setup.md`](../prep/env-setup.md)。

课前只要求：**工具装好并能验证版本**（含 GitNexus **安装成功**）。**对本仓 analyze 建索引在 P0 做。**

---

## 四、P0：存量扫描与基线（0–15）

### 0、对本仓建 GitNexus 索引（环境准备第 6 步之后）

环境准备只完成安装；进入正文后先建索引。在 **Agent 对话框** 输入：

```text
对本仓库根目录执行 GitNexus analyze 建索引；完成后用 status / list（或 MCP list_repos）确认索引指向本目录。
注意：MCP 名多为 market_workshop，文件夹名可能是 ywpt_workshop_test，以路径匹配为准。把 status 摘要回复给我。
```

- [ ] `status` 本仓已索引（符号/关系计数非空）  
- [ ] `list_repos` 路径指向本目录  

若索引已存在、仅需刷新：可在上述提示中改为「重新 analyze」。

### 1、当下有什么

持卡：`prep/prompt-cards/P0.md`  
剧本：`prep/gitnexus-queries.md`

建议对话：

```text
按 workshop/prep/prompt-cards/P0.md 与 gitnexus-queries.md 对本仓做存量扫描：
- list_repos 确认索引指向本目录（market_workshop）
- query / context OrderService、CartService、/orders
- 写入 workshop/baseline/project-baseline.md（技术栈、模块、缺口、建议 change 名）
约束：不改业务代码；不扩展支付/优惠券。
```

### 2、人闸

产品方按 `gate-scripts/P0.md`：缺口是否覆盖「下单结算」叙事？  
打回用 `R-P0`。

### 3、示范结论（干跑）

缺口 G1–G5：无结算页、地址可选、错误态不统一、缺测试、状态机需冻结。  
建议 change：`order-checkout-hardening`。

---

## 五、P1：原型与 PRD（15–45）

### 1、一句话需求

**「开发下单结算」。**

持卡：`prep/prompt-cards/P1.md`  
骨架：`product/order.html`（须含 address / items / points-summary / submit / error-states）

```text
按 workshop/prep/prompt-cards/P1.md：
1. 基于 workshop/product/order.html 补全结算页关键区块（地址、商品摘要、积分合计、提交、错误态）
2. 我确认原型后，按 .cursor/skills/prd-prototype-authoring 完善 workshop/product/PRD-order-checkout.md
   （状态机、负向需求、验收表覆盖中等 MVP）
3. 可选 prd-prototype-review；问题记入 compass
约束：不做管理端；不做真实支付。
```

### 2、是否需要 PRD？

| 问 | 答（本场口径） |
|----|----------------|
| 直接用原型不行吗？ | 原型管「看得见」；PRD 管状态唯一源、负向需求、可测 AC |
| 谁对 PRD 负责？ | 产品方人闸签字；执行者持卡撰写 |
| 检查点？ | 见 `gate-scripts/P1.md`：选地址→合计→提交→错误态；状态机优先级；AC↔coverage-scope |

### 3、对抗微调（可选）

```text
/brainstorming 根据本会话与 workshop/product/ 下文件，分析 PRD-order-checkout.md 可完善点；
我确认后再逐条更新。保持中等 MVP，拒绝支付/优惠券。
```

---

## 六、P2：OpenSpec + 对抗（45–75）

### 1、生成变更

```text
/opsx-propose 基于 workshop/product/PRD-order-checkout.md 创建 change，建议名 order-checkout-hardening
```

### 2、对抗固定 5 题（必须改生成物）

1. 范围是否偷渡优惠券/支付/售后？  
2. 订单状态优先级是否唯一？  
3. 库存与积分并发/超扣如何处理？  
4. 未登录/鉴权失败契约？  
5. 验收是否可测（对应 `coverage-scope` 最小集）？  

```text
/brainstorming 对 openspec/changes/order-checkout-hardening 做对抗分析（上述 5 题），
提出改进建议；我确认后更新 proposal/specs/design/tasks，删除不可控任务。
```

### 3、人闸

`gate-scripts/P2.md`：tasks 边界 = 中等 MVP，无范围偷渡。打回 `R-P2`。

---

## 七、P3：编码与审查（75–120）

### 1、实现

持卡：`P3.md`。仅做 `tasks.md` 内切片。

```text
按 openspec/changes/order-checkout-hardening/tasks.md 实现下一未完成任务；
遵循 OpenSpec apply / codeflow-code-testing；每片完成后：
- 更新 tasks 状态
- GitNexus impact / detect_changes 看爆炸半径
- 禁止无关重构；对照 workshop/prep/canary/
```

干跑已落地要点（示范，非抄答案强制）：

- `OrderCreate.vue` + 路由 `/orders/create` **注册在** `/orders/:id` **之前**  
- `OrderService`：`addressId` 必填提前校验  
- `PUT /cart/{id}` 支持 `isSelected`  

### 2、审查

```text
/requesting-code-review 对照 PRD AC（AC-ROUTE / AC-ADDR 等）抽查落点；要点写入 compass P3。
```

人闸：`gate-scripts/P3.md`（口述 AC，不替代走查）。打回 `R-P3`。

---

## 八、P4：质量门禁（120–155）

持卡：`P4.md`。白名单：`prep/coverage-scope.md`。

```text
按 workshop/prep/coverage-scope.md 与 P4 卡：
- TDD 补齐最小集：下单成功 / 库存不足 / 积分不足 / 地址缺失；API 契约与鉴权；集成一致性；E2E 主路径+异常
- 目标核心包行&分支 ≥95%；达不到则 degraded=true，禁止写「质量通过」
- 输出 workshop/quality/quality-summary.md（含 canary 差异表 + AC 追溯）
```

干跑诚实结论：`OrderServiceTest` 5 绿；**未达 95% / 无 E2E → degraded**。人闸 `P4`；打回 `R-P4`。

---

## 九、P5：启服走查（155–170）

```text
按 prep/env-setup.md 启 backend+frontend；
严格按 gate-scripts/P5.md 走查，记录 workshop/delivery/walkthrough-log.md。
阻塞现场修；非阻塞记债。
```

建议路径：登录 → 加购勾选 → `/orders/create` 选地址 → 提交 → 订单列表 → 取消；另验无地址错误文案。

干跑债项：浏览器真人点选未完成（API 级已绿）。打回 `R-P5`。

---

## 十、P6：罗盘收口与课件（170–180）

```text
按 P6 卡：补全 compass 所有段；回填 curriculum/01–06；
运行 node workshop/scripts/validate-kit.mjs；列出课后 PR（若有）。
```

---

## 十一、复盘 + 答疑

| 问题 | 简答 |
|------|------|
| 为什么必须人闸？ | AI 可加速；范围与 AC 由产品方锁死，防止偷渡与假通过 |
| 95% 达不到怎么办？ | 诚实 `degraded`；禁止宣称质量通过；课后补 JaCoCo/E2E |
| GitNexus 名对不上？ | 看路径是否本仓；MCP 名常为 `market_workshop` |
| `/orders/create` 404？ | `:id` 路由抢先；create 必须注册在前 |
| 积分不足？ | 种子积分 vs 商品积分；见 playbook |
| 金丝雀能否代替人闸？ | **不能**；仅超时/故障对照续跑 |

踩坑全表：`workshop/curriculum/06-踩坑-playbook.md`。

---

## 十二、评分（满分 14，≥10 合格）

见 `curriculum/04-评分表.md`。维度：基线 / 需求 / 蓝图 / 代码审查 / 质量（含诚实 degraded）/ 走查 / 罗盘。

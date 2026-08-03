# YWPT Workshop 全过程设计：存量扫描 → 交付 → 课件沉淀

**日期:** 2026-08-04  
**状态:** Draft（待用户审阅后进入 implementation plan）  
**载体项目:** `ywpt_workshop_test` 福利商城（Spring Boot 3 + Vue 3）  
**编法:** 主线 + 旁路金丝雀（Approach B）

---

## 1. 目标与成功标准

### 1.1 双主线目标

| 轨 | 交付物 |
|----|--------|
| **模块交付** | 在半成品存量上补齐「下单结算」中等 MVP，可启服演示 |
| **课件沉淀** | 跑通后产出可给他人复训的 workshop 课件包 |

### 1.2 成功标准

1. **模块:** 购物车→选地址→确认下单→扣积分/库存→订单列表/详情状态流转；库存不足、积分不足等基础异常可演示。
2. **质量:** 下单结算**核心包**行&分支覆盖率 ≥ 95%（白名单分母）；契约/集成/E2E 最小集全绿；或诚实标注 `degraded`（不得宣称质量通过）。
3. **过程:** 七段均有真人产品方人闸结论；无「跳过人闸假装通过」。
4. **课件:** `workshop/compass.md` 七段齐全；`workshop/curriculum/` 六件套可供下一班直接开课。

### 1.3 非目标（YAGNI）

- 真实三方支付、优惠券、拆单、完整售后。
- 本场不强制补齐/运行缺失的 harness 运行时（概念可讲；机械门禁用 compass + 脚本清单代替）。
- 不把无关已索引仓库当作本场扫描对象；本场 GitNexus 目标为本仓福利商城。
- 不把「一日版深度」的全部探索时间塞进现场——深度靠课前预制与金丝雀对照。

---

## 2. 约束（已锁定）

| 项 | 选择 |
|----|------|
| 成功标准权重 | 双主线等权（模块 + 课件） |
| 代码起点 | 保留现有 Order/Cart 等，半成品存量补齐 |
| 内容深度 | 一日版七段全覆盖 |
| 现场时长 | 约 3 小时（环境预装 + 预制提示词/骨架加速） |
| MVP 边界 | 中等：闭环 + 状态流转 + 基础异常 |
| 人闸 | 每闸真人产品方；课件教如何开闸 |
| 覆盖率 | 仅结算核心包硬卡 95%；其余存量不进分母 |
| 罗盘 | 现场轻量 `compass.md` → 课后正式 `curriculum/` |

---

## 3. 架构：主线 + 旁路金丝雀

```text
Track 0 课前金丝雀                Track 1 现场主线 (~180min)
─────────────────                ─────────────────────────
环境一键 / 索引就绪               P0 扫描基线
Prompt 卡 + 人闸脚本             P1 原型 → PRD（真人确认）
产物骨架 + 测试夹具               P2 OpenSpec + 对抗收敛
参考实现分支 + 达标报告样例       P3 编码 + review/fix
坑位清单（playbook 种子）         P4 核心包 95% + 多层测试 + 报告
         \                        P5 启服 + 真人走查
          \── 超时/故障时对照 ──→  P6 compass → 课件待办
              （不替代人闸）
```

**原则:** 金丝雀只读对照与续跑降级；**绝不替代**产品方人闸。

---

## 4. 阶段设计（P0–P6）

### P0 — 存量扫描与项目基线（0–15 min）

- **动作:** 对本仓执行/使用预装 GitNexus 索引；查询社区、流程、订单相关符号与路由。
- **产物:** `workshop/baseline/project-baseline.md`（技术栈、业务模块图、订单相关缺口清单）；回填 `.cursor/rules/00-project-context.mdc` 中可确定的 TODO。
- **人闸:** 产品方确认「缺口清单是否覆盖下单结算叙事」。
- **Prompt 卡:** `prep/prompt-cards/P0.md`。

### P1 — 一句话需求 → 原型 → PRD（15–45 min）

- **输入:** 「开发下单结算」。
- **动作:** 视觉伴随/原型技能在骨架上生成/微调 `workshop/product/order.html`；真人确认后按 `prd-prototype-authoring` 补全 PRD 关键节（状态机、负向需求、验收表至少覆盖中等 MVP）。
- **产物:** `order.html`、`workshop/product/PRD-order-checkout.md`；可选 `prd-prototype-review` 快检记录。
- **人闸:** 原型通过；PRD 范围与 AC 通过。

### P2 — OpenSpec 蓝图 + 对抗收敛（45–75 min）

- **动作:** `/opsx-propose`（或等价）生成 change；用 brainstorming **对抗分析**（固定 5 题清单：范围膨胀、状态歧义、库存/积分并发、鉴权、测试不可观测性）修订 proposal/specs/design/tasks，拆掉不可控项。
- **产物:** `openspec/changes/<change>/` 下可控的 proposal、specs、design、tasks。
- **人闸:** 产品方确认「tasks 边界 = 中等 MVP，无范围偷渡」。

### P3 — 编码、审查、修复（75–120 min）

- **动作:** 按 tasks 切片实现；OpenSpec apply / codeflow；review + GitNexus `impact` / `detect_changes`；对照金丝雀 diff 防跑偏。
- **产物:** 结算相关前后端改动；review 记录进 compass。
- **人闸:** 产品方按 AC 抽查关键行为描述（非点按钮，点「是否实现了约定」）。

### P4 — TDD / 契约 / 集成 / E2E + 质量报告（120–155 min）

- **覆盖率 scope（硬门禁）:** 白名单包含结算核心包，例如：
  - 后端: `OrderService` 结算路径、`OrderController` 创建/取消/详情相关、Cart→Order 协作点（最终名单在 baseline 中冻结）。
  - 前端: 结算页/下单确认相关 view + api 模块。
- **必须绿的最小集:**
  - UT: 下单成功、库存不足、积分不足、地址缺失。
  - API: 创建订单契约 + 鉴权失败。
  - 集成: 扣库存与扣积分事务一致性。
  - E2E: 主路径一条；取消或异常一条。
- **产物:** `workshop/quality/coverage-report.*`、`quality-summary.md`（含与金丝雀差异表、AC 追溯）。
- **人闸:** 产品方确认报告数字与 scope 定义可读、可复训。

### P5 — 启服与人工走查（155–170 min）

- **动作:** 启动 backend/frontend；按 `prep/gate-scripts/P5-walkthrough.md` 走查（含异常路径）。
- **产物:** `workshop/delivery/walkthrough-log.md`；阻塞项现场修，非阻塞记债项。
- **人闸:** 产品方签字通过或列出必须修复项。

### P6 — 罗盘收口与课件沉淀（170–180 min）

- **动作:** 补齐 `compass.md` 各段字段；列出 `curriculum/` 生成待办（可课后完成渲染，但字段现场填完）。
- **产物:** 完整 compass；课件六件套清单勾选状态。

---

## 5. 目录与罗盘

```text
workshop/
  README.md
  prep/
    env-setup.md
    prompt-cards/          # P0..P6 主卡 + 回收卡
    gate-scripts/          # 产品方每闸问询与通过标准
    canary/                # 参考实现指针、达标报告样例、坑位种子
  compass.md               # 现场轻量主账
  baseline/
  product/                 # order.html + PRD
  quality/
  delivery/
  curriculum/              # 课后正式课件包
```

### 5.1 `compass.md` 每段固定字段

- 目标
- 使用的 Prompt 卡 ID
- 关键指令 / 工具调用摘要
- 产物路径
- 人闸结论（通过 / 打回原因 / 签字人）
- 耗时（分钟）
- 踩坑
- `degraded`（boolean + 原因）

### 5.2 课后 `curriculum/` 六件套

1. `01-讲师手册.md` — 时间盒、人闸话术、超时策略  
2. `02-学员手册.md` — 七段目标与检查清单  
3. `03-提示词卡册.md` — P0–P6 主卡 + 回收卡  
4. `04-评分表.md` — 基线/需求/蓝图/代码/质量/交付/罗盘完整度  
5. `05-示范产物索引.md` — 链到 canary + 本场实际产物  
6. `06-踩坑-playbook.md` — 汇总 compass 踩坑  

---

## 6. 工具链映射

| 阶段 | 工具 / Skill |
|------|----------------|
| P0 | GitNexus list/query/context；项目规则回填 |
| P1 | brainstorming visual companion；prd-prototype-authoring / review |
| P2 | openspec-propose；brainstorming 对抗；收敛 design/tasks |
| P3 | openspec-apply；codeflow-code-testing；review-rca；GitNexus impact/detect_changes |
| P4 | TDD / 覆盖率白名单；契约·集成·E2E |
| P5 | 实际启服 + 走查脚本 |
| P6 | compass → curriculum 生成 |

OpenSpec 标准链保持：Spec → Design → Tasks → Code → Test → Review → RCA → Archive（本场 Archive 可课后）。

---

## 7. 失败与降级

| 场景 | 处理 |
|------|------|
| 人闸打回 | 使用本段「回收卡」修订；时限从缓冲扣；记 compass |
| 覆盖率未达 95% | 优先补核心路径测试；仍不足则 `degraded=true`，禁止宣称质量通过 |
| 走查阻塞 | P5 优先修；非阻塞写入 delivery 债项与课件「已知问题」 |
| 工具/环境故障 | 启用金丝雀对照续跑；compass 标注 degraded 原因 |
| 单段超时 | 跳过已预制机械步骤，保留人闸与关键决策；记跳过项 |

---

## 8. 角色

| 角色 | 职责 |
|------|------|
| 学员 / 执行者 | 持 Prompt 卡跑主线；维护 compass |
| 产品方（真人） | 每闸按 gate-scripts 提问；通过/打回 |
| 教练 / 金丝雀守护 | 课前 Track 0；现场仅超时或故障时启用对照 |

---

## 9. 3 小时时间盒（摘要）

| 分钟 | 阶段 |
|------|------|
| 0–15 | P0 基线 |
| 15–45 | P1 原型/PRD |
| 45–75 | P2 OpenSpec 对抗 |
| 75–120 | P3 实现/审查 |
| 120–155 | P4 质量 |
| 155–170 | P5 走查 |
| 170–180 | P6 罗盘收口 |

课前 Track 0 不计入这 180 分钟。

---

## 10. 实现本设计时的工作包（供后续 plan）

设计获批并进入 implementation plan 后，建议工作包顺序：

1. 建立 `workshop/` 骨架与 `compass.md` 模板  
2. 编写 prep：env-setup、Prompt 卡、人闸脚本、覆盖率 scope 配置草稿  
3. 本仓 GitNexus 索引与 P0 查询剧本  
4. 金丝雀：参考实现分支策略 + 样例质量报告  
5. 冻结结算核心包白名单（对照现有 Order/Cart 代码）  
6. 试跑一遍主线（可 degraded）并生成第一版 curriculum  
7. 学员 README 与评分表打磨  

---

## 11. 风险与假设

| 风险 | 缓解 |
|------|------|
| 真人每闸导致超时 | 闸卡限时 3–5 分钟；超时降级策略预写 |
| 现有订单代码「半成品」缺口不清 | P0 强制产出缺口清单并人闸确认 |
| 95% 在 3h 内过紧 | 白名单收窄 + 预制夹具 + 允许诚实 degraded |
| Write hook / harness 脚本缺失 | 已用最小 guard stub 恢复编辑；完整 harness 不纳入本场必跑 |
| GitNexus 当前索引了其他仓 | 课前必须对本仓完成 index |

**假设:** 学员已具备基本 Cursor / OpenSpec / 前后端启动能力；教练可提供产品方或邀请同事扮演。

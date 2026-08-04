# AECOS Workshop 课件 v2 设计

**日期:** 2026-08-04  
**状态:** Approved（用户确认；进入 implementation plan）  
**载体:** `ywpt_workshop_test` / GitNexus `market_workshop`  
**基线课件:** `workshop/curriculum/aecos_workshop.md`（v1，保留）

---

## 1. 目标与成功标准

### 1.1 目标

新建 `workshop/curriculum/aecos_workshop_v2.md`，在保持 P0–P6 时间盒与双主线叙事的前提下：

1. 将 `workshop/prep/prompt-cards` 的步骤与约束**直接写入各段 Chat 对话**，学员无需打开 `.md` 持卡。  
2. 将 `workshop/prep/gate-scripts` 的问句、通过/打回标准写入各段 **「人闸检查」** 区块，产品方无需打开 gate 文件。  
3. **P1** 改为：`/opsx-explore` 先生成高质量 `order.html` → **人闸-原型** → 再生成高质量 PRD → **人闸-PRD**。  
4. 课件中 Prompt / 人闸文案若变更，**同步** `workshop/prep/` 对应镜像，避免双源漂移。

### 1.2 成功标准

- 现场只打开 v2 即可复制 Chat、完成人闸；不依赖「见 `P*.md` / `gate-scripts/*.md`」外链。  
- P1 两道人闸均可独立打回（原型 / PRD）。  
- `prep/prompt-cards/P1.md`、`gate-scripts/P1.md`、`recovery-cards.md` 与 v2 文案一致。  
- v1 文件仍存在；业务代码不因本变更修改。

### 1.3 非目标

- 删除或替换 `aecos_workshop.md`（v1）。  
- 删除 `prep/prompt-cards` / `gate-scripts`（保留为 kit 镜像与校验资产）。  
- 将 `coverage-scope.md` / `gitnexus-queries.md` 全文抄入课件（仅摘要 + 镜像路径）。  
- 修改 validate-kit REQUIRED 清单（v2 为新增课件，非强制 REQUIRED）。  
- 修改业务代码或 OpenSpec change 实现。

---

## 2. 已锁定决策

| ID | 决策 |
|----|------|
| D1 | 方案 1：阶段内嵌（Chat + 人闸检查 + 可选 Tips） |
| D2 | P1 起点：一句话需求 + baseline；`/opsx-explore` 生成原型（可参考存量，不要求「按旧 P1 补骨架」话术） |
| D3 | P1 人闸：**两道**（原型通过后再写 PRD；PRD 再过闸） |
| D4 | 课件变更 → 同步 `workshop/prep/`；prep 先改时亦应回写 v2 |
| D5 | 新文件名：`aecos_workshop_v2.md` |
| D6 | P1 prep 采用**单文件两节**（不另增 P1a/P1b 文件），降低 kit 文件数变动 |

---

## 3. 文档结构

### 3.1 v2 顶层

保留 v1 的：总流程表、开场与工具分工、成功标准/非目标、Phase 0 链到 `aecos_workshop_prep.md`、复盘 FAQ、评分指向。

配套物料表调整为：

| 类型 | 说明 |
|------|------|
| 环境准备 | `aecos_workshop_prep.md` |
| Prompt / 人闸 | **已内联于各段**；镜像 `prep/prompt-cards`、`prep/gate-scripts` |
| 六件套 / README | 同 v1 |

### 3.2 各段统一模板（P0–P6）

```text
## Pn · 标题（时限）
### Chat 对话
  ```text
  …内联原 Prompt 卡步骤与约束；禁止「按 xxx.md」作为唯一指令…
  ```
### 人闸检查
  - 问句 1…n（内联原 gate）
  - **通过:** …
  - **打回:** …（内联 R-Pn 处置要点）
### Tips / 示范（可选）
```

### 3.3 P1 分步（核心）

| 子步 | Chat 要点 | 人闸要点 |
|------|-----------|----------|
| **P1-a 原型** | `/opsx-explore`；输入「开发下单结算」+ 参考 baseline 缺口；产出 `workshop/product/order.html`（地址、商品摘要、积分合计、提交、错误态）；约束：不做管理端、不做真实支付、本步不改业务代码 | 选地址→合计→提交→错误态？区块齐全？中等 MVP？**通过 / 打回 R-P1-原型** |
| **P1-b PRD** | 仅原型闸通过后执行；按 `prd-prototype-authoring` 与 rule `13-prd-prototype-authoring`；产出/完善 `workshop/product/PRD-order-checkout.md`（状态机、负向需求、验收表覆盖闭环+状态+库存/积分异常）；可选 prd-prototype-review | 状态机优先级唯一？负向清楚？AC 映射 coverage 最小集？与原型一致？**通过 / 打回 R-P1-PRD** |

时间仍落在 15–45 min；两闸各约 3–5 min。

### 3.4 其余阶段要点

- **P0：** Chat 合并「analyze 建索引 + 扫描写 baseline」（内联查询步骤摘要）；人闸内联原 P0 三问。  
- **P2–P4、P6：** 语义同 v1；Chat/人闸内联，去掉外链持卡。  
- **P5：** 走查脚本步骤写入「人闸检查」正文。

---

## 4. prep 镜像同步

| 文件 | 变更 |
|------|------|
| `prep/prompt-cards/P1.md` | 改为两节：P1-a 原型（opsx-explore）+ P1-b PRD；去掉「开闸见 gate-scripts」外链式依赖，改为「人闸见课件/本卡对应人闸节」或简述两闸 |
| `prep/gate-scripts/P1.md` | 两节：人闸-原型、人闸-PRD |
| `prep/prompt-cards/recovery-cards.md` | R-P1 拆为 R-P1-原型、R-P1-PRD（或同节两条） |
| 其他 P0/P2–P6 / gate | 若 v2 内联时改写了措辞，同步回 prep；无语义变化可仅去「见 xxx.md」类自指 |

**同步规则：** 以**正在授课的 v2 文案**为现场权威；提交前检查 v2 ↔ prep 对应段落一致。

---

## 5. 交叉引用（轻量）

更新（不删 v1 链接）：

- `workshop/README.md` — 增加 v2 为推荐授课稿  
- `curriculum/01`–`03`、`05` — 注明 v2 入口  

不强制改 `compass.md` 历史场次记录（可在备注中追加 v2 文件名，可选）。

---

## 6. 风险与缓解

| 风险 | 缓解 |
|------|------|
| v2 与 prep 再次漂移 | 同步规则写进本 spec；改课件必改 prep |
| P1 两闸超时 | 压缩版允许原型用预制稿，但 v2 默认路径仍为 explore 生成 |
| `/opsx-explore` 行为因工具版本差异 | Chat 写明产出路径与必备区块，不依赖工具内部实现细节 |

---

## 7. 实现范围（进入 plan 时拆任务）

1. 新建 `aecos_workshop_v2.md`  
2. 更新 `P1.md`、`gate-scripts/P1.md`、`recovery-cards.md`  
3. 按需同步其余 prompt/gate 措辞  
4. README + 01–03、05 交叉引用  
5. 自检：v2 无「按 prompt-cards/P*.md」「按 gate-scripts」作为唯一操作入口  

---

## 8. 修订记录

| 日期 | 变更 | 作者 |
|------|------|------|
| 2026-08-04 | 初稿：方案1 + P1 双人闸 + prep 同步 | brainstorming |
| 2026-08-04 | 用户确认 Spec；implementation plan 见 `docs/superpowers/plans/2026-08-04-aecos-workshop-v2.md` | — |
| 2026-08-04 | Implemented：`aecos_workshop_v2.md` + prep P1/gates/recovery + 交叉引用（commits 75a5c31…9385d72） | subagent-driven |

# YWPT Workshop Kit

福利商城「下单结算」全过程工作坊：在半成品存量上跑通 **模块交付** 与 **课件沉淀** 双主线。

完整设计见 [全过程设计 spec](../docs/superpowers/specs/2026-08-04-ywpt-workshop-pipeline-design.md)。

## 双轨概览

| 轨 | 说明 |
|----|------|
| **Track 0 课前金丝雀** | 环境、Prompt 卡、产物骨架、参考实现与达标报告样例；教练课前完成 |
| **Track 1 现场主线** | 学员持卡跑 P0–P6，维护 `compass.md`；约 180 分钟 |

金丝雀只读对照与超时续跑降级，**绝不替代**产品方人闸。

## 角色

| 角色 | 职责 |
|------|------|
| **学员 / 执行者** | 持 Prompt 卡跑主线；每段结束填写 `workshop/compass.md` |
| **产品方（真人）** | 每闸按 `prep/gate-scripts/P*.md` 提问；通过或打回 |
| **教练 / 金丝雀守护** | 课前 Track 0；现场仅在超时或故障时启用金丝雀对照 |

## 180 分钟时间盒（P0–P6）

| 分钟 | 阶段 | 主要产物 |
|------|------|----------|
| 0–15 | P0 存量扫描与基线 | `baseline/project-baseline.md` |
| 15–45 | P1 原型与 PRD | `product/order.html`, `product/PRD-order-checkout.md` |
| 45–75 | P2 OpenSpec 对抗收敛 | `openspec/changes/<change>/` |
| 75–120 | P3 编码与审查 | 业务代码改动 + review 要点 |
| 120–155 | P4 质量门禁 | `quality/quality-summary.md` |
| 155–170 | P5 启服走查 | `delivery/walkthrough-log.md` |
| 170–180 | P6 罗盘收口与课件 | `curriculum/` 回填 |

课前 Track 0 不计入上表 180 分钟。

## 课前准备

学员必装清单见 [curriculum/aecos_workshop_prep.md](curriculum/aecos_workshop_prep.md)；深化/冒烟见 [prep/env-setup.md](prep/env-setup.md)。

## 授课课件

| 物料 | 路径 |
|------|------|
| **总课件 v2（推荐）** | [curriculum/aecos_workshop_v2.md](curriculum/aecos_workshop_v2.md) |
| 总课件 v1 | [curriculum/aecos_workshop.md](curriculum/aecos_workshop.md) |
| **环境准备** | [curriculum/aecos_workshop_prep.md](curriculum/aecos_workshop_prep.md) |
| 六件套 | [curriculum/01](curriculum/01-讲师手册.md)–[06](curriculum/06-踩坑-playbook.md) |
| 参考示例风格 | [example/《AI Coding Workshop》课件 for PMSmart.md](example/《AI%20Coding%20Workshop》课件%20for%20PMSmart.md) |

## 如何填写 compass.md

`workshop/compass.md` 是现场轻量主账。每段（P0–P6）限时结束后**立即**填写，禁止事后补造人闸结论。

每段固定字段：

- **目标** — 本段要达成什么
- **Prompt 卡** — 使用的卡 ID（如 P0）
- **关键指令 / 工具摘要** — 主要 AI 指令或工具调用
- **产物路径** — 本段产出文件位置
- **人闸结论** — 通过 / 打回及原因、签字人
- **耗时（分钟）** — 实际用时
- **踩坑** — 遇到的问题与解法
- **degraded** — `true` / `false` 及原因（覆盖率未达标、环境故障等须诚实标注）

## Kit 完整性校验

开课前或提交 kit 变更后运行：

```bash
node workshop/scripts/validate-kit.mjs
```

预期输出：`validate-kit OK 30 files`（检查 REQUIRED 清单中全部文件存在，且 `compass.md` 含 P0–P6 段标记）。

## 目录速览

```text
workshop/
  README.md              ← 本文件
  compass.md             ← 现场主账
  example/               ← 参考课件风格（PMSmart）
  prep/                  ← 环境、Prompt 卡、人闸脚本、金丝雀
  baseline/              ← P0 基线
  product/               ← P1 原型与 PRD
  quality/               ← P4 质量报告
  delivery/              ← P5 走查记录
  curriculum/            ← 总课件 + 六件套（01–06）
  scripts/validate-kit.mjs
```

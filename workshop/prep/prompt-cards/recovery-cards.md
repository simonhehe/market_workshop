# 回收卡（人闸打回时使用）

## R-P0
缺口清单被打回：只重跑 GitNexus 相关分析，重写 `workshop/product/baseline/project-baseline.md`「缺口」及相关技术节，禁止改业务代码。

## R-P1-原型
原型打回：只改被点名的区块/交互/错误态文案；保持中等 MVP；禁止开始写 PRD 或改业务代码。

## R-P1-PRD
PRD 打回：只改被点名的状态机或 AC 行；保持中等 MVP，拒绝加需求；不重开无关原型大改（除非人闸点名原型不一致）。

## R-P2
OpenSpec 打回：打开 tasks.md，删除或降级越界任务；重新对齐 coverage-scope 最小集。

## R-P3
实现打回：只修 AC 失败项；禁止大范围重构；对照 canary。

## R-P4
质量打回：优先补白名单内失败用例；若时间不足，设 degraded 并写明缺口，不开「通过」闸。

## R-P5
走查打回：只修阻塞步骤对应代码；更新 walkthrough-log。

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

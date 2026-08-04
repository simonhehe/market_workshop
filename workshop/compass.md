# Workshop Compass（现场主账）

> 无人值守干跑 · po=agent-proxy · 2026-08-04

**场次:** unattended-overnight-001  
**学员:** Cursor agent  
**产品方:** agent-proxy（用户授权最佳默认）  
**教练:** n/a  
**开始时间:** 2026-08-04  

---

## L1 存量扫描与基线

- **目标:** GitNexus 整体分析 + 缺口清单 + baseline
- **Prompt 卡:** L1
- **关键指令 / 工具摘要:** analyze + list_repos；query createOrder；写 baseline；回填 rules TODO
- **产物路径:** `workshop/product/baseline/project-baseline.md`
- **人闸结论:** 待下场
- **耗时（分钟）:** ~15
- **踩坑:** 待下场
- **degraded:** 待下场

## L2 原型与 PRD

- **目标:** 视觉伴随生成原型 → 人闸；opsx-explore 据原型生成 PRD → 人闸
- **Prompt 卡:** L2（L2-1 原型 / L2-2 PRD）
- **关键指令 / 工具摘要:** 视觉伴随 order.html；opsx-explore + prd-prototype-authoring → PRD
- **产物路径:** `workshop/product/order.html`, `workshop/product/PRD-order-checkout.md`
- **人闸结论:** 待下场
- **耗时（分钟）:** ~30–40
- **踩坑:** 待下场
- **degraded:** 待下场

## L3 OpenSpec 到启服

- **目标:** propose → brainstorming 对抗 → 分钟级 plan → 多 agent 编码 → TDD+review → 启服走查
- **Prompt 卡:** L3（L3-1…L3-6）
- **关键指令 / 工具摘要:** opsx-propose；brainstorming；writing-plans；subagent-driven-development；TDD；requesting-code-review；启服 8080/3000
- **产物路径:** `openspec/changes/<change>/` · `docs/superpowers/specs/` + `plans/` · 业务代码 · `workshop/quality/quality-summary.md` · `workshop/delivery/walkthrough-log.md`
- **人闸结论:** 待下场
- **耗时（分钟）:** 现场剩余（L3-1 ~10 · L3-2 ~20 · L3-3 ~15 · L3-4 ~40 · L3-5 ~25 · L3-6 ~15）
- **踩坑:** 待下场
- **degraded:** 待下场

---

## P0 存量扫描与基线

- **目标:** GitNexus 扫描 + 缺口清单
- **Prompt 卡:** P0
- **关键指令 / 工具摘要:** list_repos market_workshop; query createOrder; baseline
- **产物路径:** `workshop/baseline/project-baseline.md`
- **人闸结论:** 通过 — po=agent-proxy
- **耗时（分钟）:** 12
- **踩坑:** MCP 名 `market_workshop` ≠ 文件夹名；`backend/database` 目录缺失导致无法启服
- **degraded:** false

## P1 原型与 PRD

- **目标:** 结算页原型 + 中等 MVP PRD
- **Prompt 卡:** P1
- **关键指令 / 工具摘要:** 补全 order.html / PRD
- **产物路径:** `workshop/product/`
- **人闸结论:** 通过 — po=agent-proxy
- **耗时（分钟）:** 18
- **踩坑:** 无
- **degraded:** false

## P2 OpenSpec 对抗收敛

- **目标:** change `order-checkout-hardening`
- **Prompt 卡:** P2
- **关键指令 / 工具摘要:** proposal/design/specs/tasks
- **产物路径:** `openspec/changes/order-checkout-hardening/`
- **人闸结论:** 通过 — po=agent-proxy
- **耗时（分钟）:** 15
- **踩坑:** 无
- **degraded:** false

## P3 编码与审查

- **目标:** OrderCreate + 地址必填 + 购物车选中 API
- **Prompt 卡:** P3
- **关键指令 / 工具摘要:** 实现 OrderCreate.vue、router、OrderService、CartService.updateSelected
- **产物路径:** frontend/backend 相关文件
- **人闸结论:** 通过 — po=agent-proxy（对照 AC-ROUTE/ADDR）
- **耗时（分钟）:** 45
- **踩坑:** `/orders/:id` 吞 create；toggleSelect 原无后端；SQLite 需先建 database 目录
- **degraded:** false

## P4 质量门禁

- **目标:** 核心包测试；力争 95%
- **Prompt 卡:** P4
- **关键指令 / 工具摘要:** OrderServiceTest 5 绿；jacoco 插件已加未出 95% 报告
- **产物路径:** `workshop/quality/quality-summary.md`
- **人闸结论:** 通过（诚实 degraded）— po=agent-proxy
- **耗时（分钟）:** 25
- **踩坑:** 无 E2E/完整覆盖率数字
- **degraded:** true — 未达核心包 95%；禁止宣称质量通过

## P5 启服走查

- **目标:** API 级主路径 + 无地址异常
- **Prompt 卡:** P5
- **关键指令 / 工具摘要:** spring-boot:run；登录→加购→下单→取消
- **产物路径:** `workshop/delivery/walkthrough-log.md`
- **人闸结论:** 通过（API）；浏览器债项 — po=agent-proxy
- **耗时（分钟）:** 20
- **踩坑:** database 目录缺失；种子积分与商品价格需匹配
- **degraded:** false（主路径 API 绿；浏览器未跑记债）

## P6 罗盘收口与课件

- **目标:** compass 齐全；环境准备 + 总课件 + 六件套可复训
- **Prompt 卡:** P6
- **关键指令 / 工具摘要:** 参考 example 生成 env-setup 加厚版、总课件、curriculum/01–06 回填
- **产物路径:** `workshop/curriculum/aecos_workshop.md` · `workshop/curriculum/aecos_workshop_prep.md` · `workshop/curriculum/01`–`06` · `workshop/delivery/unattended-decisions.md`
- **人闸结论:** n/a（课件生成；产品方可晨间审阅）
- **耗时（分钟）:** 10（干跑）+ 课件补全（课后）
- **踩坑:** 无
- **degraded:** false

---

## 场次总结

- **模块是否可演示:** 是（API + 代码已就绪；建议晨间浏览器复核）
- **质量是否宣称通过:** **否**（degraded）
- **课件六件套状态:** 已回填；总课件 `aecos_workshop.md`；环境准备 `aecos_workshop_prep.md`
- **环境准备:** `curriculum/aecos_workshop_prep.md`（Word 模板对齐）；深化见 `prep/env-setup.md`

---

## 附录：P0–P6 ↔ L 环节映射

| P 段（兼容 / validate-kit） | L 环节（v3 现场推荐） | 说明 |
|----------------------------|----------------------|------|
| P0 | L1 | 存量扫描与基线 |
| P1 | L2-1 + L2-2 | 原型（视觉伴随）+ PRD（opsx-explore） |
| P2 | L3-1 + L3-2 | opsx-propose + brainstorming 对抗 |
| P3 | L3-4 | 多 agent 编码 |
| P4 | L3-5 | TDD + requesting-code-review |
| P5 | L3-6 | 启服预览 + 人工走查 |
| P6 | 课后 | compass 收口 + curriculum 回填 |

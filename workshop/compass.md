# Workshop Compass（现场主账）

> 无人值守干跑 · po=agent-proxy · 2026-08-04

**场次:** unattended-overnight-001  
**学员:** Cursor agent  
**产品方:** agent-proxy（用户授权最佳默认）  
**教练:** n/a  
**开始时间:** 2026-08-04  

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

- **目标:** compass 齐全；踩坑回填
- **Prompt 卡:** P6
- **关键指令 / 工具摘要:** 更新 curriculum/06；决策日志
- **产物路径:** `workshop/curriculum/` `workshop/delivery/unattended-decisions.md`
- **人闸结论:** n/a
- **耗时（分钟）:** 10
- **踩坑:** 无
- **degraded:** false

---

## 场次总结

- **模块是否可演示:** 是（API + 代码已就绪；建议晨间浏览器复核）
- **质量是否宣称通过:** **否**（degraded）
- **课件六件套状态:** 壳完整；06 已回填本场踩坑；其余可课后润色

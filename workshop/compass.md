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
- **关键指令 / 工具摘要:** list_repos market_workshop; query createOrder/cart; 写 baseline
- **产物路径:** `workshop/baseline/project-baseline.md`
- **人闸结论:** 通过 — po=agent-proxy；缺口 G1–G5 可指导 PRD；无支付/优惠券膨胀
- **耗时（分钟）:** 12
- **踩坑:** MCP 名 `market_workshop` ≠ 文件夹名
- **degraded:** false

## P1 原型与 PRD

- **目标:** 结算页原型 + 中等 MVP PRD
- **Prompt 卡:** P1
- **关键指令 / 工具摘要:** 补全 order.html 与 PRD-order-checkout.md
- **产物路径:** `workshop/product/order.html`, `workshop/product/PRD-order-checkout.md`
- **人闸结论:** 通过 — po=agent-proxy；AC 覆盖 CREATE/STOCK/POINTS/ADDR/AUTH/CANCEL/ROUTE
- **耗时（分钟）:** 18
- **踩坑:** 无
- **degraded:** false

## P2 OpenSpec 对抗收敛

- **目标:** 可控 change `order-checkout-hardening`
- **Prompt 卡:** P2
- **关键指令 / 工具摘要:** 写 proposal/design/specs/tasks；对抗：无支付偷渡；状态 pending 唯一创建态；地址必填；鉴权走现有过滤器；测试可测
- **产物路径:** `openspec/changes/order-checkout-hardening/`
- **人闸结论:** 通过 — po=agent-proxy；tasks 边界=中等 MVP
- **耗时（分钟）:** 15
- **踩坑:** 无
- **degraded:** false

## P3 编码与审查

- **目标:** OrderCreate 页 + 地址必填 + 路由顺序
- **Prompt 卡:** P3
- **关键指令 / 工具摘要:** 后台子代理实现中（见 unattended log）
- **产物路径:** OrderCreate.vue / router / OrderService
- **人闸结论:** 待实现完成后补签
- **耗时（分钟）:** 
- **踩坑:** `/orders/:id` 会吞掉 `create` — 必须先注册 create 路由
- **degraded:** false

## P4 质量门禁

- **目标:** 核心包测试；力争 95%
- **Prompt 卡:** P4
- **关键指令 / 工具摘要:** OrderServiceTest + quality-summary
- **产物路径:** `workshop/quality/`
- **人闸结论:** 待测
- **耗时（分钟）:**
- **踩坑:**
- **degraded:** （测后填写）

## P5 启服走查

- **目标:** 启服或 API 级走查
- **Prompt 卡:** P5
- **关键指令 / 工具摘要:**
- **产物路径:** `workshop/delivery/walkthrough-log.md`
- **人闸结论:** 待办
- **耗时（分钟）:**
- **踩坑:**
- **degraded:**

## P6 罗盘收口与课件

- **目标:** compass 齐全；curriculum 回填要点
- **Prompt 卡:** P6
- **关键指令 / 工具摘要:**
- **产物路径:** `workshop/curriculum/`
- **人闸结论:** n/a（教练勾选完整度）
- **耗时（分钟）:**
- **踩坑:**
- **degraded:**

---

## 场次总结

- **模块是否可演示:** （P3 完成后更新）
- **质量是否宣称通过:** （P4 后更新；degraded 则否）
- **课件六件套状态:** 壳已有；本场笔记回填中

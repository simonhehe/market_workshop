# 教练干跑检查（Kit 级，≤60min）

- [x] `node workshop/scripts/validate-kit.mjs` → OK  
- [x] env-setup 冒烟：backend + frontend 能起（compile-level：`mvn package` + `npm run build`；端口 8080/3000 已核对；未起常驻服务）  
- [x] GitNexus `list_repos` 含本仓（`market_workshop`；已 re-analyze）  
- [x] 朗读 P0 Prompt + 走一遍 gate P0 问题（无人值守：agent 自问自答，见 baseline + unattended-decisions）  
- [x] 打开 order.html / PRD 骨架，确认 P1 可开写（已补全 v0.2）  
- [x] 确认 coverage-scope 与现有 `OrderService#createOrder` 对齐（类存在；缺口 G1/G2 已立项）  
- [x] 计时：P0→P2 桌面演练节奏 — **agent 评估可 ≤75min**（预制卡齐全；真人闸另计）  

**完整 3h 业务干跑（含编码）** 已在无人值守会话启动：见 `workshop/delivery/unattended-decisions.md` 与 `workshop/compass.md`。

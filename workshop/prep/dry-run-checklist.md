# 教练干跑检查（Kit 级，≤60min）

- [x] `node workshop/scripts/validate-kit.mjs` → OK  
- [x] env-setup 冒烟：backend + frontend 能起（compile-level：`mvn package` + `npm run build`；端口 8080/3000 已核对；未起常驻服务）  
- [x] GitNexus `list_repos` 含本仓（`market_workshop`，945 nodes；index 落后 HEAD 1 commit）  
- [ ] 朗读 P0 Prompt + 走一遍 gate P0 问题（可自问自答）  
- [ ] 打开 order.html / PRD 骨架，确认 P1 可开写  
- [ ] 确认 coverage-scope 与现有 `OrderService#createOrder` 对齐  
- [ ] 计时：从 P0 卡到 P2 卡桌面演练是否 ≤75min 节奏感  

**完整 3h 业务干跑（含编码）** 另开执行会话，使用 subagent-driven-development / executing-plans，以本 checklist 通过为前提。

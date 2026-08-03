# P0 · GitNexus 查询剧本

在 Cursor 中对**本仓**依次执行（MCP 或自然语言均可）：

1. `list_repos` — 确认当前索引是本仓：MCP 名多为 `market_workshop`，路径应指向本目录（文件夹名可能是 `ywpt_workshop_test`），不是无关仓。  
2. `query`：`order checkout createOrder cart points stock`  
   - goal: find order creation and cart-to-order flow  
3. `context`：`OrderService`  
4. `context`：`CartService`  
5. `route_map` 或 `query`：`/orders`  
6. `impact`：`OrderService` direction=`upstream` — 记录谁依赖结算  

将结论写入 `workshop/baseline/project-baseline.md`：

- 技术栈表  
- 业务模块列表  
- 下单结算缺口（半成品：已有 createOrder，缺什么状态/异常/前端闭环）  
- 建议的 OpenSpec change 名（如 `order-checkout-hardening`）  


Last indexed: 2026-08-04 — 960 nodes, 1,589 edges; gitnexus status up-to-date (market_workshop).
# 踩坑 Playbook

| 症状 | 原因 | 处理 | 来源场次 |
|------|------|------|----------|
| GitNexus list_repos 名对不上文件夹 | MCP 索引名为 `market_workshop` | P0 剧本写明路径匹配即可 | unattended-001 |
| spring-boot 起不来 SQLite path does not exist | 缺少 `backend/database/` 目录 | mkdir；写入 env-setup | unattended-001 |
| 打开 `/orders/create` 进详情或 404 | 路由 `/orders/:id` 抢先匹配 | create 路由注册在 `:id` 之前 | unattended-001 |
| 购物车勾选无效果 | `toggleSelect` 无后端 | PUT `/cart/{id}` 支持 `isSelected` | unattended-001 |
| 下单报积分不足 | 种子用户积分 < 商品积分 | user1 提到 10000；或选用低积分商品 | unattended-001 |
| 无地址错误被积分错误掩盖 | 地址校验顺序靠后 | `addressId==null` 提到 createOrder 最前 | unattended-001 |
| 覆盖率未达 95% | 一夜之间只加了 Service UT | 诚实 degraded；课后补 JaCoCo 报告与 E2E | unattended-001 |

# Walkthrough Log — P5（无人值守 API 级）

**时间:** 2026-08-04  
**方式:** 启 backend + PowerShell Invoke-RestMethod（未做浏览器点选；前端 OrderCreate 已落地）  
**账号:** user1 / password1  

## 步骤

| # | 步骤 | 结果 |
|---|------|------|
| 1 | 登录 | PASS |
| 2 | 无 addressId 创建订单 | PASS — `请先选择收货地址` (400) |
| 3 | 加购 productId=7（100 积分）isSelected=1 | PASS |
| 4 | 带 addressId 下单 | PASS — status=pending, totalPoints=100 |
| 5 | 取消订单 | PASS — status=cancelled |
| 6 | 购物车选中 API | PASS — PUT `/cart/{id}` 支持 isSelected |

## 阻塞债项

- [ ] 真人浏览器走查 OrderCreate.vue（agent 未开 frontend 常驻）
- [ ] 积分不足 / 库存不足 需另造数据再验一遍 UI

## 人闸

通过（po=agent-proxy）— 主路径 + 无地址异常已 API 验证；浏览器项记债。

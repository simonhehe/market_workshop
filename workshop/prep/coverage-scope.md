# 下单结算核心包覆盖率 Scope（冻结）

**硬门禁:** 行覆盖率 ≥ 95% 且 分支覆盖率 ≥ 95%（仅本白名单分母）。  
**非目标:** 管理端、商品浏览、收藏、非结算 User API 等不计入分母。

## 后端（JaCoCo include）

| 类型 | 路径 / 类 |
|------|-----------|
| Service | `com.example.mall.service.OrderService` |
| Service | `com.example.mall.service.CartService`（仅与下单选中项协作的方法；整类计入以简化工具配置） |
| Controller | `com.example.mall.controller.OrderController` |
| Controller | `com.example.mall.controller.CartController` |

> 若 P0 缺口分析证明结算逻辑落在其他类，**先改本文件并人闸**，再改 `jacoco-include.txt`。

## 前端（后续 Istanbul/Vitest include，本场可先人工审查）

| 模块 | 路径 |
|------|------|
| API | `frontend/src/api/order.js` |
| API | `frontend/src/api/cart.js` |
| View | `frontend/src/views/Cart.vue`（结算入口） |
| View | `frontend/src/views/Orders.vue` |
| View | `frontend/src/views/OrderDetail.vue` |

## 必须绿的最小用例（与 spec §4 对齐）

1. UT: 下单成功  
2. UT: 库存不足  
3. UT: 积分不足  
4. UT: 地址缺失 / 无效  
5. API: POST `/api/orders` 契约  
6. API: 未鉴权失败  
7. 集成: 扣库存 + 扣积分一致性  
8. E2E: 购物车→下单→订单列表  
9. E2E: 取消或异常路径一条  

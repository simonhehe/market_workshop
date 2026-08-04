# 结算链路 Coverage 白名单（L3 门禁）

> 硬门禁：**行覆盖率 ≥ 95%** 且 **分支覆盖率 ≥ 95%**（仅计本表分母）。  
> 非目标（不计入分母）：管理端、商品浏览、收藏、Profile/Favorites 及非结算 User API。

## 后端（JaCoCo class 级）

| 类 | 说明 |
|---|---|
| `com.example.mall.service.OrderService` | 下单 / 取消 / 查询 |
| `com.example.mall.service.CartService` | 购物车 + 勾选 |
| `com.example.mall.service.AddressService` | 结算页地址 list/create（工作坊 mock 同步） |
| `com.example.mall.controller.OrderController` | `/orders` |
| `com.example.mall.controller.CartController` | `/cart` |
| `com.example.mall.controller.AddressController` | `/addresses` |

## 前端（Vitest v8）

| 路径 | 说明 |
|---|---|
| `frontend/src/mocks/checkoutAddresses.js` | Mock 地址场景 |
| `frontend/src/checkout/**` | 结算纯函数（从 Checkout 提取） |
| `frontend/src/views/Checkout.vue` | 结算页组件 |

## E2E（Playwright，不计覆盖率分母）

| 场景 | 映射 AC |
|---|---|
| 购物车 → 结算 → 选地址 → 提交 → 详情 | T1 / T10 |
| 【空】地址 mock → 提交失败 banner+modal | 空地址 AC |
| 步骤① 返回购物车 | 回退链接 |

## 验证命令

```bash
# 后端 API + 覆盖率
cd backend && mvn test jacoco:report

# 前端 UT + 覆盖率
cd frontend && npm run test:coverage

# E2E（需 backend:8080 + frontend:3000/3001）
cd frontend && npm run e2e

# 汇总报告
node scripts/generate-checkout-test-report.mjs
```

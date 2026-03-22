# Smoke：用户结算、订单筛选、购物车选中（本变更）

> **目的**：最短路径验证「勾选 → 结算页 → 下单 → 订单列表筛选」与关键 API。  
> **前置**：后端 `http://localhost:8080/api`，前端 `npm run dev`（默认代理 `/api`）；测试账号 `user1` / `password1`。

---

## A. API（curl / Postman）

1. **健康检查**  
   `GET /api/health` → 200，JSON `success`。

2. **登录**  
   `POST /api/auth/login`  
   `{"username":"user1","password":"password1"}`  
   → 记录响应中的 `data.token`（或项目实际字段名）。

3. **加购（若购物车空）**  
   `POST /api/cart`  
   Header：`Authorization: Bearer <token>`  
   Body：`{"productId":1,"quantity":1}`（`productId` 以库里存在且上架的商品为准）

4. **列出购物车**  
   `GET /api/cart`（带 Bearer）→ 确认有 `cartItems`，记下一条 `id`。

5. **勾选**（推荐，避免部分环境对多段路径 PUT 返回 404）  
   `POST /api/cart/selection`  
   Body：`{"cartItemId":<购物车行id>,"isSelected":1}`  
   → 200，`summary.selectedCount` ≥ 1。  
   兼容旧路径：`PUT /api/cart/{id}/selection`，Body：`{"isSelected":1}`。

6. **订单列表筛选**  
   `GET /api/orders?status=pending`（带 Bearer）→ 200，列表为 pending（可为空）。

7. **下单**  
   `POST /api/orders`  
   Body：`{}` 或 `{"remark":"smoke"}`（可选 `"addressId"`，须为**当前用户**的地址 id）  
   → 200，返回订单 `id`。若积分不足或未选中商品，应 400 且业务错误信息明确。

8. **负例（可选）**  
   使用**其他用户地址 id**（若可构造）→ 应 400，提示含「无权使用该收货地址」类文案。

---

## B. UI（人工 Smoke）

1. 登录 → **购物车**：勾选至少一行 → 点 **去结算** → 进入 **`/checkout`**。  
2. **确认订单页**：可见选中商品与合计积分；提交订单 → 跳转 **订单详情** `/orders/:id`。  
3. **我的订单**：切换 **状态筛选**（如「待处理」）→ 列表随筛选变化（或空态合理）。  
4. 购物车行 **取消勾选** → 再点结算应提示或结算页提示无选中商品。

---

## C. 自动化回归（开发机）

**白盒（JUnit）**：源码在 **`test/backend/java`**，在 `backend` 目录执行：

```bash
cd backend
mvn -q test
```

**API 冒烟脚本（测试团队）**：`test/smoke/smoke-checkout-api.ps1`（见 `test/smoke/README.md`）。

```bash
cd frontend
npm run build
```

# 订单链路：UT 与 API 测试说明

> **区分**：本文档区分**单元测试（UT）**与 **API / 集成测试**；当前仓库**已实现 UT 代码**（见下表），**未**提交需拉起完整应用的 API 级自动化用例。  
> **依据**：`OrderService`、`AdminService`（订单状态）、`UserService.updatePoints`、`OrderController`、`AdminController`（订单相关）、`AuthMiddleware`。  
> **环境**：后端 Java 17、Spring Boot 3.2.0（见 `backend/pom.xml`）；默认数据源为 SQLite 文件（见 `backend/src/main/resources/application.yml`）。

---

## 1. UT 与 API 测试的定义（本仓库语境）

| 类型 | 典型技术 | 是否启动完整 Spring 容器 | 是否走 `AuthMiddleware` / 真实 JWT | 本订单链路覆盖目标 |
|------|-----------|---------------------------|--------------------------------------|---------------------|
| **UT（单元 / 切片）** | Mockito；`@WebMvcTest` + `addFilters = false` + `requestAttr` | Controller 切片启动轻量容器；Service 可无容器 | **否**（控制器层用手动注入 `userId` / `role` 模拟已通过鉴权） | 分支逻辑、错误码映射、admin `role` 分支 |
| **API 测试（接口 / 集成）** | `@SpringBootTest` + `@AutoConfigureMockMvc`；或 TestRestTemplate | **是** | **是**（须带 `Authorization: Bearer ...` 或调整测试配置） | 401、真实路径前缀、与数据库事务端到端一致性 |

---

## 2. 已实现的 UT 清单（订单相关）

| 测试类 | 路径 | 覆盖重点 |
|--------|------|----------|
| `OrderServiceTest` | `backend/src/test/java/com/example/mall/service/OrderServiceTest.java` | 创建 / 取消（同上）；**确认收货**；**列表筛选**（无参、合法 `status`、非法、trim） |
| `AdminServiceTest` | 同上包 `AdminServiceTest.java` | `updateOrderStatus`：订单不存在；**无状态机**任意状态落库（含 `cancelled`） |
| `UserServiceTest` | `UserServiceTest.java` | `updatePoints`：用户不存在、成功累加（订单扣/退会间接触发） |
| `OrderControllerTest` | 同上 | `POST /orders`；`GET /orders` 无参、带 `status`、`ORDER_004`；`GET /orders/{id}` **不存在**与归属；`PUT .../cancel`；**`PUT .../confirm-receipt`** 与 `ORDER_003` |
| `AdminControllerOrderTest` | `AdminControllerOrderTest.java` | `GET/PUT /admin/orders...` **非 admin → 403 AUTH_004**；admin 成功调用 `AdminService` |

**说明**：WebMvc 类测试使用 `addFilters = false`，避免依赖 JWT；与生产相比，**不验证**「无 Token → 401」——该部分归入 **API 测试**。

**本地运行**（需本机已配置 Maven）：

```bash
cd backend
mvn test
```

---

## 3. API 测试建议（待实现，高价值场景）

以下适合 `@SpringBootTest` + `context-path=/api`（与 `application.yml` 一致）+ 签发测试用 JWT（`JwtUtil`）：

- **鉴权**：未带 Token / 非法 Token 访问 `POST /api/orders` → 401（`AuthMiddleware`）。
- **订单接口**：带用户 Token 创建订单、取消订单；断言 HTTP 状态与 JSON 与 UT 一致且数据库侧积分/库存变化（可使用单独测试库或事务回滚）。
- **管理员接口**：`role=admin` 与 `role=user` 各一号，验证 `PUT /api/admin/orders/{id}/status` 的 403 / 200。
- **状态与用户取消差异**：管理员将状态改为 `cancelled` 后，断言**未**发生用户取消路径的退款与还库存（与 `doc/baseline/order-domain-baseline.md` 一致）。

---

## 4. 与风险控制点的对应关系

| 风险点 | UT 是否覆盖 | API 测试建议 |
|--------|-------------|----------------|
| 创建订单各前置校验 | `OrderServiceTest` | 可选：并发/重复提交 |
| 取消仅 `pending` | `OrderServiceTest` + `OrderControllerTest`（错误消息出口） | 端到端再跑一遍 |
| 详情 / 取消归属 | `OrderServiceTest` + `OrderControllerTest` | Token 换用户重放 |
| Admin `role` | `AdminControllerOrderTest` | 真实 JWT role |
| Admin 改状态无状态机、与用户取消不对称 | `AdminServiceTest` | DB 断言无退款流水 |

---

## 5. 相关文档

- `../baseline/order-domain-baseline.md`
- `./订单链路基线分析.md`
- `./单链路基线分析_技术.md`

---

*原 `白盒测试候选矩阵.md` 已删除；本文件替代其「规划 + 落地」角色：UT 已在 `backend/src/test` 中对应实现。*

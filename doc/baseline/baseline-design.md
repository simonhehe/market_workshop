# 基线设计说明（Baseline Design）

> 描述当前仓库的**逻辑架构、技术选型、关键机制与数据概览**，作为设计与变更的参照基线。仅含仓库可见事实；未证实项为 **N/A**。

---

## 1. 系统上下文

```text
[ Browser / Client ]
        |  HTTP(S)
        v
[ Vue 3 SPA : Vite dev :3000 ] --proxy /api--> [ Spring Boot :8080 /api ]
                                                      |
                                                      v
                                               [ SQLite file mall.db ]
```

- 生产部署与网关：**N/A**（仓库未定义）。

---

## 2. 逻辑架构（后端）

| 分层 | 包 / 职责 |
|------|-----------|
| 接入层 | `controller`：REST，统一 `ApiResponse` |
| 领域与服务 | `service`：业务规则、事务边界 |
| 持久化 | `repository`：Spring Data JPA |
| 模型 | `entity`：JPA 实体与表映射 |
| 横切 | `middleware`：`AuthMiddleware`（JWT）；`config`：CORS、Filter 注册、数据初始化等 |
| 工具 | `util`（如 `JwtUtil`）、`common` |

**未引入**完整 Spring Security Filter Chain；认证依赖自定义 `OncePerRequestFilter`。

---

## 3. 前端结构（概要）

| 项 | 说明 |
|----|------|
| 入口 | `frontend/src/main.js`，路由 `src/router/index.js` |
| 页面 | `src/views`（含 `admin/`） |
| API | `src/api/*.js`，`axios` 实例 `baseURL: '/api'`，请求拦截附加 Bearer |
| 开发代理 | `vite.config.js`：`/api` → `http://localhost:8080` |

---

## 4. 技术栈基线

### 4.1 后端

- Java **17**，Spring Boot **3.2.0**，Maven。
- Web、Data JPA、Validation；SQLite JDBC + Hibernate SQLite 方言。
- JWT（jjwt **0.12.3**）；`BCryptPasswordEncoder`（`spring-security-crypto`）。

### 4.2 前端

- **Vue 3**，**Vite 5**，**vue-router 4**，**axios**。

### 4.3 数据与迁移

- 主存储：**SQLite**（`jdbc:sqlite:./database/mall.db`）。
- 存在 `src/main/resources/db/migration/V1__init.sql`；**`pom.xml` 未引入 Flyway/Liquibase**，以 JPA `ddl-auto` 为主。

---

## 5. API 与跨域

- 全局上下文路径：`server.servlet.context-path=/api`。
- CORS：`CorsFilter` Bean，开发向配置见 `CorsConfig`（允许范围较宽，生产需收紧）。

---

## 6. 认证与授权设计

| 机制 | 说明 |
|------|------|
| 登录 | `POST /api/auth/login`，返回 Token 等 |
| 请求 | `Authorization: Bearer <token>` |
| 校验 | `JwtUtil` 校验签名与过期；`AuthMiddleware` 对非白名单路径要求有效 Token |
| 白名单（路径经 `/api` 剥离后） | `/`、`/health`、`/auth/login`、`/products`、`/categories` 及其子路径 |
| 管理员 | `AdminController` 各方法内判断 `role` |

JWT 声明（代码中）：`userId`、`username`、`role`。

---

## 7. 数据模型概览（逻辑）

| 实体 | 表名（实体标注） | 关系要点 |
|------|------------------|----------|
| User | `users` | 积分、角色、软删 |
| Category | `categories` | 分类 |
| Product | `products` | 归属分类、积分价、库存、软删 |
| Cart | `cart` | 用户+商品唯一；`is_selected` |
| Address | `addresses` | 归属用户 |
| Order | `orders` | 归属用户；可选 `address_id`；收件快照 |
| OrderItem | `order_items` | 归属订单；商品信息快照 |
| PointsHistory | `points_history` | 归属用户；关联业务类型与 `related_id` |
| Favorite | `favorites` | 用户+商品唯一 |

**ER 细节**：见 [单链路基线分析_技术.md](../analysis/单链路基线分析_技术.md) §1。

---

## 8. 启动与配置要点

| 配置项 | 值（基线） |
|--------|------------|
| 后端端口 | 8080 |
| 前端开发端口 | 3000 |
| JPA | `ddl-auto: update`，`show-sql: true`（开发向） |
| JWT 过期 | `86400000` ms（配置注释为 24h） |

`HealthController` 中 `/test` 返回体曾出现与 **Java 版本** 不一致描述，以 **`pom.xml` Java 17** 为准。

---

## 9. 质量与设计风险（基线记录）

- **测试**：`backend/src/test` 下未见测试类（以仓库为准）。
- **安全**：JWT 密钥在 `application.yml`；购物车部分接口未在控制器层绑定 `userId` 与资源归属（见技术附录）。
- **并发**：库存与积分为应用层读改写；SQLite 单写模型；高并发场景需另行设计。
- **前端订单**：结算路由与下单 API 调用未形成闭环（见订单域基线）。

---

## 10. 相关文档

| 文档 | 内容 |
|------|------|
| [openspec/config.yaml](../../openspec/config.yaml) | 结构化上下文 |
| [单链路基线分析_技术.md](../analysis/单链路基线分析_技术.md) | 接口契约、时序、数据字典、安全条目 |
| [测试基线建议.md](../analysis/测试基线建议.md) | smoke / 白盒 / CI 建议 |

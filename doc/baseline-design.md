# 基线设计（Baseline Design）

> **定位**：当前仓库可见的**架构与技术设计基线**（单体、分层、集成方式），供变更时评估影响面。  
> **不包含**未在仓库出现的部署拓扑与中间件选型（标 N/A）。

**关联文档**：`baseline-requirements.md`、`order-domain-baseline.md`、`openspec/config.yaml`（机器可读摘要）。

---

## 1. 系统上下文

```text
[Browser] --HTTP--> [Vite dev :3000] --proxy /api--> [Spring Boot :8080/context-path=/api]
                                                          |
                                                          v
                                                    [SQLite mall.db]
```

- **生产构建**：前端静态资源与后端分离部署方式 **N/A**（未文档化）。

---

## 2. 逻辑架构

| 层 | 后端（`com.example.mall`） | 前端（`frontend/src`） |
|----|---------------------------|-------------------------|
| 接入 | `controller`（REST）、`AuthMiddleware`（Filter） | `views`、`router`、`api/*.js`、Axios 实例 |
| 应用 | `service` | 视图逻辑（Options API 为主） |
| 领域/数据 | `entity`、`repository` | N/A（无独立领域层） |
| 横切 | `config`（CORS、JWT Bean、Filter 注册）、`util/JwtUtil` | 路由守卫 `meta.requiresAuth` / `requiresAdmin` |

- **API 风格**：JSON，`ApiResponse<T>` 统一封装（成功/失败结构见 `common/ApiResponse.java`）。
- **版本控制**：无 `/v1` 前缀 **N/A**。

---

## 3. 认证与授权

| 项 | 设计 |
|----|------|
| 协议 | HTTP Header `Authorization: Bearer <JWT>` |
| 生成与校验 | `JwtUtil` + `application.yml` 中 `jwt.secret`、`jwt.expiration` |
| 全局过滤器 | `AuthMiddleware` 注册于 `/*`，先剥离 `context-path` 再匹配路径 |
| 白名单（无 JWT） | `/`、`/health`、`/auth/login`、`/products`、`/categories`（含子路径匹配规则见源码） |
| 鉴权后 | `request` 属性：`userId`、`username`、`role` |
| 管理员 | 各 `/admin/*` 接口内判断 `role.equals("admin")`，否则 403 + `AUTH_004` |

**已知设计债**：白名单使商品/分类可读与「仅登录可浏览」的前端策略不一致；购物车部分接口未校验资源归属（见 `单链路基线分析_技术`）。

---

## 4. 数据与持久化

| 项 | 设计 |
|----|------|
| 数据库 | SQLite 文件 `jdbc:sqlite:./database/mall.db`（相对后端工作目录） |
| ORM | Spring Data JPA + Hibernate |
| 模式演进 | `ddl-auto: update`；**无** Flyway/Liquibase（`pom` 未引入） |
| 初始化 | `DataInitializer`：`CommandLineRunner`，表空时灌入演示用户/分类/商品 |
| 软删除 | 多实体含 `deleted_at`；查询是否过滤依各 Repository 方法而定 |

**表关系**：实体间多用 `Long` 外键字段，**非** JPA `@ManyToOne` 对象导航为主（见 `单链路基线分析_技术` ER 图）。

---

## 5. 模块边界（后端包）

| 包 | 职责 |
|----|------|
| `controller` | HTTP 映射、从 RequestAttribute 取用户、组装响应 |
| `service` | 事务与业务规则 |
| `repository` | JPA 接口 |
| `middleware` | JWT 过滤器 |
| `config` | CORS、Filter、PasswordEncoder、WebMvc、DataInitializer |

**事件 / 消息 / 缓存**：N/A（未使用）。

---

## 6. 前端架构要点

| 项 | 设计 |
|----|------|
| 入口 | `main.js`、Vue Router `createWebHistory` |
| API | `api/index.js`：`baseURL: '/api'`，请求附加 Bearer，响应拦截返回 `response.data` |
| 路由守卫 | token 与 `userInfo.role` 控制 `requiresAuth`、`requiresAdmin` |

**状态管理**：未引入 Pinia/Vuex（依赖 `localStorage` 等）— **N/A** 是否长期策略。

---

## 7. 质量与演进约束（设计侧）

- **测试**：依赖已声明，自动化测试目录为空 → 重大重构前建议先补基线测试（`测试基线建议.md`）。
- **配置密钥**：禁止在生产复用当前 YAML 密钥设计。
- **OpenSpec**：变更提案见 `openspec/README.md`；与 `spec/AI2AI` 双层文档关系以团队规范为准（当前 `spec/` 可能为空）。

---

## 8. 修订记录

| 版本 | 日期 | 说明 |
|------|------|------|
| 0.1 | N/A | 初稿，自代码与 doc 归纳 |

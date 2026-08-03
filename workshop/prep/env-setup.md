# Track 0 · 环境预装（课前）

## 必备

- JDK 21+
- Maven 3.9+
- Node.js 18+（建议 20 LTS）
- Git

## 仓库依赖

```bash
# 后端
cd backend
mvn -q -DskipTests package

# 前端
cd ../frontend
npm install
```

## 端口

| 服务 | 端口 | 说明 |
|------|------|------|
| Backend | `8080` | `context-path=/api` → `http://localhost:8080/api` |
| Frontend | `3000` | 见 `frontend/vite.config.js`（非 Vite 默认 5173） |

## 数据库

- SQLite 文件：`backend/database/mall.db`（首次启动由应用初始化）
- 勿提交本地脏库到课件主分支

## GitNexus（本仓）

当前 MCP 可能仍索引其他仓。课前必须对本仓建索引：

```bash
# 在仓库根目录
npx gitnexus analyze
# 或已安装全局：gitnexus analyze
node .gitnexus/run.cjs status
```

用 MCP `list_repos` 确认出现本仓库名（通常为 `market_workshop`，路径指向本目录）后再开课。

## 冒烟

```bash
# 终端 1
cd backend && mvn spring-boot:run

# 终端 2
cd frontend && npm run dev
```

浏览器打开 `http://localhost:3000`，登录种子账号（见 `DataInitializer`），确认购物车页可打开。

## Kit 校验

```bash
node workshop/scripts/validate-kit.mjs
```

Expected: `validate-kit OK`（全部物料提交后）。

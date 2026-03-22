## Why

收藏夹目前仅支持逐条「取消收藏」，当用户积累较多条目时，清理或管理效率低。需要在**不破坏现有单条操作**的前提下，提供**批量选中**与**批量移除**，与购物车多选体验对齐，降低操作成本。

## What Changes

- **前端 `Favorites.vue`**：每条收藏增加可选中态（checkbox）；工具栏提供「全选/取消全选」「批量取消收藏」；空选时禁用批量操作或提示。
- **后端**：新增批量删除收藏接口（推荐 `POST /favorites/batch-remove`，body 为收藏记录 id 列表），在 **Service 层**校验所有 `favoriteId` 属于当前 `userId`，再逐条或批量执行与现 `removeFavorite` 一致的副作用（商品 `favorites` 计数、删除记录）。
- **前端 API**：`favorite.js` 增加 `batchRemoveFavorite(ids)` 调用新接口。
- **非目标（本变更）**：不要求批量加入收藏、不要求后端事务外的新表结构；若列表过大，可先限制单次批量上限（如 100 条）并在设计/任务中说明。

## Capabilities

### New Capabilities

- `favorite-batch-remove`: 当前用户批量移除收藏记录（API 契约 + 权限与数据一致性约束）。

### Modified Capabilities

- （无）仓库 `openspec/specs/` 下暂无既有能力 spec；本变更以新建 `favorite-batch-remove` 能力描述为主，不依赖 delta。

## Impact

- **后端**：`FavoriteController`、`FavoriteService`、`FavoriteRepository`（如需 `deleteByIdInAndUserId` 等查询）；可选 `FavoriteControllerTest` / `FavoriteServiceTest`（`test/backend/java`）。
- **前端**：`Favorites.vue`、`frontend/src/api/favorite.js`；样式与无障碍（checkbox 标签）。
- **协议**：新增 `POST /favorites/batch-remove`（相对 `/api` 前缀由网关/上下文路径决定）。
- **数据库**：无表结构变更（沿用 `favorites` 表）。

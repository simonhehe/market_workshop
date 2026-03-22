# Tasks: 收藏夹批量选中 / 批量移除

> 依赖：`design.md`、`specs/favorite-batch-remove/spec.md`。

## 1. 后端 API 与领域逻辑

- [ ] **1.1** `FavoriteService`：实现 `batchRemoveFavorites(userId, List<Long> favoriteIds)`（或等价命名）；校验非空、数量 ≤ 上限（如 100）；对每个 id 校验存在且 `userId` 匹配，否则整单失败；删除逻辑与单条 `removeFavorite` 一致（商品收藏数、物理删）。
- [ ] **1.2** `FavoriteController`：新增 `POST /favorites/batch-remove`，body：`{ "favoriteIds": [ ... ] }`，JWT 下使用 `@RequestAttribute userId`；错误返回与现有 `ApiResponse` 风格一致。
- [ ] **1.3**（可选）`FavoriteRepository`：若需批量查询辅助方法，补充 `findByIdInAndUserId` 等并配合 1.1。

## 2. 前端 API

- [ ] **2.1** `frontend/src/api/favorite.js`：新增 `batchRemoveFavorites(favoriteIds)` → `POST /favorites/batch-remove`。

## 3. 收藏夹页面

- [ ] **3.1** `Favorites.vue`：每条展示 checkbox；维护 `selectedIds`；提供「全选/取消全选」「批量取消收藏」；调用 2.1 成功后 `loadFavorites()` 并清空选中；空列表/未选中时禁用或提示。
- [ ] **3.2** 基础样式与可点击区域（label 关联 checkbox），避免误触。

## 4. 测试与文档

- [ ] **4.1** `test/backend/java`：补充 `FavoriteServiceTest` / `FavoriteControllerTest`（`@WebMvcTest` + `@MockBean JwtUtil`）覆盖成功、空列表、含非法 id、超限。
- [ ] **4.2** 若仓库存在 `spec/AI2AI/协议和数据.md`：追加 `POST /favorites/batch-remove` 说明；并同步 `前端架构信息.md` 中收藏页行为一句。

## 5. 验收（自测）

- [ ] **5.1** 登录后收藏 ≥2 条：多选 → 批量移除 → 列表与商品侧收藏数合理（与单条删一致）。
- [ ] **5.2** `mvn -q test`、`npm run build` 通过。

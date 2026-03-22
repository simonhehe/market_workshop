## Context

- 收藏夹页面 `Favorites.vue` 为网格列表，单条 `DELETE /favorites/{id}`；`FavoriteService.removeFavorite` 已包含归属校验与商品收藏数递减。
- API 统一前缀 `/api`；鉴权由 `AuthMiddleware` 注入 `userId`。

## Goals / Non-Goals

**Goals:**

- 用户可对多条收藏**勾选**，并一次**批量取消收藏**。
- 后端对请求内所有 id **强校验**归属当前用户，避免 IDOR。
- 与单条删除在业务上**一致**（商品 `favorites` 计数、物理删除收藏行）。

**Non-Goals:**

- 批量加入收藏、跨用户操作、管理端接口。
- 异步任务或导出；超出上限时的分页批量（仅文档约定单次上限即可）。

## Decisions

1. **接口形态**：使用 **`POST /favorites/batch-remove`**，JSON body：`{ "favoriteIds": [1,2,3] }`（全链路统一使用 `favoriteIds`）。
   - *理由*：DELETE 带 body 在部分客户端/代理上兼容性差；POST 表达「提交一批 id」更清晰。
2. **幂等与空集**：`favoriteIds` 为空数组 → **400** 或 **200 + 删除 0 条**（推荐 **400** + 明确文案，便于前端校验）。
3. **实现策略**：Service 内循环调用与 `removeFavorite` **相同的私有逻辑**（或提取 `removeFavoriteInternal`），保证计数与删除一致；或使用 `deleteAllByIdInAndUserId` + 批量更新商品计数（二选一，优先**复用单条逻辑**降低回归风险）。
4. **单次上限**：默认 **100** 个 id，超出返回 **400**（可配置常量）。
5. **前端交互**：列表首行或顶部工具栏：**全选当前页**（本页即当前加载的全部收藏，因无分页时等同全部）、**取消全选**、**批量移除**；每条左侧 checkbox，`indeterminate` 可选。

## Risks / Trade-offs

- **[Risk]** 大批量一次请求阻塞 → **Mitigation**：上限 100；后续如有分页再改为「仅当前页批量」。
- **[Risk]** 部分 id 不存在或不属于用户 → **Mitigation**：整体事务内校验，**任一无效则整单失败**（简单）或**跳过无效**（需产品确认）；本设计采用 **整单失败** + 明确错误信息，与「安全优先」一致。
- **[Trade-off]** 循环单条删除较慢 → 数据量小时可接受；必要时再优化为批量 SQL。

## Migration Plan

- 仅代码与接口新增；**无需** DB 迁移。
- 部署顺序：先后端再前端；老前端未调用新接口则不受影响。

## Open Questions

- 若存在部分 id 无效，产品是否要求「部分成功」——当前设计为 **整单失败**；若需部分成功，需调整 API 响应结构（如 `removedIds` / `failedIds`）。

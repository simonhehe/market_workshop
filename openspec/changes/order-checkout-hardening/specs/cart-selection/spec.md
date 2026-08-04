## ADDED Requirements

### Requirement: 购物车勾选状态持久化

系统必须提供 API 使登录用户能够更新购物车条目的 `is_selected` 状态，且仅允许操作属于当前用户的 cart 记录。

#### Scenario: 勾选购物车条目

- **WHEN** 用户对属于自己的 cart 条目发送 `isSelected=1`
- **THEN** 系统持久化该条目为选中状态，并在后续 `GET /cart` 的 summary 中计入该条目

#### Scenario: 取消勾选购物车条目

- **WHEN** 用户对属于自己的 cart 条目发送 `isSelected=0`
- **THEN** 系统持久化该条目为未选中，且该条目不再计入 summary 的 `selectedCount` 与 `totalPoints`

#### Scenario: 操作他人购物车条目

- **WHEN** 用户尝试更新不属于自己 `userId` 的 cart 条目
- **THEN** 系统必须拒绝请求并返回 **CART_001**，且不修改任何 cart 记录

#### Scenario: cart 条目不存在

- **WHEN** 用户对不存在的 cart id 发起 select
- **THEN** 系统必须拒绝请求并返回 **CART_001**

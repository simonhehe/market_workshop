## ADDED Requirements

> **范围说明**：`OrderService.cancelOrder` 与 `PUT /api/orders/{id}/cancel` **已实现**。本 capability 规格化既有行为（AC-07~AC-08）；**无** 新增 cancel 实现 task。  
> **已知债**：`PointsHistory.balanceAfter` 在 cancel/create 路径可能不准确（baseline P1）；集成测试 **不得** 将其作为 AC-07 通过条件，修正见 tasks §7.2 defer。

### Requirement: 用户可取消 pending 订单

系统必须允许订单所属用户在订单处于 `pending` 状态时将订单置为 `cancelled`，并退还积分、回滚库存。

#### Scenario: 取消 pending 订单成功

- **WHEN** 订单所属用户对 `status=pending` 的订单发起取消
- **THEN** 系统返回成功，订单 `status` 变为 `cancelled`，用户积分增加订单 `totalPoints`，相关商品库存恢复，并写入类型为 `refund` 的积分历史记录

#### Scenario: 非 pending 订单不可取消

- **WHEN** 订单 `status` 不为 `pending`
- **THEN** 系统拒绝取消，错误信息表明只能取消待处理状态的订单，且状态、积分、库存均不变

#### Scenario: 非所属用户不可取消

- **WHEN** 非订单所属用户尝试取消该订单
- **THEN** 系统拒绝请求并返回 ORDER_002 或等价无权错误，且订单状态不变

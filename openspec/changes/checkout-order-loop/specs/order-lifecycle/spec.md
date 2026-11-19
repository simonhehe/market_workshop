# 订单状态与取消

## ADDED Requirements

### Requirement: 订单状态由服务端唯一计算

订单创建必须强制 `pending`；前端只展示服务端状态并映射文案：pending 待处理、processing 处理中、shipped 已发货、completed 已完成、cancelled 已取消。

#### 场景：列表展示
- **给定** 服务端返回 `pending`
- **当** 用户查看订单列表/详情
- **则** 展示「待处理」，前端不自行推断或回写状态

### Requirement: 仅本人且 pending 的订单可取消

取消接口必须先校验归属再校验状态；非本人返回 `ORDER_FORBIDDEN`，非 pending 返回 `ORDER_STATUS_INVALID`，两种情况均不改变数据。

#### 场景：非 pending 取消
- **给定** 订单状态为 `completed`
- **当** 本人请求取消
- **则** 返回 `ORDER_STATUS_INVALID`，状态、积分、库存不变

#### 场景：越权取消
- **给定** 订单属于他人
- **当** 当前用户请求取消
- **则** 返回 `ORDER_FORBIDDEN`，订单与资金数据不变

### Requirement: 取消必须返还积分与库存

取消成功必须在单事务内：置 `cancelled`、返还订单积分、写 refund 流水、恢复每项商品库存并回减销量（不低于 0）。

#### 场景：取消 pending 订单
- **给定** 本人 pending 订单
- **当** 取消成功
- **则** 状态为 `cancelled`，用户余额恢复下单前，库存恢复，流水 type=refund 且 `balanceAfter` 等于返还后余额

### Requirement: 取消入口仅对 pending 订单展示

前端订单列表与详情必须仅对 `status === 'pending'` 展示取消入口，不得提供其他状态流转。

#### 场景：非 pending 详情
- **给定** 订单 `completed`
- **当** 打开详情
- **则** 不展示取消按钮

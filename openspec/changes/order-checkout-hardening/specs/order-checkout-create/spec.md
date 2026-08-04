## ADDED Requirements

### Requirement: 提交订单创建 pending 状态

系统必须在用户提交合法结算请求时创建一条 `status=pending` 的订单，扣减用户积分、减少商品库存、写入积分消费记录，并删除购物车中已下单的选中条目。

#### Scenario: 正常下单成功

- **WHEN** 用户已登录，购物车存在 `is_selected=1` 的条目，商品库存充足，用户积分不低于订单合计，且 `addressId` 属于当前用户
- **THEN** 系统返回成功及订单对象，`status` 为 `pending`，`totalPoints` 等于选中商品积分合计，用户积分减少相应数量，商品库存减少，已下单 cart 项被删除

#### Scenario: 购物车无选中商品

- **WHEN** 用户提交订单但购物车中没有任何 `is_selected=1` 的条目
- **THEN** 系统拒绝请求并返回 ORDER_001，错误信息表明没有选中的商品，且不创建订单

#### Scenario: 商品不存在

- **WHEN** 购物车选中项引用的 `productId` 在数据库中不存在
- **THEN** 系统拒绝请求并返回 ORDER_001，错误信息表明商品不存在，且不创建订单

#### Scenario: 商品库存不足

- **WHEN** 某选中商品的 `stock` 小于购物车中该商品的 `quantity`
- **THEN** 系统拒绝请求并返回 ORDER_001，错误信息包含该商品名称及库存不足说明，且不创建订单

#### Scenario: 用户积分不足

- **WHEN** 用户当前积分小于订单所需 `totalPoints`
- **THEN** 系统拒绝请求并返回 ORDER_001，错误信息包含当前积分与所需积分，且不创建订单

### Requirement: 收货地址必填且归属当前用户

系统不得在未提供有效收货地址或地址不属于当前用户时创建订单。Controller 须将缺失、null、空串或非法数字的 `addressId` 规范为未提供（design D9）。

#### Scenario: 未提供 addressId

- **WHEN** 用户提交订单时 `addressId` 为空、未提供、空字符串或无法解析为有效 ID
- **THEN** 系统拒绝请求并返回 ORDER_001，错误信息为「请选择收货地址」，且不创建订单

#### Scenario: addressId 不属于当前用户

- **WHEN** 用户提交的 `addressId` 对应地址的 `userId` 与当前登录用户不一致
- **THEN** 系统拒绝请求并返回 ORDER_001，错误信息为「收货地址不存在」，且不创建订单

#### Scenario: 地址不存在

- **WHEN** 用户提交的 `addressId` 在数据库中不存在
- **THEN** 系统拒绝请求并返回 ORDER_001，错误信息为「收货地址不存在」，且不创建订单

#### Scenario: 地址已软删

- **WHEN** 用户提交的 `addressId` 对应地址记录的 `deletedAt` 非空
- **THEN** 系统拒绝请求并返回 ORDER_001，错误信息为「收货地址不存在」，且不创建订单

### Requirement: 订单地址快照

系统必须在创建订单时将所选地址的收件人、电话及完整地址写入订单快照字段。

#### Scenario: 快照字段 populated

- **WHEN** 订单使用有效 `addressId` 创建成功
- **THEN** 订单的 `receiver`、`receiverPhone`、`receiverAddress` 字段必须与下单时地址记录一致

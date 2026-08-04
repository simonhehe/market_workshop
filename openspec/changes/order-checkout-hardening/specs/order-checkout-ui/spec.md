## ADDED Requirements

### Requirement: 结算页路由与鉴权

系统必须提供需登录访问的结算页，路由为 `/orders/create`；页面须包裹于与商城其他用户页一致的 `Layout` 组件。

#### Scenario: 未登录访问结算页

- **WHEN** 未携带有效 JWT 的用户访问 `/orders/create`
- **THEN** 前端路由守卫必须重定向至登录页

#### Scenario: 已登录访问结算页

- **WHEN** 已登录用户访问 `/orders/create`
- **THEN** 系统必须展示结算页而非 404

### Requirement: 结算页展示 L2 关键区块

结算页必须展示与 PRD §4.0 及原型 `product/order.html` 对齐的关键区块：步骤指示（① done → ② active → ③未激活）、**顶栏可用积分（必选）**、收货地址单选列表（含 **默认地址 tag**）、已选商品清单（含 **商品图或 L2 占位**、库存警告）、订单备注（≤100 字）、应付积分侧栏（**商品件数、商品积分小计、合计**、当前积分、积分不足预览条）、提交按钮。视口宽度 ≤860px 时必须单列布局。

#### Scenario: 页面区块齐全

- **WHEN** 已登录用户打开结算页且购物车有选中商品
- **THEN** 页面必须同时呈现步骤条、顶栏积分、地址区、商品清单（含图/占位）、备注、积分侧栏（件数/小计/合计）及提交按钮

#### Scenario: 默认地址视觉 tag

- **WHEN** 地址列表中存在 `isDefault=1` 的地址
- **THEN** 该地址卡片必须展示「默认」类视觉标记（对齐原型 `tag-default`）

#### Scenario: 侧栏商品积分小计

- **WHEN** 结算页展示应付积分侧栏
- **THEN** 侧栏必须分别展示商品件数、商品积分小计（sumSubtotal）与合计（sumTotal）

#### Scenario: 响应式单列布局

- **WHEN** 视口宽度不超过 860px
- **THEN** 主内容与侧栏必须纵向排列

#### Scenario: 不含演示控件

- **WHEN** 结算页在生产环境渲染
- **THEN** 不得包含原型 `demo-bar` 或等价演示控件

### Requirement: 加载时积分与库存预览

结算页加载时必须展示只读预览；预览不得替代服务端提交校验（design D7）。库存数据通过对选中 cart 项关联的 **商品 API** 获取 `stock` 字段计算 stockWarn。

#### Scenario: 展示用户可用积分

- **WHEN** 结算页加载成功
- **THEN** 页面必须通过 `GET /api/user/info` 展示当前可用积分（顶栏与侧栏同步）

#### Scenario: 库存不足预览警告

- **WHEN** 某选中商品的可售库存小于购物车数量
- **THEN** 该商品行必须展示 stockWarn，但不阻止用户点击提交

#### Scenario: 积分不足预览条

- **WHEN** 用户可用积分小于订单应付合计
- **THEN** 侧栏必须展示 pointsGap（缺口积分），但不阻止用户点击提交

#### Scenario: 仅展示选中购物车项

- **WHEN** 结算页加载购物车数据
- **THEN** 商品清单必须仅包含 `is_selected=1` 的条目

### Requirement: 地址选择与默认地址

结算页必须从 `GET /api/addresses` 加载地址列表；若存在默认地址则默认选中。

#### Scenario: 默认地址预选

- **WHEN** 用户进入结算页且存在 `isDefault=1` 的地址
- **THEN** 该地址必须处于选中状态

#### Scenario: 无地址时的空态

- **WHEN** 地址列表为空
- **THEN** 必须展示无地址提示并引导添加，且 **不得** 提供可跳转「新增地址」操作（O-01 defer）

#### Scenario: 无选中商品进入结算

- **WHEN** 用户访问 `/orders/create` 但无 `is_selected=1` 的条目
- **THEN** 必须提示并引导返回 `/cart`

#### Scenario: 购物车完全为空

- **WHEN** 用户访问 `/orders/create` 且 cart 无任何条目
- **THEN** 必须提示并引导返回 `/cart` 或去选购

### Requirement: 提交订单与错误展示

结算页必须通过 `POST /api/orders` 提交 `{ addressId, remark? }`；失败须 banner **与** modal 双通道展示服务端错误；禁止本地伪下单。成功必须跳转订单详情。

#### Scenario: 提交成功跳转详情

- **WHEN** 服务端接受下单请求
- **THEN** 必须导航至 `/orders/{id}`，`status` 来自 API

#### Scenario: 未选地址前端拦截

- **WHEN** 用户未选地址即提交
- **THEN** 必须可见提示并 scroll 至地址区；若仍发请求则展示 ORDER_001

#### Scenario: 提交失败 banner 与 modal

- **WHEN** 服务端拒绝下单
- **THEN** 必须同时展示顶部 error-banner 与 modal，内容含服务端 `message`

#### Scenario: 备注透传并在详情展示

- **WHEN** 用户填写 ≤100 字备注并成功下单
- **THEN** 订单详情页必须展示与提交一致的 `remark`（AC-13）

#### Scenario: 全站禁止冻结文案

- **WHEN** 用户在订单列表或订单详情查看下单后的积分相关文案
- **THEN** 不得出现「积分已冻结」；须与服务端扣减语义一致

### Requirement: 订单状态只读展示（回归）

订单列表与详情页的 `status` 必须来自 API；`getStatusText` 类映射允许，但输入必须来自 API 返回的 `status` 字段。

#### Scenario: 列表展示服务端状态

- **WHEN** 用户查看订单列表或详情
- **THEN** 展示的状态必须基于 API 返回的 `status`，不得本地伪造 pending 等值

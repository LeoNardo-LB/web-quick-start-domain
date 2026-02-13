## ADDED Requirements

### Requirement: 用户可以修改订单收货地址

系统 SHALL 允许用户在订单发货前修改收货地址。

#### Scenario: 修改地址成功
- **GIVEN** 订单状态为 CREATED 或 PAID
- **WHEN** 用户提交新的收货地址
- **THEN** 订单的收货地址被更新
- **AND** 发布 OrderShippingAddressUpdatedEvent 事件

#### Scenario: 发货后修改地址失败
- **GIVEN** 订单状态为 SHIPPED
- **WHEN** 用户尝试修改收货地址
- **THEN** 系统抛出 BizException
- **AND** 错误码为 ORDER_STATUS_INVALID

#### Scenario: 订单不存在
- **GIVEN** 订单 ID 不存在
- **WHEN** 用户尝试修改收货地址
- **THEN** 系统抛出 BizException
- **AND** 错误码为 ORDER_NOT_FOUND

### Requirement: 地址修改必须验证地址有效性

系统 SHALL 验证新地址的完整性。

#### Scenario: 地址完整
- **GIVEN** 用户提交的地址包含省、市、区、详细地址
- **WHEN** 系统验证地址
- **THEN** 验证通过

#### Scenario: 地址不完整
- **GIVEN** 用户提交的地址缺少必填字段
- **WHEN** 系统验证地址
- **THEN** 返回参数校验错误

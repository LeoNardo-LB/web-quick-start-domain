## ADDED Requirements

### Requirement: 订单状态流转验证方法
系统 SHALL 提供 `canTransitionTo(OrderStatus targetStatus)` 方法，用于验证订单是否可以从当前状态流转到目标状态。

#### Scenario: 待支付状态可流转到已确认
- **WHEN** 订单状态为 PENDING
- **AND** 调用 `canTransitionTo(CONFIRMED)`
- **THEN** 返回 true

#### Scenario: 待支付状态可流转到已取消
- **WHEN** 订单状态为 PENDING
- **AND** 调用 `canTransitionTo(CANCELLED)`
- **THEN** 返回 true

#### Scenario: 待支付状态不可流转到已发货
- **WHEN** 订单状态为 PENDING
- **AND** 调用 `canTransitionTo(SHIPPED)`
- **THEN** 返回 false

#### Scenario: 已确认状态可流转到已支付
- **WHEN** 订单状态为 CONFIRMED
- **AND** 调用 `canTransitionTo(PAID)`
- **THEN** 返回 true

#### Scenario: 已确认状态可流转到已取消
- **WHEN** 订单状态为 CONFIRMED
- **AND** 调用 `canTransitionTo(CANCELLED)`
- **THEN** 返回 true

#### Scenario: 已支付状态可流转到已发货
- **WHEN** 订单状态为 PAID
- **AND** 调用 `canTransitionTo(SHIPPED)`
- **THEN** 返回 true

#### Scenario: 已支付状态可流转到已取消
- **WHEN** 订单状态为 PAID
- **AND** 调用 `canTransitionTo(CANCELLED)`
- **THEN** 返回 true

#### Scenario: 已发货状态可流转到已完成
- **WHEN** 订单状态为 SHIPPED
- **AND** 调用 `canTransitionTo(COMPLETED)`
- **THEN** 返回 true

#### Scenario: 已完成状态不可流转
- **WHEN** 订单状态为 COMPLETED
- **AND** 调用 `canTransitionTo(任意状态)`
- **THEN** 返回 false

#### Scenario: 已取消状态不可流转
- **WHEN** 订单状态为 CANCELLED
- **AND** 调用 `canTransitionTo(任意状态)`
- **THEN** 返回 false

#### Scenario: 相同状态不可流转
- **WHEN** 订单状态为任意非终态
- **AND** 调用 `canTransitionTo(当前状态)`
- **THEN** 返回 false

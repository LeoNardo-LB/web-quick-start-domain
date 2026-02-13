## ADDED Requirements

### Requirement: OrderAggr 支持状态流转验证
OrderAggr 聚合根 SHALL 包含 `canTransitionTo(OrderStatus targetStatus)` 实例方法，用于验证状态流转的合法性。

#### Scenario: 方法签名正确
- **WHEN** 查看 OrderAggr 类定义
- **THEN** 存在方法 `public boolean canTransitionTo(OrderStatus targetStatus)`

#### Scenario: 方法可被外部调用
- **WHEN** 在应用层或测试代码中
- **AND** 持有 OrderAggr 实例
- **THEN** 可以调用 `order.canTransitionTo(OrderStatus.CONFIRMED)`

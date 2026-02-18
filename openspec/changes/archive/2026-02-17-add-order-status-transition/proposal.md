## Why

订单系统目前缺乏订单状态流转的前置验证能力。在执行状态变更前，无法判断目标状态是否可达，可能导致非法状态流转。需要在 OrderAggr
聚合根中添加状态流转验证方法，确保业务规则的完整性和一致性。

## What Changes

- 在 OrderAggr 聚合根中添加 `canTransitionTo(OrderStatus targetStatus)` 方法
- 实现完整的状态流转规则验证
- 添加对应的单元测试覆盖所有状态组合

## Capabilities

### New Capabilities

- `order-status-validation`: 订单状态流转验证能力，提供状态流转的前置校验

### Modified Capabilities

- `exampleorder`: 扩展现有订单聚合根，增加状态验证方法

## Impact

**受影响代码**:

- `domain/src/main/java/org/smm/archetype/domain/exampleorder/OrderAggr.java` - 新增方法
- `domain/src/main/java/org/smm/archetype/domain/exampleorder/OrderStatus.java` - 可能需要扩展（如已存在则复用）
- `test/src/test/java/org/smm/archetype/test/cases/unittest/domain/exampleorder/OrderAggrUTest.java` - 新增测试用例

**依赖**:

- 无新增外部依赖

**API 影响**:

- 无 API 变更（仅内部领域方法）

# order-sort-field Specification

## Purpose

TBD - created by archiving change add-order-sort-field. Update Purpose after archive.

## Requirements

### Requirement: Order has sort order field

订单聚合根必须包含 sortOrder 字段，用于控制订单在列表中的显示顺序。

#### Scenario: Create order with default sort order

- **WHEN** 创建订单时未指定 sortOrder
- **THEN** sortOrder 默认值为 0

#### Scenario: Create order with custom sort order

- **WHEN** 创建订单时指定 sortOrder 为 100
- **THEN** 订单的 sortOrder 为 100

### Requirement: Order sort order can be updated

只有 CREATED 状态的订单可以修改排序值。

#### Scenario: Update sort order for created order

- **WHEN** 订单状态为 CREATED 时调用 updateSortOrder(50)
- **THEN** 订单的 sortOrder 更新为 50

#### Scenario: Cannot update sort order for paid order

- **WHEN** 订单状态为 PAID 时调用 updateSortOrder(50)
- **THEN** 抛出 BizException 异常，错误码为 ORDER_STATUS_INVALID

### Requirement: Sort order value range

sortOrder 字段使用 Integer 类型，支持正负值。

#### Scenario: Negative sort order value

- **WHEN** 创建订单时 sortOrder 为 -10
- **THEN** 订单的 sortOrder 为 -10

#### Scenario: Large positive sort order value

- **WHEN** 创建订单时 sortOrder 为 999
- **THEN** 订单的 sortOrder 为 999


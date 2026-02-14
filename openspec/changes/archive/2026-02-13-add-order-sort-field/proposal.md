## Why

当前订单列表按创建时间排序，但业务需要手动控制订单在列表中的显示顺序。例如：

- 促销订单需要置顶显示
- VIP 客户订单需要优先处理
- 特定类型订单需要固定排序

## What Changes

- 在 `OrderAggr` 聚合根中添加 `sortOrder` 字段（Integer 类型）
- 修改 `create` 工厂方法，支持设置排序顺序
- 添加 `updateSortOrder` 业务方法，允许在订单创建后修改排序顺序

## Capabilities

### New Capabilities

- `order-sort-field`: 订单排序字段功能，支持手动控制订单在列表中的显示顺序

### Modified Capabilities

- 无（这是新功能，不修改现有规格）

## Impact

**受影响的代码**：

- `domain/exampleorder/model/OrderAggr.java` - 添加 sortOrder 字段和相关方法

**数据库影响**：

- 需要在订单表中添加 `sort_order` 字段

**API 影响**：

- 创建订单 API 可选参数 `sortOrder`
- 更新订单排序 API（新接口）

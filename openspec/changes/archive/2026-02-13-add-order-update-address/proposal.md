## Why

用户在下单后可能需要修改收货地址（如填错地址、搬家等场景），当前系统不支持此功能，导致用户体验不佳。电商行业标准做法是允许在订单发货前修改收货地址。

## What Changes

- 新增 `OrderAggr.updateShippingAddress()` 方法，允许在订单创建后、发货前修改收货地址
- 新增 `UpdateShippingAddressCommand` 和对应的 Application 层处理
- 新增 REST API 端点 `PUT /api/orders/{id}/shipping-address`
- 新增单元测试和集成测试

**业务规则**：
- 只有 `CREATED` 和 `PAID` 状态的订单可以修改地址
- 发货后（`SHIPPED` 状态）不允许修改
- 记录 `OrderShippingAddressUpdatedEvent` 领域事件

## Capabilities

### New Capabilities

- `order-update-address`: 订单修改收货地址功能，包含地址验证、状态检查、事件发布

### Modified Capabilities

无（仅新增功能，不修改现有行为）

## Impact

### 代码变更

| 层级 | 文件 | 变更类型 |
|------|------|----------|
| Domain | `OrderAggr.java` | 新增方法 |
| Domain | `OrderErrorCode.java` | 新增错误码 |
| Application | `OrderAppService.java` | 新增方法 |
| Application | `UpdateShippingAddressCommand.java` | 新增类 |
| Adapter | `OrderController.java` | 新增端点 |
| Test | `OrderAggrUTest.java` | 新增测试 |
| Test | `OrderAppServiceUTest.java` | 新增测试 |
| Test | `OrderControllerITest.java` | 新增测试 |

### API 变更

- 新增 `PUT /api/orders/{id}/shipping-address`
  - Request Body: `{ "address": { "province": "...", "city": "...", "district": "...", "detail": "..." } }`
  - Response: 更新后的订单 DTO

### 依赖

- 无新增外部依赖
- 复用现有的 `Address` 值对象

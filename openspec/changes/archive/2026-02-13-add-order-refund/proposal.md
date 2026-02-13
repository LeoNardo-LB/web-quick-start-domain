## Why

用户购买商品后可能因商品质量问题、发错货、不满意等原因需要申请退款。当前订单系统仅支持取消功能，无法处理已支付订单的部分或全部退款场景。需要新增退款功能以完善订单生命周期管理，提升用户体验。

## What Changes

### 新增功能
- **退款申请**：用户可对已支付订单申请全额或部分退款
- **退款审核**：系统自动验证退款条件（退款原因、时间窗口、订单状态）
- **退款状态**：新增 `REFUNDED` 和 `PARTIALLY_REFUNDED` 订单状态
- **退款事件**：发布 `OrderRefundEventDTO` 事件通知下游系统
- **退款记录**：记录退款金额、退款原因、退款时间

### 修改内容
- 扩展 `OrderStatus` 枚举，新增 `REFUNDED` 和 `PARTIALLY_REFUNDED` 状态
- 在 `OrderAggr` 中添加 `refund()` 方法
- 在 `OrderAppService` 中添加 `refundOrder()` 用例
- 在 `OrderController` 中添加 `POST /api/orders/{orderId}/refund` 端点
- 扩展 `OrderDTO` 添加退款相关字段

## Capabilities

### New Capabilities
- `order-refund`: 订单退款功能，支持全额和部分退款，包含退款验证、退款处理、退款事件发布

### Modified Capabilities
- `api-design`: 新增退款 API 端点 `POST /api/orders/{orderId}/refund`

## Impact

### 受影响的代码模块

| 模块 | 文件 | 变更类型 |
|------|------|----------|
| **Domain** | `OrderAggr.java` | 修改 - 添加 refund() 方法 |
| **Domain** | `OrderStatus.java` | 修改 - 添加 REFUNDED、PARTIALLY_REFUNDED 状态 |
| **Domain** | `OrderRefundEventDTO.java` | **新增** |
| **Application** | `OrderAppService.java` | 修改 - 添加 refundOrder() 方法 |
| **Application** | `RefundOrderCommand.java` | **新增** |
| **Application** | `OrderDTO.java` | 修改 - 添加退款字段 |
| **Application** | `RefundOrderResultDTO.java` | **新增** |
| **Adapter** | `OrderController.java` | 修改 - 添加退款端点 |
| **Adapter** | `RefundOrderRequest.java` | **新增** |

### API 变更
- **新增**: `POST /api/orders/{orderId}/refund` - 申请退款
- **请求体**: `{ "refundAmount": 100.00, "refundReason": "商品质量问题", "refundType": "FULL|PARTIAL" }`
- **响应体**: `{ "success": true, "data": { "refundId": "...", "refundStatus": "PROCESSING", ... } }`

### 依赖系统
- **支付网关**：需要调用退款接口
- **库存系统**：退款成功后可能需要恢复库存
- **通知系统**：退款状态变更通知

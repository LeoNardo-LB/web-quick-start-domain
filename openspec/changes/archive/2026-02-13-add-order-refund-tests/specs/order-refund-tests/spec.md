## ADDED Requirements

### Requirement: OrderAggr.refund() 单元测试

系统 SHALL 为 `OrderAggr.refund()` 方法提供完整的单元测试覆盖，验证所有业务规则和边界条件。

#### Scenario: 全额退款成功
- **WHEN** 订单状态为 PAID 且退款金额等于订单总金额
- **THEN** 订单状态变为 REFUNDED，totalRefundedAmount 等于订单总金额，发布 OrderRefundEventDTO 事件

#### Scenario: 部分退款成功
- **WHEN** 订单状态为 PAID 且退款金额小于订单总金额
- **THEN** 订单状态变为 PARTIALLY_REFUNDED，totalRefundedAmount 等于本次退款金额，发布 OrderRefundEventDTO 事件

#### Scenario: 多次累计退款达到全额
- **WHEN** 订单已部分退款，再次退款使累计退款等于订单总金额
- **THEN** 订单状态变为 REFUNDED，totalRefundedAmount 等于订单总金额

#### Scenario: 多次累计退款仍为部分退款
- **WHEN** 订单已部分退款，再次退款后累计退款仍小于订单总金额
- **THEN** 订单状态保持 PARTIALLY_REFUNDED，totalRefundedAmount 正确累计

#### Scenario: 状态不允许退款
- **WHEN** 订单状态为 CREATED、CANCELLED 或其他不允许退款的状态
- **THEN** 抛出 BizException，错误码为 ORDER_STATUS_INVALID

#### Scenario: 退款金额无效
- **WHEN** 退款金额为 null 或小于等于 0
- **THEN** 抛出 BizException，错误码为 REFUND_AMOUNT_INVALID

#### Scenario: 退款金额超过剩余可退金额
- **WHEN** 退款金额超过订单总金额减去已退款金额
- **THEN** 抛出 BizException，错误码为 REFUND_AMOUNT_EXCEEDED

---

### Requirement: OrderAppService.refundOrder() 单元测试

系统 SHALL 为 `OrderAppService.refundOrder()` 方法提供单元测试，验证用例编排逻辑。

#### Scenario: 退款成功
- **WHEN** 调用 refundOrder 且订单存在且状态允许退款
- **THEN** 调用 orderRepository.save()，返回 OrderDTO

#### Scenario: 订单不存在
- **WHEN** 调用 refundOrder 且订单不存在
- **THEN** 抛出异常

#### Scenario: 领域层异常传播
- **WHEN** OrderAggr.refund() 抛出 BizException
- **THEN** 异常正确传播到调用方

---

### Requirement: OrderController.refundOrder() 集成测试

系统 SHALL 为 `POST /api/orders/{id}/refund` 端点提供集成测试，验证 REST API 行为。

#### Scenario: 退款成功返回 200
- **WHEN** 发送有效的退款请求
- **THEN** 返回 HTTP 200，响应体包含退款后的订单信息

#### Scenario: 参数校验失败返回 400
- **WHEN** 发送缺少必填字段或格式错误的请求
- **THEN** 返回 HTTP 400，响应体包含校验错误信息

#### Scenario: 业务异常返回 400
- **WHEN** 订单状态不允许退款或退款金额超限
- **THEN** 返回 HTTP 400，响应体包含业务错误信息

#### Scenario: 订单不存在返回 404
- **WHEN** 请求的订单 ID 不存在
- **THEN** 返回 HTTP 404

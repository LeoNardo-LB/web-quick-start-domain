# order-refund Specification

## Purpose
定义订单退款功能的业务规则、状态流转、事件发布和 API 规范，确保退款操作符合 DDD 架构设计并满足电商业务需求。

## Requirements

### Requirement: 退款业务规则

The system SHALL implement order refund functionality with proper business rule validation.

#### Scenario: 全额退款

- **WHEN** 用户对已支付订单申请全额退款
- **THEN** 系统必须验证订单状态为 PAID 或 PARTIALLY_REFUNDED
- **THEN** 系统必须验证退款金额等于订单剩余可退金额
- **THEN** 系统必须更新订单状态为 REFUNDED
- **THEN** 系统必须记录退款金额、退款原因、退款时间
- **THEN** 系统必须发布 OrderRefundEventDTO 事件

#### Scenario: 部分退款

- **WHEN** 用户对已支付订单申请部分退款
- **THEN** 系统必须验证订单状态为 PAID 或 PARTIALLY_REFUNDED
- **THEN** 系统必须验证退款金额小于订单剩余可退金额
- **THEN** 系统必须更新订单状态为 PARTIALLY_REFUNDED
- **THEN** 系统必须累计已退款金额
- **THEN** 系统必须发布 OrderRefundEventDTO 事件

#### Scenario: 不可退款订单处理

- **WHEN** 用户对不可退款状态的订单申请退款
- **THEN** 系统必须拒绝退款请求
- **THEN** 系统必须返回错误信息 "ORDER_STATUS_INVALID"
- **THEN** 系统必须记录退款失败日志

---

### Requirement: 退款金额验证

The system SHALL validate refund amount to prevent over-refund.

#### Scenario: 退款金额超限

- **WHEN** 用户申请的退款金额超过订单剩余可退金额
- **THEN** 系统必须拒绝退款请求
- **THEN** 系统必须返回错误信息 "REFUND_AMOUNT_EXCEEDED"
- **THEN** 系统必须在错误信息中显示剩余可退金额

#### Scenario: 退款金额为零或负数

- **WHEN** 用户申请的退款金额为零或负数
- **THEN** 系统必须拒绝退款请求
- **THEN** 系统必须返回错误信息 "REFUND_AMOUNT_INVALID"

---

### Requirement: 退款状态流转

The system SHALL manage order status transitions during refund process.

#### Scenario: 状态流转验证

- **WHEN** 订单状态为 CREATED
- **THEN** 系统必须禁止退款操作（应使用取消功能）
- **WHEN** 订单状态为 PAID
- **THEN** 系统必须允许全额或部分退款
- **WHEN** 订单状态为 PARTIALLY_REFUNDED
- **THEN** 系统必须允许再次退款（不超过剩余可退金额）
- **WHEN** 订单状态为 SHIPPED
- **THEN** 系统必须禁止退款操作（需要先退货）
- **WHEN** 订单状态为 COMPLETED
- **THEN** 系统必须禁止退款操作
- **WHEN** 订单状态为 CANCELLED
- **THEN** 系统必须禁止退款操作
- **WHEN** 订单状态为 REFUNDED
- **THEN** 系统必须禁止退款操作

---

### Requirement: 退款事件发布

The system SHALL publish domain events when order is refunded.

#### Scenario: 发布退款事件

- **WHEN** 订单退款成功
- **THEN** 系统必须发布 OrderRefundEventDTO
- **THEN** 事件必须包含订单ID、订单编号、客户ID、退款金额、退款类型、退款原因、退款时间
- **THEN** 事件必须继承 DomainEventDTO

#### Scenario: 事件消费者处理

- **WHEN** OrderRefundEventDTO 被消费
- **THEN** 库存服务可选择恢复库存（根据退款类型）
- **THEN** 通知服务必须发送退款通知
- **THEN** 财务服务必须记录退款流水

---

### Requirement: 退款 API 设计

The system SHALL provide REST API for order refund.

#### Scenario: 申请退款 API

- **WHEN** 客户端调用 POST /api/orders/{orderId}/refund
- **THEN** 请求体必须包含 refundAmount、refundReason、refundType
- **THEN** 系统必须验证 orderId 存在
- **THEN** 系统必须返回退款结果（包含 refundId、refundStatus）
- **THEN** 响应格式必须符合 BaseResult 规范

#### Scenario: API 错误响应

- **WHEN** 退款失败
- **THEN** 系统必须返回统一错误格式
- **THEN** errorCode 必须使用常量（如 ORDER_STATUS_INVALID、REFUND_AMOUNT_EXCEEDED）
- **THEN** errorMessage 必须清晰描述失败原因

---

### Requirement: 数据持久化

The system SHALL persist refund information in order aggregate.

#### Scenario: 退款字段存储

- **WHEN** 订单退款成功
- **THEN** 系统必须更新 refundedAmount 字段（累计已退款金额）
- **THEN** 系统必须更新 refundReason 字段
- **THEN** 系统必须更新 refundedTime 字段
- **THEN** 系统必须更新 refundType 字段（FULL/PARTIAL）

#### Scenario: 查询退款信息

- **WHEN** 查询订单详情
- **THEN** 系统必须返回退款相关字段（refundedAmount、refundReason、refundedTime、refundType）
- **THEN** 未退款的订单，退款字段必须为空

---

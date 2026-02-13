## Context

当前订单系统支持创建、支付、发货、退款等核心流程，但缺少订单创建后修改收货地址的能力。本设计在现有 DDD 架构下扩展订单聚合根，添加地址修改功能。

**约束**：
- 必须遵循 DDD 四层架构原则
- 地址修改必须在 Domain 层进行业务规则校验
- 复用现有的 `Address` 值对象

## Goals / Non-Goals

**Goals:**
- 实现 `OrderAggr.updateShippingAddress()` 方法
- 添加 REST API 端点供前端调用
- 确保业务规则（状态检查）在 Domain 层强制执行
- 发布领域事件供审计使用

**Non-Goals:**
- 不支持发货后修改地址（需取消订单重下）
- 不涉及物流系统对接
- 不修改数据库表结构（地址字段已存在）

## Decisions

### Decision 1: 状态校验位置
- **选择**: 在 `OrderAggr.updateShippingAddress()` 方法内部校验
- **理由**: 保持业务规则在 Domain 层，符合 DDD 原则
- **替代方案**: 在 Application 层校验 → 拒绝，违反聚合根职责

### Decision 2: API 设计
- **选择**: `PUT /api/orders/{id}/shipping-address`
- **理由**: 符合 RESTful 规范，PUT 表示更新资源
- **替代方案**: `PATCH /api/orders/{id}` → 拒绝，语义不够明确

### Decision 3: 事件设计
- **选择**: 发布 `OrderShippingAddressUpdatedEvent`
- **理由**: 供审计日志、通知系统使用
- **事件字段**: orderId, oldAddress, newAddress, updatedAt

## Risks / Trade-offs

- **[并发修改风险]** → 使用乐观锁（version 字段）防止并发修改
- **[恶意修改风险]** → 在 Application 层添加操作日志记录

## Implementation Sequence

1. Domain 层：添加 `updateShippingAddress()` 方法和错误码
2. Application 层：添加 Command 和 AppService 方法
3. Adapter 层：添加 Controller 端点
4. 测试：编写单元测试和集成测试

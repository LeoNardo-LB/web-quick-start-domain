## Context

退款功能已通过 `add-order-refund` 变更实现，包含以下组件：

| 层级 | 组件 | 功能 |
|------|------|------|
| Domain | `OrderAggr.refund()` | 退款业务逻辑：状态验证、金额验证、累计退款、状态转换、事件发布 |
| Domain | `RefundType` | 退款类型枚举：FULL（全额）、PARTIAL（部分） |
| Domain | `OrderRefundEventDTO` | 退款事件 DTO |
| App | `OrderAppService.refundOrder()` | 退款用例编排：查询订单、构建金额、执行退款、保存 |
| App | `RefundOrderCommand` | 退款命令 DTO |
| Adapter | `OrderController.refundOrder()` | REST API 端点 |
| Adapter | `RefundOrderRequest` | 退款请求 DTO |

**测试约束**：
- 单元测试继承 `UnitTestBase`（纯 Mock，不启动 Spring）
- 集成测试继承 `IntegrationTestBase`（@SpringBootTest）
- 测试命名：单元测试 `*UTest`，集成测试 `*ITest`
- 覆盖率要求：单元测试行≥95%、分支≥95%

## Goals / Non-Goals

**Goals:**
- 为 `OrderAggr.refund()` 编写全面的单元测试，覆盖所有业务规则和边界条件
- 为 `OrderAppService.refundOrder()` 编写单元测试，验证用例编排逻辑
- 为 `OrderController.refundOrder()` 编写集成测试，验证 REST API 行为
- 达到项目宪章规定的覆盖率标准

**Non-Goals:**
- 不修改现有退款功能实现
- 不测试 `RefundType` 枚举（无逻辑需要测试）
- 不测试 DTO 类（纯数据容器）
- 不进行性能测试或压力测试

## Decisions

### 1. 测试分层策略

**决定**：按层级分离测试，每个层级独立测试

| 层级 | 测试类型 | 测试内容 |
|------|----------|----------|
| Domain | 单元测试 | 纯业务逻辑，Mock 依赖 |
| App | 单元测试 | 用例编排，Mock Repository 和 DomainService |
| Adapter | 集成测试 | REST API，启动 Spring 上下文 |

**理由**：
- Domain 层无外部依赖，适合纯单元测试
- App 层通过 Mock 隔离外部依赖，测试编排逻辑
- Adapter 层需要验证 HTTP 请求/响应，使用集成测试

### 2. OrderAggr.refund() 测试用例设计

**决定**：按业务规则设计测试用例

| 测试场景 | 测试方法 | 验证点 |
|----------|----------|--------|
| 全额退款成功 | `testRefund_FullRefund_Success` | 状态变为 REFUNDED，事件发布 |
| 部分退款成功 | `testRefund_PartialRefund_Success` | 状态变为 PARTIALLY_REFUNDED |
| 多次累计退款 | `testRefund_MultipleRefunds_Cumulative` | 累计金额正确，最终全额退款 |
| 状态不允许退款 | `testRefund_InvalidStatus_ThrowsException` | 抛出 BizException |
| 退款金额无效 | `testRefund_InvalidAmount_ThrowsException` | 抛出 BizException |
| 退款金额超限 | `testRefund_ExceededAmount_ThrowsException` | 抛出 BizException |

**理由**：覆盖所有业务规则和边界条件，确保领域逻辑正确性

### 3. OrderAppService.refundOrder() 测试用例设计

**决定**：验证用例编排流程

| 测试场景 | 测试方法 | 验证点 |
|----------|----------|--------|
| 退款成功 | `testRefundOrder_Success` | 调用 Repository，返回 DTO |
| 订单不存在 | `testRefundOrder_OrderNotFound_ThrowsException` | 抛出异常 |

**理由**：应用层测试重点在编排逻辑，业务规则已在 Domain 层验证

### 4. OrderController.refundOrder() 测试用例设计

**决定**：使用 WebTestClient 测试 REST API

| 测试场景 | 测试方法 | 验证点 |
|----------|----------|--------|
| 退款成功 | `testRefundOrder_Success_Returns200` | HTTP 200，响应体正确 |
| 参数校验失败 | `testRefundOrder_InvalidRequest_Returns400` | HTTP 400 |
| 业务异常 | `testRefundOrder_BusinessError_Returns400` | HTTP 400，错误信息正确 |

**理由**：集成测试验证完整的请求-响应流程

## Risks / Trade-offs

| 风险 | 缓解措施 |
|------|----------|
| 测试数据构建复杂 | 使用 Builder 模式和工厂方法简化测试数据创建 |
| 集成测试执行慢 | 仅在必要时运行集成测试，开发时优先单元测试 |
| 覆盖率未达标 | 使用 JaCoCo 报告识别未覆盖的分支 |

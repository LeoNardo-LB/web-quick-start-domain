## Why

退款功能已实现（add-order-refund 变更），但缺少测试覆盖。为满足项目宪章的 TDD 原则和覆盖率要求（单元测试行≥95%、分支≥95%），需要为退款功能编写完整的单元测试和集成测试。

## What Changes

- 新增 `OrderAggr.refund()` 方法的单元测试，覆盖状态验证、金额验证、累计退款、状态转换、事件发布等场景
- 新增 `OrderAppService.refundOrder()` 方法的单元测试，覆盖正常流程和异常处理
- 新增 `OrderController.refundOrder()` 端点的集成测试，覆盖 REST API 调用

## Capabilities

### New Capabilities

- `order-refund-tests`: 订单退款功能的测试套件，包含：
  - Domain 层：`OrderAggrUTest` - 聚合根退款业务逻辑测试
  - Application 层：`OrderAppServiceUTest` - 应用服务退款用例测试
  - Adapter 层：`OrderControllerITest` - REST API 退款端点测试

### Modified Capabilities

无（仅新增测试代码，不修改现有行为）

## Impact

### 测试文件（新增）

| 层级 | 测试类 | 位置 |
|------|--------|------|
| Domain | `OrderAggrUTest` | `test/cases/unittest/domain/exampleorder/` |
| App | `OrderAppServiceUTest` | `test/cases/unittest/app/exampleorder/` |
| Adapter | `OrderControllerITest` | `test/cases/integrationtest/adapter/exampleorder/` |

### 覆盖的测试场景

**OrderAggr.refund() 单元测试**：
- 正常场景：全额退款、部分退款、多次累计退款
- 异常场景：状态不允许退款、退款金额无效、退款金额超限

**OrderAppService.refundOrder() 单元测试**：
- 正常场景：成功退款
- 异常场景：订单不存在、领域层异常传播

**OrderController.refundOrder() 集成测试**：
- 正常场景：POST /api/orders/{id}/refund 返回 200
- 异常场景：参数校验失败、业务异常返回 400

### 依赖

- 测试基类：`UnitTestBase`、`IntegrationTestBase`
- Mock 框架：Mockito
- 断言框架：JUnit 5 Assertions

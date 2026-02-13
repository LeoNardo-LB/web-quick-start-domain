## 0. 前置准备（必读）

- [x] 0.1 阅读 AGENTS.md 了解技能加载要求
- [x] 0.2 阅读 .opencode/skills/tdd-workflow/SKILL.md 了解 TDD 流程

## 1. Domain 层实现

- [ ] 1.1 在 `OrderAggr.java` 中添加 `updateShippingAddress(Address newAddress)` 方法
- [ ] 1.2 在 `OrderErrorCode.java` 中添加 `ORDER_SHIPPING_ADDRESS_UPDATE_NOT_ALLOWED` 错误码
- [ ] 1.3 创建 `OrderShippingAddressUpdatedEvent.java` 领域事件

## 2. Application 层实现

- [ ] 2.1 创建 `UpdateShippingAddressCommand.java`
- [ ] 2.2 在 `OrderAppService.java` 中添加 `updateShippingAddress()` 方法

## 3. Adapter 层实现

- [ ] 3.1 在 `OrderController.java` 中添加 `PUT /api/orders/{id}/shipping-address` 端点
- [ ] 3.2 创建 `UpdateShippingAddressRequest.java` 请求 DTO

## 4. 测试

- [ ] 4.1 创建 `OrderAggrUTest.java` 中修改地址相关的单元测试
- [ ] 4.2 创建 `OrderAppServiceUTest.java` 中修改地址相关的单元测试
- [ ] 4.3 创建 `OrderControllerITest.java` 中修改地址相关的集成测试

## 5. 验证

- [ ] 5.1 运行单元测试验证
- [ ] 5.2 运行集成测试验证
- [ ] 5.3 运行覆盖率验证
- [ ] 5.4 运行启动验证
- [ ] 5.5 提交代码

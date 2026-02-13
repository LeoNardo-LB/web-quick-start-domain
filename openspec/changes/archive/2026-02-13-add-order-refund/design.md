## Context

### 背景
当前订单系统已实现完整的订单生命周期管理（创建→支付→发货→完成→取消），但缺少退款功能。用户在已支付状态下无法申请退款，需要通过"取消订单"来间接实现，这不符合电商业务的常见场景。

### 现有架构约束
- **DDD 四层架构**：Domain → Application → Infrastructure → Adapter
- **聚合根设计**：OrderAggr 管理订单状态，通过 `recordEvent()` 发布领域事件
- **CQRS 模式**：Command/Query 分离，事务边界在 Application 层
- **MapStruct 转换**：Domain ↔ DTO 转换通过 OrderDtoConverter 实现

### 相关利益方
- **用户**：申请退款、查看退款状态
- **客服**：审核退款申请（后续扩展）
- **财务**：处理退款流水
- **库存系统**：退款成功后恢复库存

## Goals / Non-Goals

**Goals:**
1. 实现订单退款的核心业务逻辑（全额/部分退款）
2. 新增退款相关状态（REFUNDED、PARTIALLY_REFUNDED）
3. 发布退款事件通知下游系统
4. 提供退款 API 端点供前端调用

**Non-Goals:**
1. 退款审核流程（人工审核）- 后续迭代
2. 退款对账功能 - 后续迭代
3. 退款失败重试机制 - 复用现有事件重试策略
4. 第三方支付网关对接 - 本次仅记录退款意向

## Decisions

### Decision 1: 退款状态设计

**选择**: 新增 `REFUNDED` 和 `PARTIALLY_REFUNDED` 两个状态

**原因**:
- 全额退款和部分退款是不同的业务语义
- 部分退款后订单仍可继续发货或申请二次退款
- 便于后续统计分析退款率

**替代方案**:
- ❌ 复用 CANCELLED 状态：退款和取消是不同概念，取消不涉及资金流动
- ❌ 只用一个 REFUNDED 状态：无法区分全额/部分退款

### Decision 2: 退款金额验证位置

**选择**: 在 Domain 层（OrderAggr.refund()）验证退款金额

**原因**:
- 退款金额验证是核心业务规则，属于领域逻辑
- 聚合根负责维护自身一致性
- 遵循 DDD "富领域模型" 原则

**替代方案**:
- ❌ 在 Application 层验证：违反 DDD 原则，业务规则分散

### Decision 3: 部分退款后的状态流转

**选择**: 部分退款后订单状态变为 `PARTIALLY_REFUNDED`，可继续发货或完成

**原因**:
- 实际电商场景中，部分退款很常见（如多商品订单退一件）
- 部分退款不应阻止订单继续流转
- 状态机设计：`PAID → PARTIALLY_REFUNDED → SHIPPED/COMPLETED`

**替代方案**:
- ❌ 部分退款后直接完成订单：不符合业务逻辑，用户可能还想退货其他商品

### Decision 4: 退款事件设计

**选择**: 新增 `OrderRefundEventDTO`，包含退款金额、退款原因、退款类型

**原因**:
- 退款事件与取消事件语义不同
- 下游系统（库存、通知、财务）需要知道退款详情
- 遵循现有事件设计模式（继承 DomainEventDTO）

## Risks / Trade-offs

### Risk 1: 并发退款
**风险**: 用户可能多次点击退款按钮，导致重复退款
**缓解**: 
- 在 OrderAggr 中使用乐观锁（版本号）
- 在 Controller 层添加幂等性检查（Idempotency-Key）

### Risk 2: 退款金额超限
**风险**: 部分退款累计金额超过订单总额
**缓解**: 
- 在 OrderAggr 中维护 `refundedAmount` 字段
- 每次退款前验证 `剩余可退金额 >= 本次退款金额`

### Risk 3: 退款后订单状态混乱
**风险**: 部分退款后订单状态可能不符合预期
**缓解**: 
- 严格的单元测试覆盖所有状态流转场景
- 在 OrderStatus 中添加 `canRefund()` 验证方法

## Migration Plan

### 阶段 1: 基础实现（本次迭代）
1. 新增 REFUNDED、PARTIALLY_REFUNDED 状态
2. 实现 OrderAggr.refund() 方法
3. 实现退款 API 端点
4. 添加单元测试和集成测试

### 阶段 2: 支付网关对接（后续迭代）
1. 对接支付宝/微信退款接口
2. 实现退款状态查询
3. 处理退款失败场景

### 回滚策略
- 退款功能为增量添加，不影响现有功能
- 如需回滚，删除退款相关代码即可
- 数据库字段（refundAmount、refundReason）可为空，不影响现有数据

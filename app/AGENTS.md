# Application 层 - 应用服务层

**用例编排层**：CQRS 编排、DTO 转换、事务边界、事件处理器。

## 目录结构

```
app/src/main/java/org/smm/archetype/app/
├── shared/             # 共享应用服务
│   ├── event/          # 事件处理器（@Async 异步执行）
│   ├── query/          # 共享查询服务
│   └── result/         # 统一响应（BaseResult、PageResult）
└── {模块}/             # 业务模块
    ├── command/        # 命令对象：{UseCase}Command
    ├── query/          # 查询对象：{UseCase}Query
    ├── dto/            # DTO：{Entity}DTO
    └── *AppService.java # 应用服务
```

## 关键查找

| 目标    | 位置                                       | 说明                      |
|-------|------------------------------------------|-------------------------|
| 应用服务  | `app/**/*AppService.java`                | CQRS 编排、事务边界            |
| 命令对象  | `app/**/command/*Command.java`           | 命名格式 `{UseCase}Command` |
| 查询对象  | `app/**/query/*Query.java`               | 命名格式 `{UseCase}Query`   |
| DTO   | `app/**/dto/*DTO.java`                   | 命名格式 `{Entity}DTO`      |
| 事件处理器 | `app/bizshared/event/*EventHandler.java` | 处理领域事件                  |

## 核心规则

### CQRS 模式（NON-NEGOTIABLE）

| 规则     | 说明                                 |
|--------|------------------------------------|
| 命令查询分离 | Command 和 Query 必须严格分离，不得混用        |
| 命令操作   | 修改状态，返回 void/ID/值对象，**禁止返回完整领域模型** |
| 查询操作   | 只读操作，返回 DTO 或分页结果，**不得包含业务逻辑**     |
| 事务边界   | 命令操作必须在事务边界内执行                     |

```java
// Command 示例
public class CreateOrderCommand {
    private String customerId;
    private Money totalAmount;
}

// Query 示例
public class OrderQuery {
    private String customerId;
}

// AppService - CQRS 分离
public class OrderAppService {
    // 命令：创建订单
    @Transactional(rollbackFor = Exception.class)
    public OrderId create(CreateOrderCommand command) {
        OrderAggr order = OrderAggr.create(command.getCustomerId(), command.getTotalAmount());
        orderRepository.save(order);
        return order.getId();
    }
    
    // 查询：查询订单
    @Transactional(readOnly = true)
    public List<OrderDTO> query(OrderQuery query) {
        return orderRepository.findByCustomerId(query.getCustomerId())
            .stream()
            .map(this::toDTO)
            .toList();
    }
}
```

### 事务管理原则（NON-NEGOTIABLE）

| 规则       | 说明                                                   |
|----------|------------------------------------------------------|
| 唯一位置     | 必须位于 Application 层（AppService 方法）                    |
| Domain 层 | **禁止**使用 `@Transactional`                            |
| 修改操作     | 必须使用 `@Transactional(rollbackFor = Exception.class)` |
| 只读查询     | 建议使用 `@Transactional(readOnly = true)` 优化性能          |
| 跨聚合事务    | 涉及多个聚合根时拆分为多个事务，避免大事务                                |
| 复杂场景     | 可使用 `TransactionTemplate` 编程式事务                      |

### DTO 转换责任

| 层级             | 转换方向              | 说明                         |
|----------------|-------------------|----------------------------|
| Controller     | Request → Command | Adapter 层职责                |
| AppService     | Domain → DTO      | Application 层职责，向外暴露       |
| Response       | DTO → Response    | Adapter 层，`fromDTO()` 静态方法 |
| Infrastructure | Domain ↔ DO       | 使用 MapStruct，包含枚举转换        |

### 事件处理器

| 规则   | 说明                                   |
|------|--------------------------------------|
| 异步执行 | 必须使用 `@Async("virtualTaskExecutor")` |
| 幂等性  | 事件处理必须幂等，重复处理不得产生副作用                 |
| 事件来源 | 仅处理 `Source.DOMAIN` 源的事件             |

## 反模式（禁止）

| ❌ 禁止                        | ✅ 正确做法                             |
|-----------------------------|------------------------------------|
| Domain 层使用 `@Transactional` | 事务边界在 AppService 方法                |
| 命令返回完整领域模型                  | 返回 ID 或值对象                         |
| 查询包含业务逻辑                    | 查询仅做数据组装                           |
| 事件处理器同步执行                   | 使用 `@Async("virtualTaskExecutor")` |
| 大事务包含多聚合                    | 拆分为多个独立事务                          |

---

## 相关文档

- [项目知识库](../AGENTS.md) - 架构概览和全局规范
- [Domain 层](../domain/AGENTS.md) - 领域层规范
- [Application 层](../app/AGENTS.md) - 应用层规范
- [Infrastructure 层](../infrastructure/AGENTS.md) - 基础设施层规范
- [Adapter 层](../adapter/AGENTS.md) - 接口层规范
- [Start 模块](../start/AGENTS.md) - 启动模块规范
- [Test 模块](../test/AGENTS.md) - 测试规范
- [TDD 流程](../openspec/config.yaml) - 四阶段验证流程

---
**版本**: 3.0 | **更新**: 2026-02-17

# APP 层

应用编排层 - CQRS、事务边界、用例协调。

## 文档导航

### 🔗 相关文档

| 文档                                             | 用途                | 读者      |
|------------------------------------------------|-------------------|---------|
| **[项目知识库](../AGENTS.md)**                      | 项目架构概览和架构偏差分析     | 开发者、架构师 |
| **[AI 开发指南](../CLAUDE.md)**                    | AI 开发元指南          | 开发者、AI  |
| **[项目 README](../README.md)**                  | 项目概览和快速开始         | 开发者、架构师 |
| [Domain 层指南](domain/AGENTS.md)                 | 领域层核心业务逻辑和约定      | 后端开发者   |
| [Infrastructure 层指南](infrastructure/AGENTS.md) | 基础设施层技术实现和约定      | 后端开发者   |
| [Adapter 层指南](adapter/AGENTS.md)               | 接口层 REST 控制器和事件监听 | 后端开发者   |
| [Start 模块指南](start/AGENTS.md)                  | 启动模块 Bean 装配和配置   | 后端开发者   |
| [Test 模块指南](test/AGENTS.md)                    | 测试模块测试规范和最佳实践     | 测试开发者   |

### 🔗 规格文档

- [验证流程指南](_docs/specification/业务代码生成(AI)流程.md)
- [业务代码编写规范](_docs/specification/业务代码编写规范.md)
- [测试代码编写与示例指南](_docs/specification/测试代码编写与示例指南.md)

### 🔗 业务文档

- [业务文档索引](_docs/business/README.md)

## 概述

- **职责**：用例编排、CQRS 分离、DTO 转换
- **依赖规则**：依赖 Domain，被 Adapter 调用
- **关键特点**：事务边界、多聚合协调、领域事件处理

## 结构
```
app/
├── bizshared/       # 共享应用服务
│   ├── result/      # BaseResult, PageResult 包装器
│   └── query/       # 查询服务基类
└── _example/        # 示例应用服务
    └── order/
        ├── OrderAppService.java     # 用例编排
        ├── command/                 # CQRS Commands（CreateOrderCommand 等）
        ├── query/                   # CQRS Queries（OrderQuery 等）
        └── dto/                     # 应用层 DTO（OrderDTO, MoneyDTO）
```

## 关键位置

| 任务            | 位置                      | 备注                     |
|---------------|-------------------------|------------------------|
| 应用服务          | app/**/*AppService.java | 用例编排                   |
| CQRS Commands | app/**/command/         | 写操作输入 DTO              |
| CQRS Queries  | app/**/query/           | 读操作输入 DTO              |
| 应用层 DTO       | app/**/dto/             | 数据传输对象                 |
| 结果包装器         | app/bizshared/result/   | BaseResult, PageResult |

## 约定（项目特有）

- **仅编排**：无业务规则（由 Domain 强制不变量）
- 事务边界：ApplicationService 方法上使用 `@Transactional`
- CQRS：分离 Command/Query DTO 和处理器
- 转换器：MapStruct 做 App DTO ↔ Domain/Response DTO 转换
- 协调多个聚合/服务

## CQRS 模式示例

```java
// Command（写操作）
public class CreateOrderCommand {
    private String customerId;
    private MoneyDTO totalAmount;
}

// Query（读操作）
public class OrderQuery {
    private String customerId;
}

// ApplicationService
public class OrderAppService {
    @Transactional
    public OrderId create(CreateOrderCommand command) {
        OrderAggr order = OrderAggr.create(command.getCustomerId(), 
            Money.of(command.getTotalAmount()));
        orderRepository.save(order);
        return order.getId();
    }

    public List<OrderDTO> query(OrderQuery query) {
        return orderRepository.findByCustomerId(query.getCustomerId())
            .stream()
            .map(this::toDTO)
            .toList();
    }
}
```

## 反模式

- ❌ 业务规则（移至 Domain 聚合）
- ❌ 直接访问基础设施（使用 Domain 的 Repository 接口）
- ❌ 领域对象暴露给 Adapter（通过 DTO 转换）
- ❌ 配置类（必须在 start/）
- ❌ `@Data` 注解

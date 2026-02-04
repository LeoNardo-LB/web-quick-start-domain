# DOMAIN 层

核心DDD业务逻辑 - 纯净领域模型，无外部依赖。

## 文档导航

### 🔗 相关文档

| 文档                                                    | 用途             | 读者      |
|-------------------------------------------------------|----------------|---------|
| **[项目知识库]`../AGENTS.md`**                             | 项目架构概览和架构偏差分析  | 开发者、架构师 |
| **[AI 开发指南]`../CLAUDE.md`**                           | AI 开发元指南       | 开发者、AI  |
| **[项目 README]`../README.md`**                         | 项目概览和快速开始      | 开发者、架构师 |
| **[Application 层指南]`../app/AGENTS.md`**               | 应用层 CQRS 和用例编排 | 后端开发者   |
| **[Infrastructure 层指南]`../infrastructure/AGENTS.md`** | 基础设施层技术实现和约定   | 后端开发者   |

### 🔗 规格文档

- [验证流程指南](_docs/specification/业务代码生成(AI)流程.md) - TDD 代码验证强制流程
- [业务代码编写规范](_docs/specification/业务代码编写规范.md) - 业务代码编码标准
- [测试代码编写与示例指南](_docs/specification/测试代码编写与示例指南.md) - 测试代码编写标准和完整示例

### 🔗 业务文档

- [业务文档索引](_docs/business/README.md) - 业务开发文档（需求、设计、技术选型等）

---

## 概述

- **职责**：聚合根、实体、值对象、领域事件、仓储接口定义
- **依赖规则**：零外部依赖（无Spring、MyBatis等）
- **关键特点**：业务规则封装、不变性保护、事件溯源

## 结构
```
domain/
├── bizshared/       # 共享领域基类
│   ├── base/        # AggregateRoot, Entity, DomainEvent, Identifier
│   ├── client/      # 技术客户端接口（CacheClient, NotificationClient）
│   └── event/       # DomainEvent 基类
├── common/          # 通用领域对象
│   ├── file/        # FileMetadata, FileType 值对象
│   └── search/      # SearchCondition, SortCondition
└── _example/        # 订单示例（75个类）
    └── order/
        ├── model/
        │   ├── OrderAggr.java          # 聚合根
        │   ├── entity/                 # 实体
        │   ├── valueobject/            # 值对象（Money, OrderStatus）
        │   └── event/                  # 领域事件（OrderCreatedEvent）
        ├── repository/                # 仓储接口
        └── service/                    # 领域服务
```

## 关键位置

| 任务   | 位置                           | 备注                      |
|------|------------------------------|-------------------------|
| 聚合根  | domain/**/model/*Aggr.java   | 业务规则封装                  |
| 值对象  | domain/**/model/valueobject/ | 不可变、无身份标识               |
| 领域事件 | domain/**/model/event/       | DomainEvent 子类          |
| 仓储接口 | domain/**/repository/        | 仅接口，实现在 infrastructure/ |
| 领域服务 | domain/**/service/           | 跨聚合业务逻辑                 |

## 约定（项目特有）

- **零外部依赖**：纯业务逻辑，禁用 Spring 注解
- 聚合根：通过 `recordEvent()` 记录事件
- 仓储：仅定义接口，实现在 infrastructure/
- 值对象：使用 `@ValueObject` 注解标记（自定义）
- 使用 Lombok：`@RequiredArgsConstructor`，禁止 `@Data`

## 反模式

- ❌ 外部库依赖（Spring、MyBatis、Lombok 部分）
- ❌ 仓储实现（仅定义接口）
- ❌ 数据库实体混入领域模型
- ❌ `@Data` 注解
- ❌ 贫血模型（业务逻辑在Service而非Entity）

## DDD 模式示例

```java
// 聚合根
public class OrderAggr extends AggregateRoot<OrderAggr, OrderId> {
    private OrderId id;
    private List<OrderItem> items;
    private OrderStatus status;

    public static OrderAggr create(String customerId, Money totalAmount) {
        OrderAggr order = new OrderAggr();
        order.id = OrderId.generate();
        order.status = OrderStatus.CREATED;
        order.recordEvent(new OrderCreatedEvent(order.id));
        return order;
    }

    public void pay(PaymentMethod method) {
        if (this.status != OrderStatus.CREATED) {
            throw new IllegalStateException("只有已创建的订单可以支付");
        }
        this.status = OrderStatus.PAID;
        this.recordEvent(new OrderPaidEvent(this.id));
    }
}
```

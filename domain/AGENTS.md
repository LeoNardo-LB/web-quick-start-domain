# DOMAIN 层

核心DDD业务逻辑 - 纯净领域模型，无外部依赖。

## 概述

职责：聚合根、实体、值对象、领域事件、仓储接口定义。
依赖规则：零外部依赖（无Spring、MyBatis等）。
规模：80+ Java 文件，19,358+ 行代码（项目核心）。

## 结构

```
domain/
├── bizshared/       # 共享领域基类（AggregateRoot, Entity, DomainEvent, Identifier）
│   ├── base/        # 领域对象基类
│   ├── client/      # 技术客户端接口
│   ├── event/       # DomainEvent 基类
│   └── util/        # 工具类（⚠️ MyBeanUtil.java 导入Spring，违反零依赖原则）
├── common/          # 通用领域对象（FileMetadata, SearchCondition）
└── _example/        # 订单示例（75个类）
    └── order/
        ├── model/    # OrderAggr（聚合根）、实体、值对象、事件
        ├── repository/ # 仓储接口
        └── service/    # 领域服务
```

## 关键位置

| 任务   | 位置                           | 备注                      |
|------|------------------------------|-------------------------|
| 聚合根  | domain/**/model/*Aggr.java   | 业务规则封装                  |
| 值对象  | domain/**/model/valueobject/ | 不可变、无身份标识               |
| 领域事件 | domain/**/model/event/       | DomainEvent 子类          |
| 仓储接口 | domain/**/repository/        | 仅接口，实现在 infrastructure/ |
| 领域服务 | domain/**/service/           | 跨聚合业务逻辑                 |

## 约定

零外部依赖：纯业务逻辑，禁用 Spring 注解。
聚合根：通过 `recordEvent()` 记录事件。
仓储：仅定义接口，实现在 infrastructure/。
Lombok：`@RequiredArgsConstructor`，禁止 `@Data`。

## 反模式

❌ 外部库依赖（Spring、MyBatis）、仓储实现、数据库实体（DO）混入、`@Data`、贫血模型。

## 架构偏差

**⚠️ P0 - Domain 层外部依赖违反**

- `domain/bizshared/util/MyBeanUtil.java` 导入 Spring 类（`BeanUtils`, `BeanWrapper`）
- **影响**：破坏 DDD 核心原则，领域层耦合 Spring，难以独立测试
- **建议**：重构为纯 Java，使用手动属性拷贝

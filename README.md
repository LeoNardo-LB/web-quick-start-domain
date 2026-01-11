# DDD Web Quick Start Domain

一个符合DDD（领域驱动设计）规范的Maven骨架项目，提供完整的基础设施和示例代码，帮助企业快速启动高质量的应用开发。

## 📋 目录

- [项目概述](#项目概述)
- [DDD符合度评分](#ddd符合度评分)
- [快速开始](#快速开始)
- [项目架构](#项目架构)
- [核心DDD概念](#核心ddd概念)
- [模块文档](#模块文档)
- [开发指南](#开发指南)
- [最佳实践](#最佳实践)
- [更新日志](#更新日志)

---

## 项目概述

本项目严格遵循DDD原则，提供了：

- ✅ 清晰的分层架构（Domain、Application、Infrastructure、Adapter）
- ✅ 完整的DDD基础设施（聚合根、值对象、领域事件、仓储）
- ✅ CQRS模式支持（Command/Query分离）
- ✅ 规格模式（Specification）
- ✅ 事件驱动架构（Kafka + 本地事件）
- ✅ 完整的代码示例和文档

### 📚 完整文档索引

| 文档                                                                                                  | 用途        | 目标读者   |
|-----------------------------------------------------------------------------------------------------|-----------|--------|
| [README.md](README.md)                                                                              | 项目概览和架构说明 | 所有人    |
| [CLAUDE.md](CLAUDE.md)                                                                              | AI开发元指南   | AI、开发者 |
| [业务代码编写规范.md](业务代码编写规范.md)                                                                          | 编码标准详细参考  | 开发者    |
| [代码AI生成工作流.md](代码AI生成工作流.md)                                                                        | 强制性代码生成流程 | AI、开发者 |
| [domain/README.md](domain/src/main/java/org/smm/archetype/domain/README.md)                         | 领域层详细指南   | 开发者    |
| [app/README.md](app/src/main/java/org/smm/archetype/app/README.md)                                  | 应用层详细指南   | 开发者    |
| [adapter/README.md](adapter/src/main/java/org/smm/archetype/adapter/README.md)                      | 接口层详细指南   | 开发者    |
| [infrastructure/README.md](infrastructure/src/main/java/org/smm/archetype/infrastructure/README.md) | 基础设施层详细指南 | 开发者    |

---

## DDD符合度评分

| 维度     | 得分         | 说明                        |
|--------|------------|---------------------------|
| 分层架构   | 9/10       | 严格的依赖方向，清晰的职责划分           |
| 领域模型   | 9/10       | 完整的聚合根、实体、值对象             |
| 仓储模式   | 9/10       | Repository/DataAccessor分离 |
| 领域事件   | 9/10       | 事件不可变，完整的发布消费机制           |
| 应用服务   | 9/10       | CQRS模式，事务边界清晰             |
| 领域服务   | 8/10       | 职责清晰，接口定义完善               |
| 限界上下文  | 7/10       | 按业务能力组织                   |
| **总分** | **8.6/10** | **优秀**                    |

---

## 快速开始

### 环境要求

- **JDK**: 25
- **Maven**: 3.8+
- **Spring Boot**: 3.5.9

### 编译项目

```bash
mvn clean compile
```

### 运行测试

```bash
# 单元测试
mvn test

# 启动测试（最关键）
mvn test -Dtest=ApplicationStartupTests -pl start
```

### 生产环境运行

```bash
mvn spring-boot:run -pl start
```

---

## 项目架构

### 四层架构

```
┌─────────────────────────────────────────────────────────┐
│                    Adapter Layer (接口层)                │
│  Controllers、Listeners、Schedules、DTOs                   │
│  职责：接收外部请求，参数验证，调用应用服务，返回响应          │
└────────────────────┬────────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────────┐
│                  Application Layer (应用层)               │
│  ApplicationServices、CQRS、事务管理、DTO转换               │
│  职责：用例编排，事务管理，事件发布                          │
└────────────────────┬────────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────────┐
│                     Domain Layer (领域层)                 │
│  AggregateRoot、Entity、ValueObject、DomainEvent           │
│  职责：核心业务逻辑，业务规则，领域模型                       │
└────────────────────▲────────────────────────────────────┘
                     │
┌────────────────────┴────────────────────────────────────┐
│              Infrastructure Layer (基础设施层)             │
│  Repository实现、EventPublisher、CacheService、OSS         │
│  职责：数据持久化，外部服务集成，技术实现                      │
└─────────────────────────────────────────────────────────┘
```

**依赖规则**: 依赖方向只能由外向内，外层可以引用内层，内层不感知外层

### 项目结构

```
web-quick-start-domain/
├── adapter/           # 接口层
│   ├── _shared/       # 共享组件（枚举、返回结果）
│   └── access/        # 接入适配器（Web、Listener、Schedule）
│
├── app/              # 应用层
│   ├── _shared/       # 共享应用层组件（注解、转换器）
│   └── common/        # 通用应用服务（文件、日志、通知）
│
├── domain/           # 领域层 ⭐
│   ├── _shared/       # DDD基类（Entity、AggregateRoot、ValueObject）
│   ├── common/        # 通用领域（文件、日志、通知）
│   └── _example/      # 示例领域（订单）
│
├── infrastructure/  # 基础设施层
│   ├── _shared/       # 共享基础设施（配置、事件发布、ID生成）
│   └── common/        # 通用基础设施实现（文件、日志、通知）
│
└── start/           # 启动模块
    ├── ApplicationBootstrap.java  # 主启动类
    └── application.yaml          # 配置文件
```

⭐ 标记为核心DDD基础设施

---

## 核心DDD概念

> 💡 **提示**: 以下是DDD核心概念的简要介绍，详细实现请参考[domain/README.md](domain/src/main/java/org/smm/archetype/domain/README.md)

### 1. 聚合根（AggregateRoot）

**特征**:
- 是聚合的入口点
- 维护聚合内部的一致性边界
- 管理领域事件

**示例**:

```java
public class Order extends AggregateRoot {
    public void pay(String paymentMethod) {
        if (!status.canPay()) {
            throw new IllegalStateException("只有已创建的订单可以支付");
        }
        this.status = OrderStatus.PAID;
        this.addDomainEvent(new OrderPaidEvent(this));
    }
}
```

**详细信息**: [domain/README.md - 聚合根](domain/src/main/java/org/smm/archetype/domain/README.md#1-聚合根aggregateroot)

### 2. 实体（Entity）

**特征**:

- 有唯一标识
- 可变性
- 通过业务方法修改状态

**详细信息**: [domain/README.md - 实体](domain/src/main/java/org/smm/archetype/domain/README.md#2-实体entity)

### 3. 值对象（ValueObject）

**特征**:

- 不可变
- 基于值的相等性
- 没有唯一标识

**示例**: Money、Address

**详细信息**: [domain/README.md - 值对象](domain/src/main/java/org/smm/archetype/domain/README.md#3-值对象value-object)

### 4. 领域事件（DomainEvent）

**特征**:
- 不可变
- 表示已发生的事实
- 使用过去式命名

**示例**: OrderCreatedEvent、OrderPaidEvent

**详细信息**: [domain/README.md - 领域事件](domain/src/main/java/org/smm/archetype/domain/README.md#4-领域事件domain-event)

### 5. 仓储模式（Repository）

**职责**:

- 定义持久化抽象接口（只定义，不实现）
- 维护一致性边界
- 发布领域事件

**详细信息**:

- [domain/README.md - 仓储接口](domain/src/main/java/org/smm/archetype/domain/README.md#5-仓储repository)
- [infrastructure/README.md - 仓储实现](infrastructure/src/main/java/org/smm/archetype/infrastructure/README.md#1-repository实现仓储实现)

### 6. CQRS模式

**特点**:
- Command（命令）- 写操作，改变状态
- Query（查询）- 读操作，不改变状态

**详细信息**: [app/README.md - CQRS](app/src/main/java/org/smm/archetype/app/README.md#2-cqrs命令查询职责分离)

### 7. 事件驱动机制

**架构**:

```
聚合根.addDomainEvent()
  → ApplicationService收集事件
  → EventPublisher.publish()
  → Kafka/Spring Events
  → EventHandler消费事件
```

**详细信息**:

- [业务代码编写规范.md - 事件驱动机制](业务代码编写规范.md#5-事件驱动机制)
- [infrastructure/README.md - EventPublisher实现](infrastructure/src/main/java/org/smm/archetype/infrastructure/README.md#2-eventpublisher实现事件发布器)

---

## 模块文档

### 📖 Domain Layer（领域层）

**职责**: 核心业务逻辑层，包含所有业务规则和领域模型

**核心概念**:

- AggregateRoot（聚合根）
- Entity（实体）
- ValueObject（值对象）
- DomainEvent（领域事件）
- Repository（仓储接口）
- Specification（规格模式）

**详细文档**: [domain/README.md](domain/src/main/java/org/smm/archetype/domain/README.md)

### 📖 Application Layer（应用层）

**职责**: 应用服务层，负责用例编排和事务管理

**核心概念**:

- ApplicationService（应用服务）
- CQRS（命令查询职责分离）
- 事务边界管理
- DTO转换
- 事件发布

**详细文档**: [app/README.md](app/src/main/java/org/smm/archetype/app/README.md)

### 📖 Adapter Layer（接口层）

**职责**: 系统的最外层，负责与外部系统的交互和适配

**核心组件**:

- Controller（REST控制器）
- EventListener（事件监听器）
- Schedule（定时任务）
- Request/Response DTO

**详细文档**: [adapter/README.md](adapter/src/main/java/org/smm/archetype/adapter/README.md)

### 📖 Infrastructure Layer（基础设施层）

**职责**: 提供技术实现和外部系统集成

**核心组件**:

- Repository实现
- EventPublisher（Kafka/Spring）
- CacheService（Redis/Caffeine）
- OssClient（本地存储/阿里云OSS）
- 配置类

**详细文档**: [infrastructure/README.md](infrastructure/src/main/java/org/smm/archetype/infrastructure/README.md)

---

## 开发指南

**快速参考**：

- **代码生成流程**：[代码AI生成工作流.md](代码AI生成工作流.md) - 4步强制验证流程
- **编码规范**：[业务代码编写规范.md](业务代码编写规范.md) - 权威编码标准
- **测试规范**：[测试代码编写规范.md](测试代码编写规范.md) - 测试代码生成标准
- **各层开发**
  ：参考各模块README（[domain](domain/src/main/java/org/smm/archetype/domain/README.md)、[app](app/src/main/java/org/smm/archetype/app/README.md)、[adapter](adapter/src/main/java/org/smm/archetype/adapter/README.md)、[infrastructure](infrastructure/src/main/java/org/smm/archetype/infrastructure/README.md)）

### 核心原则

1. **4步验证流程**：单元测试 → 编译 → 测试 → 启动验证
2. **测试要求**：每次生成业务代码后必须生成单测与集测用例，并保证通过
3. **编码规范**：禁止@Data、使用枚举、构造器注入、三层架构
4. **分层依赖**：Adapter → App → Domain ← Infrastructure

---

## 最佳实践

**详细最佳实践**：参考各模块README和[业务代码编写规范.md](业务代码编写规范.md)

### 核心最佳实践

**✅ DO（推荐）**：

- 通过业务方法修改状态（如 `order.pay()`）
- 在聚合根内发布领域事件
- 使用规格模式封装业务规则
- 在应用服务中管理事务

**❌ DON'T（避免）**：

- 不要使用setter修改状态
- 不要在外部直接操作聚合内部集合
- 不要在应用服务中编写业务逻辑
- 不要为聚合内部的实体创建Repository

---

## 技术栈

| 分类       | 技术           | 版本     | 说明        |
|----------|--------------|--------|-----------|
| **语言**   | Java         | 25     | 虚拟线程支持    |
| **核心框架** | Spring Boot  | 3.5.9  | 基础框架      |
| **持久层**  | MyBatis-Flex | 1.11.5 | ORM框架     |
| **消息队列** | Kafka        | -      | 事件驱动      |
| **缓存**   | Redis        | -      | 分布式缓存     |
| **工具库**  | Lombok       | latest | 简化代码      |
| **工具库**  | Guava        | 33.5.0 | Google工具库 |
| **工具库**  | Hutool       | 5.8.41 | Java工具库   |

---

## 更新日志

### 2026/01/10 - 文档重构

**重大更新**:

1. **文档重组**
    - ✅ 创建[业务代码编写规范.md](业务代码编写规范.md)（1,143行，18KB）
    - ✅ 创建[代码AI生成工作流.md](代码AI生成工作流.md)（666行，11KB）
    - ✅ 创建[domain/README.md](domain/src/main/java/org/smm/archetype/domain/README.md)（590行，15KB）
    - ✅ 创建[adapter/README.md](adapter/src/main/java/org/smm/archetype/adapter/README.md)（639行）
    - ✅ 创建[app/README.md](app/src/main/java/org/smm/archetype/app/README.md)（589行）
    - ✅ 创建[infrastructure/README.md](infrastructure/src/main/java/org/smm/archetype/infrastructure/README.md)（997行）

2. **文档特点**
    - 清晰的职责划分
    - 完整的代码示例
    - 详细的最佳实践
    - 交叉引用导航

### 2026/01/09 - 功能增强

**新增功能**:

- Kafka事件发布集成
- 消息消费重试机制
- 对象存储服务（RustFS集成准备）
- 多云消息服务（短信/邮件）

**依赖更新**:

- Spring Boot 3.5.9
- MyBatis-Flex 1.11.5
- Kafka集成

**数据库表**:

- event_publish（事件发布表）
- event_consume（事件消费表）
- file_info（文件信息表）

---

## 参考资源

### 📚 推荐书籍

- 《领域驱动设计》- Eric Evans
- 《实现领域驱动设计》- Vaughn Vernon
- 《领域驱动设计精粹》- Vaughn Vernon

### 🔗 相关链接

- [DDD Community](https://ddcommunity.org/)
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [MyBatis-Flex Documentation](https://mybatis-flex.com/)

---

## 许可证

本项目采用 Apache License 2.0 许可证。

# Infrastructure 层 - 技术实现层

**技术实现层**：为 Domain 层提供技术实现，包含持久化、事件发布、缓存、文件服务。

## 目录结构

```
infrastructure/src/main/java/org/smm/archetype/infrastructure/
├── shared/             # 共享基础设施
│   ├── event/          # 事件发布（EventPublisher、EventRepository）
│   ├── dal/            # 数据访问层（MyBatis Plus 生成代码）
│   └── util/           # 工具类（SpringContextUtils、KryoSerializer）
├── platform/           # 平台基础设施
│   ├── file/           # 文件存储实现
│   └── search/         # 搜索实现（Elasticsearch）
└── {模块}/             # 业务模块
    └── persistence/    # Repository 实现 + Converter
```

## 关键查找

| 目标              | 位置                                                   |
|-----------------|------------------------------------------------------|
| Repository 实现   | `infrastructure/**/persistence/*RepositoryImpl.java` |
| Converter       | `infrastructure/**/*Converter.java`                  |
| Event Publisher | `infrastructure/shared/event/`                       |
| 缓存实现            | `infrastructure/platform/cache/`                     |

## 核心规则

### Repository 实现（NON-NEGOTIABLE）

| 规则   | 说明                                     |
|------|----------------------------------------|
| 转换工具 | 必须使用 MapStruct 进行 Domain ↔ DO 转换       |
| 枚举转换 | Domain ↔ DO 的枚举转换**必须**在 Converter 中完成 |
| 代码生成 | Mapper 和 DO 类使用 MyBatis Plus（Lambda查询） |

```java
@Mapper(componentModel = "spring")
public interface OrderConverter {
    @Mapping(target = "orderStatus", expression = "java(orderDO.getStatusEnum())")
    OrderAggr toDomain(OrderDO orderDO);
    
    @Mapping(target = "status", expression = "java(order.getStatus().getCode())")
    OrderDO toDO(OrderAggr order);
}
```

### 事件驱动架构（NON-NEGOTIABLE）

| 规则        | 说明                                            |
|-----------|-----------------------------------------------|
| 事件发布      | 通过 `EventPublisher.publishEvent(DomainEvent)` |
| 重试策略      | 优先使用内置指数退避，必要时支持 XXL-JOB/PowerJob             |
| 幂等性       | 事件必须幂等，重复处理不得产生副作用                            |

**发布链路**：

```
Domain.recordEvent() 
    → DomainEventCollectPublisher (收集 + 持久化)
        → SpringDomainEventPublisher (发布)
```

### 中间件接入规范

| 场景        | 方式                     |
|-----------|------------------------|
| 简单接入（轻量级） | 直接实现 `*Client` 接口      |
| 复杂接入（多实现） | `Abstract*Client` 抽象基类 |

**条件化配置**：使用 `@ConditionalOnProperty` 按需加载

## 禁止

| ❌ 禁止                           | ✅ 正确                         |
|--------------------------------|------------------------------|
| Domain 层处理 DO ↔ Domain 转换      | 转换在 Converter 中完成            |
| Repository 直接操作数据库实体           | 使用 Mapper 操作，通过 Converter 转换 |
| 为抽象而抽象（简单场景创建 Abstract*Client） | 直接实现接口                       |
| 创建配置类                          | 配置类在 start 模块                |

---
← [项目知识库](../AGENTS.md) | **版本**: 3.3 | **更新**: 2026-02-19

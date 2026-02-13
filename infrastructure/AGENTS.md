# Infrastructure 层 - 技术实现层

**技术实现层**：为 Domain 层提供技术实现，包含持久化、事件发布、缓存、文件服务、中间件接入。

## 目录结构

```
infrastructure/src/main/java/org/smm/archetype/infrastructure/
├── bizshared/          # 共享基础设施
│   ├── event/          # 事件发布（EventPublisher、EventRepository）
│   ├── dal/            # 数据访问层（MyBatis-Flex 生成代码）
│   ├── log/            # 日志服务（LogAspect、Log、MyLog）
│   ├── util/           # 工具类（SpringContextUtils、KryoSerializer）
│   └── retry/          # 重试策略（指数退避、外部调度）
├── common/             # 通用基础设施
│   ├── cache/          # 缓存实现（Caffeine/Redis）
│   ├── file/           # 文件存储实现
│   ├── search/         # 搜索实现（Elasticsearch）
│   └── notification/   # 通知服务（SMS/Email）
└── {模块}/             # 业务模块
    └── persistence/    # Repository 实现 + Converter
```

## 关键查找

| 目标              | 位置                                                   | 说明                       |
|-----------------|------------------------------------------------------|--------------------------|
| Repository 实现   | `infrastructure/**/persistence/*RepositoryImpl.java` | 实现仓储接口                   |
| Converter       | `infrastructure/**/*Converter.java`                  | MapStruct Domain ↔ DO 转换 |
| Event Publisher | `infrastructure/bizshared/event/`                    | 事件发布器                    |
| 缓存实现            | `infrastructure/common/cache/`                       | Caffeine/Redis 实现        |
| 日志服务            | `infrastructure/bizshared/log/`                      | LogAspect、日志工具           |

## 核心规则

### Repository 实现规范

| 规则   | 说明                                     |
|------|----------------------------------------|
| 转换工具 | 必须使用 MapStruct 进行 Domain ↔ DO 转换       |
| 枚举转换 | Domain ↔ DO 的枚举转换**必须**在 Converter 中完成 |
| 代码生成 | Mapper 和 Table 类使用 MyBatis-Flex 代码生成器  |
| 依赖注入 | 使用构造函数注入                               |

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
| 事件 DTO 命名 | `{Event}DTO`（如 `OrderCreatedEventDTO`）        |
| 事件发布      | 通过 `EventPublisher.publishEvent(DomainEvent)` |
| 重试策略      | 优先使用内置指数退避，必要时支持 XXL-JOB/PowerJob             |
| 幂等性       | 事件必须幂等，重复处理不得产生副作用                            |

### 中间件接入规范

**接入方式选择**：

| 场景               | 方式                     | 示例                    |
|------------------|------------------------|-----------------------|
| 简单接入（轻量级、API 简单） | 直接实现 `*Client` 接口      | Search（Elasticsearch） |
| 复杂接入（多实现、统一流程）   | `Abstract*Client` 抽象基类 | SMS、Email、Cache       |

**禁止过度设计**：

- 简单场景禁止创建抽象层
- 抽象层代码行数超过实现类 50% 时需重新评估

**条件化配置**：

- 使用 `@ConditionalOnProperty` 按需加载
- 本地实现标记 `@Primary`，外部实现条件化覆盖

### 日志服务规范

| 规则   | 说明                                               |
|------|--------------------------------------------------|
| 日志框架 | SLF4J + `@Slf4j` 注解                              |
| 日志格式 | `[类型] 类#方法 \| 业务描述 \| 耗时ms \| 线程 \| 入参 \| 出参/错误` |
| 日志级别 | DEBUG（调试）、INFO（业务操作）、WARN（边界情况）、ERROR（异常）        |
| 敏感信息 | 密码、token、身份证号等必须脱敏                               |
| 截断   | JSON 序列化最大 2048 字符，超出标记 `...(truncated)`         |

### 中间件引入评估清单

新增中间件前必须完成：

1. 技术选型理由（性能、成熟度、社区支持）
2. 与现有架构的兼容性评估
3. 运维成本评估（部署复杂度、监控能力）
4. 团队能力评估（学习曲线、维护能力）

## 反模式（禁止）

| ❌ 禁止                           | ✅ 正确做法                       |
|--------------------------------|------------------------------|
| Domain 层处理 DO ↔ Domain 转换      | 转换在 Converter 中完成            |
| Repository 直接操作数据库实体           | 使用 Mapper 操作，通过 Converter 转换 |
| 为抽象而抽象（简单场景创建 Abstract*Client） | 直接实现接口                       |
| 使用 `spring-boot-starter` 引入依赖  | 手动指定具体依赖                     |

## 常见任务

| 任务            | 步骤                                                                                          |
|---------------|---------------------------------------------------------------------------------------------|
| 新增 Repository | 1. 创建 `*Converter`（MapStruct）<br>2. 创建 `*RepositoryImpl` 实现接口<br>3. 在 start 模块通过 `@Bean` 组装 |
| 新增事件发布        | 使用 `EventPublisher.publishEvent(DomainEvent event)`                                         |
| 新增中间件         | 1. 完成评估清单<br>2. 在根 POM 管理版本<br>3. 使用 `@ConditionalOnProperty` 按需加载                          |

---
**版本**: 2.0 | **整合自**: CONSTITUTION.md §IV/§X/§XXII/§XXIII

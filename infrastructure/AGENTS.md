# INFRASTRUCTURE 层

技术实现层 - 仓储适配器、外部服务集成、横切关注点。

## 概述

- **职责**：Repository 实现、事件发布、缓存/搜索/OSS 服务
- **依赖规则**：依赖 Domain 层接口，被 Adapter 层调用
- **关键特点**：中间件自动检测、MyBatis-Flex ORM、事件驱动支持

## 结构
```
infrastructure/
├── bizshared/       # 共享基础设施
│   ├── dal/         # MyBatis-Flex 生成代码
│   │   └── generated/
│   │       ├── mapper/    # Mapper 接口
│   │       └── entity/    # 数据库实体（DO）
│   ├── event/       # 事件发布
│   │   ├── publisher/     # SpringEventPublisherImpl, KafkaEventPublisherImpl
│   │   ├── retry/         # ExponentialBackoffRetryStrategy, ExternalSchedulerRetryStrategy
│   │   └── persist/       # 领域事件持久化（Kryo 序列化）
│   ├── client/      # 客户端实现
│   │   ├── cache/         # CacheClient（Caffeine/Redis）
│   │   ├── notification/  # NotificationClient（阿里云短信/邮件）
│   │   └── oss/           # OssClient（本地/RustFS）
│   └── util/        # 工具类（SpringContext、Kryo）
├── common/          # 通用技术服务
│   ├── cache/       # CaffeineCacheClientImpl, RedisCacheClientImpl
│   ├── file/        # LocalOssServiceImpl, RustFsOssServiceImpl
│   ├── log/         # 日志持久化
│   ├── notification/ # 阿里云短信/邮件
│   └── search/      # Elasticsearch 服务
└── _example/        # 示例实现
    └── order/
        └── persistence/
            └── OrderAggrRepositoryImpl
```

## 关键位置

| 任务     | 位置                                                 | 备注                          |
|--------|----------------------------------------------------|-----------------------------|
| 仓储实现   | infrastructure/**/persistence/*RepositoryImpl.java | MyBatis-Flex + MapStruct 转换 |
| Mapper | infrastructure/**/dal/generated/mapper/            | MyBatis-Flex 生成             |
| 事件发布   | infrastructure/bizshared/event/publisher/          | Spring/Kafka 自动检测           |
| 缓存服务   | infrastructure/common/cache/                       | Caffeine/Redis 自动检测         |
| OSS 服务 | infrastructure/common/file/                        | 本地/RustFS 自动检测              |
| 搜索服务   | infrastructure/common/search/                      | Elasticsearch               |

## 约定（项目特有）

- 实现接口：Domain 层定义的 Repository 接口
- 自动检测：`@ConditionalOnBean` 检测 Redis/Kafka/RustFS Bean
- 转换器：MapStruct 的 `*BusinessConverter` 做 DO ↔ Domain 映射
- 事件持久化：Kryo 序列化到数据库
- 禁止：配置类（必须在 start/）

## MyBatis-Flex 集成

```java
// Repository 实现
public class OrderAggrRepositoryImpl implements OrderAggrRepository {
    private final OrderMapper orderMapper;
    private final OrderBusinessConverter converter;

    @Override
    public void save(OrderAggr order) {
        OrderDO orderDO = converter.toDO(order);
        orderMapper.insertOrUpdate(orderDO);
    }

    @Override
    public OrderAggr findById(OrderId id) {
        OrderDO orderDO = orderMapper.selectById(id.getValue());
        return converter.toDomain(orderDO);
    }
}
```

## 事件发布策略

- **Spring Events**（默认）：内存事件总线
- **Kafka**：检测到 `KafkaTemplate` Bean 时自动切换
- **重试策略**：指数退避、外部调度器（XXL-JOB/PowerJob）

## 反模式

- ❌ 业务逻辑（委托给 Domain）
- ❌ Adapter 直接访问 Domain（通过 App 层）
- ❌ 配置类（必须在 start/config/）
- ❌ 仓储接口定义（在 Domain 层）

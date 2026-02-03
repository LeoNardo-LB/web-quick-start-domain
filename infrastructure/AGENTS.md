# INFRASTRUCTURE 层

技术实现层 - 仓储适配器、外部服务集成、横切关注点。

## 概述

职责：Repository 实现、事件发布、缓存/搜索/OSS 服务。
依赖规则：依赖 Domain 层接口，被 Adapter 层调用。
规模：60+ Java 文件，技术栈集成密集。

## 结构

```
infrastructure/
├── bizshared/       # 共享基础设施
│   ├── dal/generated/  # MyBatis-Flex 生成代码（禁止编辑）
│   ├── event/         # 事件发布（Spring/Kafka publisher, retry, persist）
│   ├── client/        # 客户端实现（Cache, Notification, OSS）
│   └── util/          # 工具类（SpringContext, Kryo）
├── common/          # 通用技术服务（cache, file, log, notification, search）
└── _example/        # 示例实现
```

## 关键位置

| 任务     | 位置                                                 | 备注                          |
|--------|----------------------------------------------------|-----------------------------|
| 仓储实现   | infrastructure/**/persistence/*RepositoryImpl.java | MyBatis-Flex + MapStruct 转换 |
| Mapper | infrastructure/**/dal/generated/mapper/            | MyBatis-Flex 生成             |
| 事件发布   | infrastructure/bizshared/event/publisher/          | Spring/Kafka 自动检测           |
| 缓存服务   | infrastructure/common/cache/                       | Caffeine/Redis 自动检测         |
| OSS 服务 | infrastructure/common/file/                        | 本地/RustFS 自动检测              |

## 约定

实现接口：Domain 层定义的 Repository 接口。
自动检测：`@ConditionalOnBean` 检测 Redis/Kafka/RustFS Bean。
转换器：MapStruct `*BusinessConverter` 做 DO ↔ Domain 映射。
事件持久化：Kryo 序列化。
禁止：配置类（必须在 start/）。

## 反模式

❌ 业务逻辑、Adapter 直接访问 Domain、配置类、仓储接口定义。

## 事件发布策略

- **Spring Events**（默认）：内存事件总线
- **Kafka**：检测到 `KafkaTemplate` Bean 时自动切换
- **重试策略**：指数退避、外部调度器（XXL-JOB/PowerJob）

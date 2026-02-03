# START 模块

Spring Boot 启动模块 - Bean 装配、配置、入口点。

## 概述

职责：Bean 组装、配置类管理、应用入口。
依赖规则：依赖所有其他模块，无业务逻辑。
规模：20+ Java 文件，所有配置类集中。

## 结构

```
start/
├── config/          # 所有 @Configuration 类（Bean 装配中心）
│   ├── AppConfigure.java          # 应用层 Bean
│   ├── DomainEventConfigure.java  # 事件发布/消费
│   ├── EventKafkaConfigure.java  # Kafka 事件配置
│   ├── CacheConfigure.java       # 缓存客户端选择
│   ├── SearchConfigure.java      # Elasticsearch
│   ├── OssConfigure.java        # 对象存储
│   ├── NotificationConfigure.java # 短信/邮件
│   ├── ThreadPoolConfigure.java  # 线程池（IO/CPU/Daemon）
│   ├── ScheduleConfigure.java    # 定时任务配置
│   ├── WebConfigure.java        # Web 配置
│   ├── LogConfigure.java         # 日志配置
│   ├── properties/              # @ConfigurationProperties
│   └── condition/               # @Conditional 条件类
└── ApplicationBootstrap.java      # 主类，CommandLineRunner
```

## 关键位置

| 任务      | 位置                                            | 备注                       |
|---------|-----------------------------------------------|--------------------------|
| 主入口     | start/.../ApplicationBootstrap.java           | Spring Boot 主类           |
| Bean 装配 | start/src/main/java/org/smm/archetype/config/ | 所有 *Configure 类          |
| 配置属性    | start/.../config/properties/*.java            | @ConfigurationProperties |
| 条件逻辑    | start/.../config/condition/*.java             | 阿里云启用条件                  |

## 约定

所有 `@Configuration` 类必须在这里（其他模块禁止）。
命名：`{Aggregate}Configure`（非标准，有意为之）。
Bean 组装：仅通过 `@Bean` 方法。
注入：`@RequiredArgsConstructor` 构造器注入。
跨配置依赖：`@Bean` 方法参数。
循环依赖：Optional/`@ConditionalOnBean`（禁止 `@Lazy`）。

## 反模式

❌ adapter/infrastructure 模块中的配置类、`@Lazy`/`ObjectProvider`、`@Component` 扫描 Bean、命名使用 `*Config`（应使用 `*Configure`）。

## 架构偏差

**⚠️ P1 - 配置类位置偏差**

- `start/exampleorder/OrderConfigure.java` 在 `start/` 下但不在 `config/` 包下
- **影响**：不符合"配置类集中在 config/"的约定
- **建议**：移至 `start/src/main/java/.../config/example/OrderConfigure.java`
# ADAPTER 层

接口适配层 - REST 控制器、事件监听器、定时任务。

## 概述

职责：对外接口、事件监听、定时调度。
依赖规则：依赖 App 层，与 Infrastructure 无直接依赖。
规模：50+ Java 文件，接口密集。

## 结构

```
adapter/
├── _example/        # 示例控制器（web/api/OrderController）
├── access/          # 接入层
│   ├── listener/    # 事件监听器（SpringEventListener, KafkaEventListener）
│   └── schedule/    # 事件重试调度器（ExponentialBackoffRetryStrategy, ExternalSchedulerRetryStrategy）
├── web/api/         # REST 端点
├── bizshared/       # 共享适配器工具（enums, util）
└── config/          # Web 配置（仅 Advice/Filter，非 @Configuration）
```

## 关键位置

| 任务           | 位置                                  | 备注                        |
|--------------|-------------------------------------|---------------------------|
| REST 控制器     | adapter/**/web/api/*Controller.java | 端点定义，委托给 App 层            |
| 事件监听器        | adapter/access/listener/            | Spring Events & Kafka 消费者 |
| 重试调度器        | adapter/access/schedule/            | 事件重试，多种策略                 |
| Request DTO  | adapter/**/dto/request/             | 输入验证                      |
| Response DTO | adapter/**/dto/response/            | 输出格式化                     |

## 约定

无业务逻辑：委托给 Application 层。
控制器：`@RestController`，返回 `Response<T>` 包装器。
事件监听：`@ConditionalOnBean` 自动检测（Kafka/Spring）。
重试策略：指数退避、外部调度器（XXL-JOB/PowerJob）。
禁止：配置类（必须在 start/config/）。

## 事件监听器

- **SpringEventListener**：内存事件总线
- **KafkaEventListener**：Kafka 消息队列（检测到 `KafkaTemplate` 时启用）
- 自动注册：`@ConditionalOnBean` 控制启用

## 重试调度器

- **ExponentialBackoffRetryStrategy**：指数退避策略
- **ExternalSchedulerRetryStrategy**：外部调度器（XXL-JOB、PowerJob、SchedulerX）

## 反模式

❌ 业务规则在 Controller、直接调用 Repository、配置类、DTO 中包含 Domain 对象。

## 架构偏差

**⚠️ P1 - @Component 使用违反 Bean 装配规范**

- `adapter/schedule/` 下的 RetryStrategy 使用了 `@Component`
- **影响**：偏离显式 Bean 装配原则，可能导致循环依赖
- **建议**：移至 `start/config/` 并通过 `@Bean` 方法装配

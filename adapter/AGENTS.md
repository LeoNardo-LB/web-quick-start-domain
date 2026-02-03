# ADAPTER 层

接口适配层 - REST 控制器、事件监听器、定时任务。

## 概述

- **职责**：对外接口、事件监听、定时调度
- **依赖规则**：依赖 App 层，与 Infrastructure 无直接依赖
- **关键特点**：无业务逻辑、事件自动检测、多种重试策略

## 结构
```
adapter/
├── _example/        # 示例控制器
│   └── web/
│       └── api/     # OrderController
├── access/          # 接入层
│   ├── listener/    # 事件监听器
│   │   ├── SpringEventListener.java
│   │   └── KafkaEventListener.java
│   └── schedule/    # 事件重试调度器
│       ├── EventRetryScheduler.java
│       ├── ExponentialBackoffRetryStrategy.java
│       └── ExternalSchedulerRetryStrategy.java
├── web/             # Web 控制器
│   └── api/         # REST 端点
├── bizshared/       # 共享适配器工具
│   └── util/        # IpUtils 等
└── config/          # Web 配置（禁止，必须在 start/）
```

## 关键位置

| 任务           | 位置                                  | 备注                        |
|--------------|-------------------------------------|---------------------------|
| REST 控制器     | adapter/**/web/api/*Controller.java | 端点定义，委托给 App 层            |
| 事件监听器        | adapter/access/listener/            | Spring Events & Kafka 消费者 |
| 重试调度器        | adapter/access/schedule/            | 事件重试，多种策略                 |
| Request DTO  | adapter/**/dto/request/             | 输入验证                      |
| Response DTO | adapter/**/dto/response/            | 输出格式化                     |

## 约定（项目特有）

- **无业务逻辑**：委托给 Application 层
- 控制器：`@RestController`，返回 `Response<T>` 包装器
- 事件监听：`@ConditionalOnBean` 自动检测（Kafka/Spring）
- 重试策略：指数退避、外部调度器（XXL-JOB/PowerJob）
- DTO 转换：MapStruct 做 DTO ↔ App DTO 转换
- 禁止：配置类（必须在 start/config/）

## REST 控制器示例

```java

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderAppService orderAppService;

    @PostMapping
    public Response<OrderDTO> create(@RequestBody CreateOrderRequest request) {
        OrderId orderId = orderAppService.create(new CreateOrderCommand(request));
        return Response.success(orderAppService.queryById(orderId));
    }

    @GetMapping
    public Response<List<OrderDTO>> list(@RequestParam String customerId) {
        return Response.success(orderAppService.query(new OrderQuery(customerId)));
    }

}
```

## 事件监听器

- **SpringEventListener**：内存事件总线
- **KafkaEventListener**：Kafka 消息队列（检测到 `KafkaTemplate` 时启用）
- 自动注册：`@ConditionalOnBean` 控制启用

## 重试调度器

- **ExponentialBackoffRetryStrategy**：指数退避策略
- **ExternalSchedulerRetryStrategy**：外部调度器（XXL-JOB、PowerJob、SchedulerX）

## 反模式

- ❌ 业务规则在 Controller（移至 Domain）
- ❌ 直接调用 Repository（使用 AppService）
- ❌ 配置类（必须在 start/config/）
- ❌ DTO 中包含 Domain 对象（通过 App 层转换）

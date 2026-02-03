# START 模块

Spring Boot 启动模块 - Bean 装配、配置、入口点。

## 概述

- **职责**：Bean 组装、配置类管理、应用入口
- **依赖规则**：依赖所有其他模块，无业务逻辑
- **关键特点**：`*Configure` 命名、构造器注入、无 `@Lazy`

## 结构
```
start/
├── config/          # 所有 @Configuration 类
│   ├── AppConfigure.java          # 应用层 Bean
│   ├── EventConfigure.java        # 事件发布/消费
│   ├── CacheConfigure.java        # 缓存客户端选择
│   ├── SearchConfigure.java       # Elasticsearch
│   ├── OssConfigure.java         # 对象存储
│   ├── NotificationConfigure.java  # 短信/邮件
│   ├── ThreadPoolConfigure.java   # 线程池（IO/CPU/Daemon）
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

## 约定（项目特有）

- **所有 @Configuration 类必须在这里**（其他模块禁止）
- 命名：`{Aggregate}Configure`（非标准，有意为之）
- Bean 组装：仅通过 `@Bean` 方法
- 注入：`@RequiredArgsConstructor` 构造器注入
- 跨配置依赖：`@Bean` 方法参数
- 循环依赖：Optional/`@ConditionalOnBean`（禁止 `@Lazy`）

## Bean 装配示例

```java
@Configuration
@RequiredArgsConstructor
public class OrderConfigure {
    private final OrderAggrRepository orderRepository;

    @Bean
    public OrderAppService orderAppService() {
        return new OrderAppService(orderRepository);
    }

    @Bean
    public OrderController orderController(OrderAppService orderAppService) {
        return new OrderController(orderAppService);
    }
}
```

## 反模式

- ❌ adapter/infrastructure 模块中的配置类
- ❌ `@Lazy` 或 `ObjectProvider` 处理循环依赖（必须重构）
- ❌ `@Component` 扫描 Bean（使用 `@Bean`）
- ❌ 命名使用 `*Config`（应使用 `*Configure`）

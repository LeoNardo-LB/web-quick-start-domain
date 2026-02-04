# START 模块

Spring Boot 启动模块 - Bean 装配、配置、入口点。

## 文档导航

### 🔗 相关文档

| 文档                                             | 用途                | 读者      |
|------------------------------------------------|-------------------|---------|
| **[项目知识库](../AGENTS.md)**                      | 项目架构概览和架构偏差分析     | 开发者、架构师 |
| **[AI 开发指南](../CLAUDE.md)**                    | AI 开发元指南          | 开发者、AI  |
| **[项目 README](../README.md)**                  | 项目概览和快速开始         | 开发者、架构师 |
| [Domain 层指南](domain/AGENTS.md)                 | 领域层核心业务逻辑和约定      | 后端开发者   |
| [Infrastructure 层指南](infrastructure/AGENTS.md) | 基础设施层技术实现和约定      | 后端开发者   |
| [Application 层指南](app/AGENTS.md)               | 应用层 CQRS 和用例编排    | 后端开发者   |
| [Adapter 层指南](adapter/AGENTS.md)               | 接口层 REST 控制器和事件监听 | 后端开发者   |
| [Test 模块指南](test/AGENTS.md)                    | 测试模块测试规范和最佳实践     | 测试开发者   |

### 🔗 规格文档

- [验证流程指南](_docs/specification/业务代码生成(AI)流程.md)
- [业务代码编写规范](_docs/specification/业务代码编写规范.md)
- [测试代码编写与示例指南](_docs/specification/测试代码编写与示例指南.md)

### 🔗 业务文档

- [业务文档索引](_docs/business/README.md)

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

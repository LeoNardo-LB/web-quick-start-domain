# Start 模块 - 启动模块

**启动模块**：Spring Boot 入口、所有配置类集中管理、Bean 装配、线程池配置。

## 目录结构

```
start/src/main/java/org/smm/archetype/
├── ApplicationBootstrap.java          # Spring Boot 启动类
├── exampleorder/                       # 示例聚合配置
└── config/                            # 配置类（唯一位置）
    ├── AppConfigure.java              # 应用配置
    ├── CacheConfigure.java            # 缓存配置
    ├── EventConfigure.java            # 事件配置
    ├── EventKafkaConfigure.java       # Kafka 事件配置
    ├── ThreadPoolConfigure.java       # 线程池配置
    ├── WebConfigure.java              # Web 配置
    └── properties/                    # 配置属性类
        ├── CacheProperties.java
        ├── EventProperties.java
        └── ThreadPoolProperties.java
```

## 关键查找

| 目标    | 位置                                   | 说明                                                 |
|-------|--------------------------------------|----------------------------------------------------|
| 启动入口  | `ApplicationBootstrap.java`          | @SpringBootApplication                             |
| 配置类   | `config/*Configure.java`             | Bean 装配                                            |
| 属性类   | `config/properties/*Properties.java` | @ConfigurationProperties                           |
| 线程池配置 | `config/ThreadPoolConfigure.java`    | ioTaskExecutor、cpuTaskExecutor、virtualTaskExecutor |

## 核心规则

### 配置集中化原则（NON-NEGOTIABLE）

| 规则      | 说明                                                  |
|---------|-----------------------------------------------------|
| 唯一位置    | 所有 `@Configuration` 类必须位于 `start/.../config/`       |
| 命名规范    | 配置类使用 `{Aggregate}Configure` 格式（如 `OrderConfigure`） |
| Bean 组装 | 必须通过 `@Bean` 方法，**禁止 `@Component` 扫描**              |
| 依赖注入    | 配置类内部必须使用构造函数注入（`@RequiredArgsConstructor`）         |

**禁止**：

- ❌ 在 adapter/infrastructure 模块创建配置类
- ❌ 使用 `@Component` 扫描装配 Bean
- ❌ 在配置类中使用字段注入

```java
@Configuration
@RequiredArgsConstructor
public class OrderConfigure {
    private final OrderAggrRepository orderRepository;
    private final EventPublisher eventPublisher;
    
    @Bean
    public OrderAppService orderAppService() {
        return new OrderAppService(orderRepository, eventPublisher);
    }
}
```

### 依赖隔离原则（NON-NEGOTIABLE）

| 规则         | 说明                                          |
|------------|---------------------------------------------|
| 禁止 `@Lazy` | 绝对禁止使用 `@Lazy`、`ObjectProvider` 等延迟注入解决循环依赖 |
| 循环依赖       | 必须通过重构解决，不得使用变通方案                           |
| 同配置类 Bean  | 通过 `@Bean` 方法参数注入实现                         |

### 线程池配置原则（NON-NEGOTIABLE）

| 线程池                   | 用途                    | 拒绝策略             |
|-----------------------|-----------------------|------------------|
| `ioTaskExecutor`      | IO 密集型任务              | CallerRunsPolicy |
| `cpuTaskExecutor`     | CPU 密集型任务             | AbortPolicy      |
| `virtualTaskExecutor` | 高并发轻量级任务（JDK 25 虚拟线程） | -                |
| `daemonTaskExecutor`  | 低优先级后台任务              | -                |

**异步处理规则**：

- 必须使用 `@Async("指定线程池名称")`，**禁止使用默认线程池**
- 事件处理器默认使用 `@Async("virtualTaskExecutor")`

### 条件化配置加载原则（NON-NEGOTIABLE）

| 规则     | 说明                                                                 |
|--------|--------------------------------------------------------------------|
| 注解选择   | 统一使用 `@ConditionalOnProperty`                                      |
| 配置命名   | 使用点分层次结构（如 `middleware.event.retry`）                               |
| 策略选择模式 | 相同 `prefix` + `name` + 不同 `havingValue`，设置 `matchIfMissing = true` |
| 功能开关模式 | `name="enabled"` + `havingValue="true"`，不设置 `matchIfMissing`       |

**多实现优先级**：

- 本地组件标记 `@Primary`
- 外部实现使用 `@ConditionalOnProperty` 覆盖

```java
@Configuration
public class CacheConfigure {
    @Bean
    @Primary
    public CacheClient localCacheClient() {
        return new CaffeineCacheClient();
    }
    
    @Bean
    @ConditionalOnProperty(prefix = "middleware.cache", name = "type", havingValue = "redis")
    public CacheClient redisCacheClient() {
        return new RedisCacheClient();
    }
}
```

## 反模式（禁止）

| ❌ 禁止                                       | ✅ 正确做法                               |
|--------------------------------------------|--------------------------------------|
| adapter/infrastructure 创建 `@Configuration` | 移至 start/config 目录                   |
| 使用 `@Component` 装配 Bean                    | 在 start 模块通过 `@Bean` 方法组装            |
| 配置类字段注入                                    | 使用 `@RequiredArgsConstructor` 构造函数注入 |
| 使用 `@Lazy` 解决循环依赖                          | 重构解决循环依赖                             |
| `@Async` 不指定线程池                            | 必须指定线程池名称                            |

---
**版本**: 2.0 | **整合自**: CONSTITUTION.md §V/§VI/§XIII/§XX

# Start模块 README

## 1. 模块概述

Start（启动模块）是DDD项目的启动入口和配置管理中心。

### 核心理念

- **配置管理中心**：所有Bean配置集中在start模块
- **Bean装配中心**：负责创建和管理所有Bean
- **按聚合根命名**：配置类按聚合根命名（如OrderConfigure）
- **条件装配**：使用条件注解灵活控制Bean加载

### 关键特点

- 统一Bean配置（@Configuration + @Bean）
- 构造器注入（避免@Autowired）
- 条件装配（@ConditionalOnProperty、@ConditionalOnMissingBean）
- 配置属性管理（@ConfigurationProperties）
- 应用启动入口（ApplicationBootstrap）

### 架构定位

```
┌─────────────────────────────────────────┐
│         Start (启动模块) ★ 本模块       │
│  ┌──────────────────────────────────┐   │
│  │  config/                         │   │
│  │    ├─ _example/                  │   │
│  │    │   └─ OrderConfigure.java   │   │
│  │    ├─ EventConfigure.java        │   │
│  │    ├─ CacheConfigure.java        │   │
│  │    ├─ AppConfigure.java          │   │
│  │    └─ AdapterWebConfig.java      │   │
│  │  properties/                     │   │
│  │    ├─ EventProperties.java       │   │
│  │    ├─ CacheProperties.java       │   │
│  │    └─ KafkaProperties.java       │   │
│  │  ApplicationBootstrap.java       │   │
│  │  application.yaml                │   │
│  │  logback-spring.xml              │   │
│  └──────────────────────────────────┘   │
└─────────────────────────────────────────┘
```

---

## 2. 目录结构

```
start/
├── src/main/java/{groupId}/
│   ├── config/                       # 配置类
│   │   ├── _example/                 # 示例配置
│   │   │   └─ OrderConfigure.java   # 订单聚合根配置
│   │   ├── EventConfigure.java       # 事件发布配置
│   │   ├── CacheConfigure.java       # 缓存配置
│   │   ├── OssConfigure.java         # 对象存储配置
│   │   ├── ThreadPoolConfigure.java  # 线程池配置
│   │   ├── AppConfigure.java         # 应用服务配置
│   │   └── properties/               # 配置属性类
│   │       ├── EventProperties.java  # 事件配置属性
│   │       ├── CacheProperties.java  # 缓存配置属性
│   │       ├── KafkaProperties.java  # Kafka配置属性
│   │       ├── OssProperties.java    # OSS配置属性
│   │       └── RetryDelayProperties.java
│   │
│   └── ApplicationBootstrap.java     # 启动类
│
├── src/main/resources/
│   ├── application.yaml              # 主配置文件
│   ├── application-dev.yaml          # 开发环境配置
│   ├── application-prod.yaml         # 生产环境配置
│   └── logback-spring.xml            # 日志配置
│
└── README.md                          # 本文档
```

### 包结构说明

- **config/**：所有配置类，按业务领域或技术命名
- **config/_example/**：示例业务配置（如OrderConfigure）
- **config/properties/**：配置属性类，用于绑定application.yaml
- **ApplicationBootstrap.java**：Spring Boot启动类

---

## 3. 核心职责与边界

### 3.1 核心职责

**Bean配置和装配**

- 创建所有Bean（@Bean方法）
- 管理Bean依赖关系
- 配置Bean生命周期
- 处理循环依赖

**配置属性管理**

- 绑定application.yaml配置
- 提供类型安全的配置访问
- 支持多环境配置
- 配置验证

**应用启动入口**

- Spring Boot启动类
- 组件扫描配置
- 自动配置管理

### 3.2 能力边界

**✅ Start模块能做什么**

- 创建和管理所有Bean
- 配置Bean之间的依赖关系
- 绑定配置文件
- 条件装配Bean
- 处理循环依赖
- 启动Spring Boot应用

**❌ Start模块不能做什么**

- 包含业务逻辑
- 包含技术实现（如Repository实现）
- 被其他模块依赖

---

## 4. 关键组件类型

### 4.1 配置类（Configuration）

**作用**

- 配置Spring Bean
- 管理依赖关系
- 条件装配

**职责**

- 使用@Bean方法创建Bean
- 通过方法参数注入依赖
- 使用@Conditional*注解控制Bean加载

**编写要点**

1. 使用@Configuration注解
2. 按聚合根或技术命名
3. 使用@Bean方法定义Bean
4. 通过方法参数注入依赖
5. 构造器注入配置属性

**伪代码示例**

```java

@Configuration
public class XxxConfigure {

    // 依赖的Bean（通过构造器注入）
    private final XxxRepository  repository;
    private final EventPublisher eventPublisher;

    // 构造器注入
    public XxxConfigure(
            XxxRepository repository,
            EventPublisher eventPublisher
    ) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
    }

    // 配置应用服务
    @Bean
    public XxxAppService xxxAppService() {
        return new XxxAppService(repository, eventPublisher);
    }

    // 配置其他Bean
    @Bean
    public XxxOtherService xxxOtherService(XxxAppService appService) {
        // 通过方法参数注入同配置类内的Bean
        return new XxxOtherService(appService);
    }

}
```

**与其他组件协作**

- 管理应用服务、Repository实现、领域服务、事件处理器等Bean
- 通过方法参数注入依赖
- 配置属性通过构造器注入

**边界**

- **配置位置**：配置类放在start模块的config包下
- **命名规范**：按聚合根命名（如OrderConfigure），不按层命名（如AppConfigure）
- **不允许**：使用@Service、@Component等注解

---

### 4.2 条件装配

**作用**

- 根据配置或条件动态加载Bean
- 支持多实现切换
- 提供可选功能

**职责**

- 使用@Conditional*注解
- 根据配置属性决定是否加载Bean
- 支持用户自定义实现

**编写要点**

1. 使用@ConditionalOnProperty基于配置属性
2. 使用@ConditionalOnMissingBean允许用户自定义
3. 使用@ConditionalOnBean基于Bean存在性
4. matchIfMissing设置默认值

**伪代码示例**

```java
// 按配置属性条件装配
@Configuration
public class CacheConfigure {

    // Redis缓存实现（当cache.type=redis时）
    @Bean
    @ConditionalOnProperty(
            prefix = "cache",
            name = "type",
            havingValue = "redis"
    )
    public CacheClient redisCacheClient(RedisProperties properties) {
        return new RedisCacheClient(properties);
    }

    // Caffeine缓存实现（当cache.type=caffeine时，默认）
    @Bean
    @ConditionalOnProperty(
            prefix = "cache",
            name = "type",
            havingValue = "caffeine",
            matchIfMissing = true
    )
    public CacheClient caffeineCacheClient(CaffeineProperties properties) {
        return new CaffeineCacheClient(properties);
    }

}

// 允许用户自定义实现
@Configuration
public class XxxConfigure {

    // 当用户未提供自定义实现时，使用默认实现
    @Bean
    @ConditionalOnMissingBean(XxxRepository.class)
    public XxxRepository xxxRepository() {
        return new XxxRepositoryImpl();
    }

}
```

**边界**

- **条件互斥**：同一类型的Bean只能有一个被加载
- **默认值**：使用matchIfMissing设置默认行为
- **用户优先**：@ConditionalOnMissingBean允许用户自定义

---

### 4.3 配置属性类

**作用**

- 绑定application.yaml配置
- 提供类型安全的配置访问
- 支持嵌套配置

**职责**

- 使用@ConfigurationProperties绑定配置
- 提供getter/setter访问配置
- 支持默认值

**编写要点**

1. 使用@ConfigurationProperties注解
2. 使用@EnableConfigurationProperties启用
3. 提供getter/setter（Lombok的@Getter/@Setter）
4. 支持嵌套静态类
5. 提供默认值

**伪代码示例**

```java

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "middleware.cache")
public class CacheProperties {

    // 缓存类型（默认值）
    private final String type = "caffeine";

    // 嵌套配置
    private final Caffeine caffeine = new Caffeine();
    private final Redis    redis    = new Redis();

    // 嵌套配置类
    public static class Caffeine {

        private final Long     maximumSize      = 1000L;
        private final Duration expireAfterWrite = Duration.ofMinutes(10);
        // getter/setter
    }

    public static class Redis {

        private final String  host = "localhost";
        private final Integer port = 6379;
        // getter/setter
    }

}

// 在配置类中启用
@Configuration
@EnableConfigurationProperties({CacheProperties.class})
public class CacheConfigure {

    private final CacheProperties properties;

    public CacheConfigure(CacheProperties properties) {
        this.properties = properties;
    }

    @Bean
    public CacheClient cacheClient() {
        // 使用配置属性
        Long maxSize = properties.getCaffeine().getMaximumSize();
        return new CaffeineCacheClient(maxSize);
    }

}
```

**在配置文件中使用**

```yaml
middleware:
  cache:
    type: redis  # 可选：caffeine、redis
    caffeine:
      maximum-size: 1000
      expire-after-write: 10m
    redis:
      host: localhost
      port: 6379
```

**边界**

- **前缀规范**：使用middleware.{功能}作为前缀
- **类型安全**：提供明确的类型和默认值
- **验证**：使用@Validated进行配置验证

---

### 4.4 循环依赖解决

**作用**

- 通过重构避免循环依赖
- 使用构造器注入 + Optional处理可选依赖
- 使用@Bean方法参数注入同配置类内的依赖

**职责**

- 避免使用@Lazy、ObjectProvider、ApplicationContext.getBean()
- 通过重构代码解耦依赖
- 正确使用依赖注入模式

**编写要点**

**绝对禁止的解决方法**：

- ❌ 使用@Lazy注解
- ❌ 使用ObjectProvider延迟注入
- ❌ 使用ApplicationContext.getBean()依赖查找
- ❌ 使用@PostConstruct延迟初始化

**正确的解决方法**：

**方法1：同配置类内依赖 - 使用@Bean方法参数**

```java

@Configuration
public class XxxConfigure {

    // 第一个Bean
    @Bean
    public XxxService xxxService() {
        return new XxxService();
    }

    // 第二个Bean（通过方法参数注入第一个Bean）
    @Bean
    public YyyService yyyService(XxxService xxxService) {
        return new YyyService(xxxService);
    }

}
```

**方法2：跨配置类依赖 - 使用构造器注入 + Optional**

```java

@Configuration
public class XxxConfigure {

    private final YyyService yyyService; // 可能不存在

    // 使用Optional避免循环依赖
    public XxxConfigure(Optional<YyyService> yyyService) {
        this.yyyService = yyyService.orElse(null);
    }

    @Bean
    public XxxService xxxService() {
        return new XxxService(yyyService);
    }

}
```

**方法3：重构架构 - 提取公共接口**

```java
// 有循环依赖
public class ServiceA {

    private ServiceB serviceB;

}

public class ServiceB {

    private ServiceA serviceA;

}

// 重构：提取公共接口
public interface ServiceADependencies {

    void methodX();

}

public class ServiceA implements ServiceADependencies {
    // 不依赖ServiceB
}

public class ServiceB {

    private ServiceADependencies deps; // 依赖接口

}
```

**边界**

- **原则**：通过重构避免循环依赖
- **禁止**：使用@Lazy、ObjectProvider等workaround
- **正确**：使用构造器注入 + Optional或重构架构

---

## 5. 设计模式和原则

### 5.1 核心设计模式

**工厂模式**

- @Bean方法作为Bean工厂
- 封装Bean创建逻辑
- 管理Bean依赖关系

**策略模式**

- 条件装配支持多实现切换
- 根据配置选择不同策略
- 便于扩展

**依赖注入模式**

- 构造器注入
- 方法参数注入
- 避免字段注入

### 5.2 配置原则

**集中配置原则**

- 所有配置类集中在start模块
- 便于管理和查找
- 避免配置分散

**命名规范原则**

- 按聚合根命名（如OrderConfigure）
- 按技术命名（如EventConfigure）
- 不按层命名（❌AppConfigure、❌InfraConfigure）

**条件装配原则**

- 使用@Conditional*注解
- 支持多实现切换
- 允许用户自定义

---

## 6. 开发指南

### 6.1 创建新配置类

**步骤1：确定配置类位置和名称**

配置类放在start模块的config包下，按聚合根或技术命名。

**步骤2：创建配置类**

```java

@Configuration
public class BlogConfigure {
    // Bean定义
}
```

**步骤3：定义@Bean方法**

```java

@Bean
public BlogAppService blogAppService(
        BlogRepository repository,
        EventPublisher eventPublisher
) {
    return new BlogAppService(repository, eventPublisher);
}
```

**步骤4：添加条件装配（可选）**

```java

@Bean
@ConditionalOnProperty(prefix = "blog.audit", name = "enabled", havingValue = "true")
public BlogAuditService blogAuditService() {
    return new DefaultBlogAuditService();
}
```

### 6.2 配置属性管理

**步骤1：创建Properties类**

```java

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "middleware.xxx")
public class XxxProperties {

    private final String type = "default";
    // getter/setter
}
```

**步骤2：在配置类中启用**

```java

@Configuration
@EnableConfigurationProperties({XxxProperties.class})
public class XxxConfigure {
    // 使用配置属性
}
```

**步骤3：在application.yaml中配置**

```yaml
middleware:
  xxx:
    type: custom
```

### 6.3 循环依赖解决

**原则**：通过重构避免循环依赖

**绝对禁止**：

- ❌ 使用@Lazy注解
- ❌ 使用ObjectProvider延迟注入
- ❌ 使用ApplicationContext.getBean()依赖查找

**正确方法**：

- ✅ 重构架构、解耦依赖
- ✅ 跨配置类：使用构造器注入 + Optional
- ✅ 同配置类：使用@Bean方法参数注入

---

## 7. 配置说明

### 7.1 application.yaml

主配置文件：`src/main/resources/application.yaml`

```yaml
server:
  port: 8080

spring:
  application:
    name: web-quick-start-domain
  datasource:
    url: jdbc:mysql://localhost:3306/web_quick_start_domain
    username: root
    password: root

middleware:
  event:
    publisher:
      type: spring  # 可选：kafka、spring
  cache:
    type: caffeine  # 可选：caffeine、redis
```

### 7.2 多环境配置

**开发环境**：`application-dev.yaml`
**生产环境**：`application-prod.yaml`

启动时指定环境：

```bash
java -jar app.jar --spring.profiles.active=prod
```

---

## 8. 常见问题FAQ

### Q1: 为什么配置类在start模块？

**A**: 将所有配置类集中在start模块的原因：

1. **统一管理**：所有Bean配置集中管理，便于查找
2. **职责分离**：其他模块不包含配置，保持纯净
3. **避免循环依赖**：配置集中在start模块，避免跨模块配置
4. **启动入口**：start模块是启动入口，配置和启动在一起合理

**错误示例**：

```java
// ❌ 错误：在infrastructure模块配置Bean
@Configuration
public class OrderRepositoryConfigure {}
```

**正确示例**：

```java
// ✅ 正确：在start模块配置Bean
@Configuration
public class OrderConfigure {}
```

---

### Q2: 配置类如何命名？

**A**: 配置类命名规则：

1. **按聚合根命名**：`{聚合根}Configure`
    - 示例：`OrderAggr → OrderConfigure`

2. **按技术命名**：`{技术}Configure`
    - 示例：`EventPublisher → EventConfigure`

3. **通用配置**：直接按功能命名
    - 示例：`ThreadPoolConfigure`

**不按层命名**：

- ❌ AppConfigure
- ❌ InfraConfigure
- ❌ DomainConfigure

---

### Q3: 如何避免循环依赖？

**A**: 避免循环依赖的原则：

1. **重构架构、解耦依赖**：引入接口，解耦依赖
2. **跨配置类：使用构造器注入 + Optional**：处理可选依赖
3. **同配置类：使用@Bean方法参数注入**：方法参数注入

**绝对禁止**：

- ❌ 使用@Lazy注解
- ❌ 使用ObjectProvider延迟注入
- ❌ 使用ApplicationContext.getBean()依赖查找
- ❌ 使用@PostConstruct延迟初始化

**详细示例**：参见[4.4 循环依赖解决](#44-循环依赖解决)

---

### Q4: 什么时候使用条件装配？

**A**: 条件装配的使用场景：

1. **多种实现选择**：根据配置选择不同的实现
2. **可选功能**：功能可以启用或禁用
3. **用户自定义**：允许用户自定义实现

**示例**：

```java
// 场景1：根据配置选择实现
@ConditionalOnProperty(prefix = "cache", name = "type", havingValue = "redis")
public CacheService redisCacheService() {}

@ConditionalOnProperty(prefix = "cache", name = "type", havingValue = "caffeine")
public CacheService caffeineCacheService() {}

// 场景2：可选功能
@ConditionalOnProperty(prefix = "payment.stripe", name = "enabled", havingValue = "true")
public PaymentGateway stripePaymentGateway() {}

// 场景3：用户自定义
@ConditionalOnMissingBean(XxxRepository.class)
public XxxRepository xxxRepository() {}
```

---

### Q5: 如何配置多环境？

**A**: 多环境配置步骤：

1. **创建环境配置文件**：
    - `application.yaml`（主配置）
    - `application-dev.yaml`（开发环境）
    - `application-prod.yaml`（生产环境）

2. **在主配置中指定profile**：
   ```yaml
   spring:
     profiles:
       active: dev  # 默认使用dev环境
   ```

3. **启动时指定环境**：
   ```bash
   java -jar app.jar --spring.profiles.active=prod
   ```

4. **环境特定配置**：
   ```yaml
   # application-dev.yaml
   middleware:
     cache:
       type: caffeine

   # application-prod.yaml
   middleware:
     cache:
       type: redis
   ```

---

## 9. 相关文档

- [项目根README.md](../README.md) - 项目整体架构说明
- [业务代码编写规范.md](../业务代码编写规范.md) - 编码标准详细参考
- [app/README.md](../app/README.md) - 应用层开发指南
- [infrastructure/README.md](../infrastructure/README.md) - 基础设施层开发指南
- [domain/README.md](../domain/README.md) - 领域层开发指南

---

**文档版本**: v2.0 (概念指导版)
**最后更新**: 2026-01-13
**维护者**: Leonardo

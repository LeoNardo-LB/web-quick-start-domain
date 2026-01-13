# Infrastructure模块 README

## 1. 模块概述

Infrastructure（基础设施层）是DDD四层架构的技术实现层，负责实现领域层定义的接口。

### 核心理念

- **实现领域接口**：实现Domain层定义的仓储接口、端口接口
- **技术细节隔离**：将技术实现细节（如数据库操作、外部服务调用）隔离在Infrastructure层
- **适配器模式**：使用适配器模式集成外部系统
- **DO转换**：负责Domain对象与DO（Data Object）之间的转换

### 关键特点

- Repository实现（数据持久化）
- 外部服务适配（Adapter）
- 中间件集成（缓存、事件、存储）
- MapStruct转换器（DO ↔ Domain）
- MyBatis-Flex自动生成代码

### 架构定位

```
┌─────────────────────────────────────────┐
│          Adapter (接口层)                │
├─────────────────────────────────────────┤
│         Application (应用层)             │
├─────────────────────────────────────────┤
│          Domain (领域层)                 │
│  ┌──────────────────────────────────┐   │
│  │  Repository接口                 │   │
│  │  端口接口（EmailService等）      │   │
│  └──────────────────────────────────┘   │
├─────────────────────────────────────────┤
│      Infrastructure (基础设施层) ★ 本模块│
│  ┌──────────────────────────────────┐   │
│  │  RepositoryImpl (仓储实现)       │   │
│  │  MapStruct Converter (转换器)    │   │
│  │  Adapter (适配器)                │   │
│  │  EventPublisher (事件发布)       │   │
│  │  CacheService (缓存服务)         │   │
│  └──────────────────────────────────┘   │
└─────────────────────────────────────────┘
```

---

## 2. 目录结构

```
infrastructure/
├── src/main/java/{groupId}/infrastructure/
│   ├── _shared/                      # 共享基础
│   │   ├── event/                    # 事件发布
│   │   │   ├── publisher/            # 事件发布器
│   │   │   └── TransactionEventPublishingAspect.java
│   │   ├── client/                   # 客户端封装
│   │   │   └── cache/               # 缓存客户端
│   │   │       └── AbstractCacheClient.java
│   │   └── generated/               # MyBatis-Flex生成代码
│   │       └── repository/           # 自动生成
│   │           ├── entity/           # DO实体
│   │           └── mapper/           # Mapper接口
│   │
│   ├── _example/                     # 示例业务（订单模块）
│   │   └── order/
│   │       ├── persistence/          # 持久化
│   │       │   ├── OrderAggrRepositoryImpl.java # 仓储实现
│   │       │   └── converter/        # MapStruct转换器
│   │       │       └── OrderAggrConverter.java
│   │       └── adapter/             # 适配器
│   │           └── MockInventoryServiceAdapter.java
│   │
│   └── common/                       # 通用实现
│       ├── file/                     # 文件服务实现
│       └── notification/            # 通知服务实现
│
└── README.md                          # 本文档
```

### 包结构说明

- **_shared/**：共享基础组件，所有业务模块通用
- **_example/**：示例业务模块，演示Infrastructure层开发实践
- **common/**：通用业务实现，可在实际项目中使用
- **persistence/**：Repository实现和DO转换器
- **adapter/**：外部服务适配器实现
- **generated/**：MyBatis-Flex自动生成的代码

---

## 3. 核心职责与边界

### 3.1 核心职责

**数据持久化**

- 实现Domain层定义的Repository接口
- Domain对象 ↔ DO转换
- 数据库CRUD操作
- 事务管理

**外部服务集成**

- 实现Domain层定义的端口接口
- 封装外部系统调用
- 异常转换
- 重试和降级

**中间件接入**

- 缓存（Caffeine、Redis）
- 事件发布（Spring Event、Kafka）
- 对象存储（OSS）
- 消息队列

**事件发布实现**

- 实现事件发布器接口
- 事务性事件发布
- 异步事件发布

### 3.2 能力边界

**✅ Infrastructure层能做什么**

- 实现Repository接口（数据持久化）
- DO ↔ Domain对象转换
- 实现端口接口（如EmailService、SmsService）
- 集成外部系统（如支付网关、库存服务）
- 中间件集成（缓存、消息队列、对象存储）
- 技术细节封装（数据库操作、HTTP调用）

**❌ Infrastructure层不能做什么**

- 业务逻辑（业务逻辑在Domain层）
- 用例编排（用例编排在Application层）
- HTTP处理（HTTP处理在Adapter层）
- 包含Controller、Controller注解
- 包含ApplicationService

---

## 4. 关键组件类型

### 4.1 Repository实现（RepositoryImpl）

**作用**

- 实现Domain层定义的Repository接口
- 负责Domain对象与DO的转换
- 协调多个Mapper

**职责**

- Domain ↔ DO双向转换
- 数据库CRUD操作
- 协调多个Mapper（一个聚合根可能对应多个Mapper）
- 管理事务边界

**编写要点**

1. 实现`XxxAggrRepository`接口
2. 构造器注入Mapper和Converter
3. 使用`@Transactional`管理事务
4. 只做转换，不包含业务逻辑

**伪代码示例**

```java
// Repository实现基本结构
public class XxxAggrRepositoryImpl implements XxxAggrRepository {

    private final XxxAggrMapper    mapper;       // MyBatis-Flex生成的Mapper
    private final XxxAggrConverter converter;   // DO转换器
    private final XxxItemMapper    itemMapper;     // 关联实体Mapper

    // 构造器注入
    public XxxAggrRepositoryImpl(
            XxxAggrMapper mapper,
            XxxAggrConverter converter,
            XxxItemMapper itemMapper
    ) {
        this.mapper = mapper;
        this.converter = converter;
        this.itemMapper = itemMapper;
    }

    // 保存聚合根
    @Override
    @Transactional
    public XxxAggr save(XxxAggr aggr) {
        // 1. Domain → DO
        XxxDO xxxDO = converter.toDO(aggr);

        // 2. 判断是新增还是更新
        if (aggr.isNew()) {
            mapper.insert(xxxDO);
        } else {
            mapper.update(xxxDO);
        }

        // 3. 保存关联数据
        saveRelatedData(aggr, xxxDO.getId());

        // 4. 返回领域对象
        return aggr;
    }

    // 查询聚合根
    @Override
    public Optional<XxxAggr> findById(XxxId id) {
        // 1. 查询DO
        XxxDO xxxDO = mapper.selectOneById(id);

        // 2. DO → Domain
        if (xxxDO == null) {
            return Optional.empty();
        }
        return Optional.of(toDomain(xxxDO));
    }

    // 条件查询
    @Override
    public List<XxxAggr> findByCondition(XxxQuery query) {
        // 1. 构建查询条件
        QueryWrapper queryWrapper = QueryWrapper.create()
                                            .where(XxxTable.XXX.STATUS.eq(query.getStatus()));

        // 2. 查询DO列表
        List<XxxDO> doList = mapper.selectListByQuery(queryWrapper);

        // 3. DO → Domain
        return doList.stream()
                       .map(this::toDomain)
                       .collect(Collectors.toList());
    }

    // 私有方法：DO转Domain
    private XxxAggr toDomain(XxxDO xxxDO) {
        // 使用反射或其他方式转换
        return converter.toDomain(xxxDO);
    }

}
```

**与其他组件协作**

- 实现Domain层定义的Repository接口
- 被ApplicationService调用
- 使用MyBatis-Flex的Mapper进行数据库操作
- 使用Converter进行DO转换

**边界**

- **只做转换**：不包含业务逻辑
- **不允许**：被Controller直接调用、包含业务规则

---

### 4.2 MapStruct转换器（Converter）

**作用**

- 使用MapStruct自动生成Domain ↔ DO转换代码
- 避免手动编写样板代码（减少70%-80%）
- 编译期类型安全检查

**职责**

- Domain对象转换为DO
- DO转换为Domain对象
- 处理值对象转换（如Money → BigDecimal）
- 处理枚举转换

**编写要点**

1. 使用 `@Mapper(componentModel = "spring")` 注解
2. 定义接口（MapStruct自动生成实现类）
3. 提供 `toDO()` 方法（Domain → DO）
4. 提供 `updateDO()` 方法（更新DO）
5. 提供 `toDomain()` 方法（DO → Domain，可选）
6. 使用 `@Mapping` 注解定义字段映射规则
7. 使用 `default` 方法实现自定义转换逻辑

**伪代码示例**

```java
// DO转换器基本结构（MapStruct方式）
@Mapper(
    componentModel = "spring",
    imports = {XxxStatus.class}
)
public interface XxxAggrConverter {

    // Domain → DO（用于新增）
    @Mapping(target = "amount", source = "money.amount")
    @Mapping(target = "status", expression = "java(aggr.getStatus().name())")
    XxxDO toDO(XxxAggr aggr);

    // 更新DO（用于修改）
    @Mapping(target = "amount", source = "money.amount")
    @Mapping(target = "status", expression = "java(aggr.getStatus().name())")
    @Mapping(target = "id", ignore = true)  // 不更新id
    @Mapping(target = "createTime", ignore = true)  // 不更新createTime
    void updateDO(XxxAggr aggr, @MappingTarget XxxDO xxxDO);

    // DO → Domain（可选）
    @InheritInverseConfiguration
    @Mapping(target = "money", expression = "java(Money.of(xxxDO.getAmount(), xxxDO.getCurrency()))")
    XxxAggr toDomain(XxxDO xxxDO);

    // 自定义枚举转换方法（default方法）
    default XxxStatus stringToXxxStatus(String status) {
        return status == null ? null : XxxStatus.valueOf(status);
    }

}
```

**实际使用示例**

```java
// 示例：OrderAggrConverter
@Mapper(
    componentModel = "spring",
    imports = {OrderStatus.class, PaymentMethod.class}
)
public interface OrderAggrConverter {

    /**
     * 领域对象转DO（用于新增）
     */
    @Mapping(target = "totalAmount", source = "totalAmount.amount")
    @Mapping(target = "status", expression = "java(order.getStatus().name())")
    @Mapping(target = "paymentMethod", expression = "java(order.getPaymentMethod().name())")
    OrderAggrDO toDO(OrderAggr order);

    /**
     * 更新DO（用于修改）
     */
    @Mapping(target = "totalAmount", source = "totalAmount.amount")
    @Mapping(target = "status", expression = "java(order.getStatus().name())")
    @Mapping(target = "paymentMethod", expression = "java(order.getPaymentMethod().name())")
    @Mapping(target = "id", source = "order.id")
    @Mapping(target = "createTime", source = "order.createTime")
    @Mapping(target = "updateTime", source = "order.updateTime")
    void updateDO(OrderAggr order, @MappingTarget OrderAggrDO orderDO);

}
```

**MapStruct常用注解**

| 注解                             | 说明                | 示例                                                           |
|--------------------------------|-------------------|--------------------------------------------------------------|
| `@Mapper`                      | 标记接口为MapStruct转换器 | `@Mapper(componentModel = "spring")`                         |
| `@Mapping`                     | 定义字段映射规则          | `@Mapping(target = "amount", source = "money.amount")`       |
| `@InheritInverseConfiguration` | 继承反向映射配置          | `@InheritInverseConfiguration`                               |
| `@MappingTarget`               | 标记更新方法的目标参数       | `void updateDO(Order order, @MappingTarget OrderDO orderDO)` |
| `@BeanMapping`                 | 定义Bean级别的映射规则     | `@BeanMapping(ignoreByDefault = true)`                       |

**Maven配置**

```xml
<!-- infrastructure/pom.xml -->
<properties>
    <mapstruct.version>1.6.4</mapstruct.version>
    <lombok-mapstruct-binding.version>0.2.0</lombok-mapstruct-binding.version>
</properties>

<dependencies>
<!-- MapStruct依赖 -->
<dependency>
    <groupId>org.mapstruct</groupId>
    <artifactId>mapstruct</artifactId>
    <version>${mapstruct.version}</version>
</dependency>
</dependencies>

<build>
<plugins>
    <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-compiler-plugin</artifactId>
        <configuration>
            <annotationProcessorPaths>
                <!-- Lombok -->
                <path>
                    <groupId>org.projectlombok</groupId>
                    <artifactId>lombok</artifactId>
                    <version>${lombok.version}</version>
                </path>
                <!-- MapStruct -->
                <path>
                    <groupId>org.mapstruct</groupId>
                    <artifactId>mapstruct-processor</artifactId>
                    <version>${mapstruct.version}</version>
                </path>
                <!-- Lombok + MapStruct 绑定 -->
                <path>
                    <groupId>org.projectlombok</groupId>
                    <artifactId>lombok-mapstruct-binding</artifactId>
                    <version>${lombok-mapstruct-binding.version}</version>
                </path>
            </annotationProcessorPaths>
        </configuration>
    </plugin>
</plugins>
</build>
```

**与其他组件协作**

- 被RepositoryImpl调用
- MapStruct自动生成实现类（如 `XxxAggrConverterImpl`）
- 自动注册为Spring Bean（`@Mapper(componentModel = "spring")`）
- 支持依赖注入（使用 `@Autowired` 或构造器注入）

**使用方式**

```java
// 在RepositoryImpl中注入Converter
@RequiredArgsConstructor
public class OrderAggrRepositoryImpl implements OrderAggrRepository {

    private final OrderAggrMapper orderAggrMapper;   // MyBatis Mapper
    private final OrderAggrConverter orderAggrConverter;  // MapStruct Converter（自动注入）

    @Override
    public OrderAggr save(OrderAggr order) {
        // 转换为DO
        OrderAggrDO orderDO = orderAggrConverter.toDO(order);

        // 保存到数据库
        orderAggrMapper.insert(orderDO);

        return order;
    }

    @Override
    public void update(OrderAggr order) {
        // 查询DO
        OrderAggrDO orderDO = orderAggrMapper.selectOneById(order.getId());

        // 更新DO
        orderAggrConverter.updateDO(order, orderDO);

        // 保存到数据库
        orderAggrMapper.update(orderDO);
    }
}
```

**边界**

- 只做转换，不包含业务逻辑
- MapStruct自动生成空值检查
- 不访问数据库
- 放在 `infrastructure/.../converter/` 包下

**优势**

1. **类型安全**：编译期生成代码，字段映射错误在编译期发现
2. **性能优异**：无反射开销，性能接近手动编写的代码
3. **代码简洁**：减少70%-80%的样板代码
4. **维护性高**：字段新增/修改时，MapStruct自动检测编译错误
5. **灵活性**：支持复杂映射（嵌套对象、集合、自定义转换）

---

### 4.3 适配器（Adapter）

**作用**

- 实现Domain层定义的端口接口
- 封装外部服务调用
- 异常转换

**职责**

- 调用外部API
- 参数转换（领域模型 → 外部请求格式）
- 结果转换（外部响应格式 → 领域模型）
- 异常转换（外部异常 → 领域异常）

**编写要点**

1. 实现Domain层定义的端口接口
2. 构造器注入外部服务客户端
3. 不使用@Component注解
4. 异常转换为领域异常

**伪代码示例**

```java
// Domain层定义的端口接口
public interface PaymentService {

    PaymentResult pay(PaymentRequest request);

}

// Infrastructure层的实现
public class StripePaymentAdapter implements PaymentService {

    private final RestTemplate     restTemplate;
    private final StripeProperties properties;

    public StripePaymentAdapter(
            RestTemplate restTemplate,
            StripeProperties properties
    ) {
        this.restTemplate = restTemplate;
        this.properties = properties;
    }

    @Override
    public PaymentResult pay(PaymentRequest request) {
        try {
            // 1. 转换为外部请求格式
            StripeRequest stripeRequest = toStripeRequest(request);

            // 2. 调用外部API
            StripeResponse response = restTemplate.postForObject(
                    properties.getApiUrl(),
                    stripeRequest,
                    StripeResponse.class
            );

            // 3. 转换为领域模型
            return toPaymentResult(response);

        } catch (RestClientException e) {
            log.error("调用Stripe支付失败", e);
            throw new PaymentException("支付失败", e);
        }
    }

    // 转换方法
    private StripeRequest toStripeRequest(PaymentRequest request) {
        return StripeRequest.builder()
                       .amount(request.getAmount().getValue())
                       .currency(request.getCurrency())
                       .build();
    }

    private PaymentResult toPaymentResult(StripeResponse response) {
        return PaymentResult.builder()
                       .success(response.isSuccess())
                       .transactionId(response.getTransactionId())
                       .build();
    }

}
```

**与其他组件协作**

- 实现Domain层定义的端口接口
- 被DomainService调用
- 配置为Bean（在start模块）

**边界**

- **只做适配**：不包含业务逻辑
- **异常转换**：将外部异常转为领域异常
- **不抛出外部异常**：转换为领域异常

---

### 4.4 中间件三层架构

**概念**

- 抽象层（Abstract）：定义统一接口和流程
- 实现层（Impl）：具体技术实现
- 配置层（Config）：条件装配和Bean配置

**职责分离**

**抽象层**：

- 定义统一接口
- 提供模板方法
- 统一处理流程

**实现层**：

- 具体技术实现
- 如Kafka、Redis、Spring Event

**配置层**：

- 条件装配
- Bean配置

**伪代码示例**

```java
// 第1层：抽象层
public abstract class AbstractCacheClient implements CacheClient {

    // 模板方法：定义流程
    @Override
    public final <T> T get(String key, Class<T> type) {
        T value = doGet(key, type);
        return value;
    }

    @Override
    public final void put(String key, Object value) {
        doPut(key, value);
    }

    // 抽象方法：子类实现
    protected abstract <T> T doGet(String key, Class<T> type);

    protected abstract void doPut(String key, Object value);

}

// 第2层：实现层
public class CaffeineCacheClient extends AbstractCacheClient {

    private final Cache<String, Object> cache;

    public CaffeineCacheClient(CaffeineProperties properties) {
        this.cache = Caffeine.newBuilder()
                             .maximumSize(properties.getMaximumSize())
                             .expireAfterWrite(properties.getExpireAfterWrite())
                             .build();
    }

    @Override
    protected <T> T doGet(String key, Class<T> type) {
        return (T) cache.getIfPresent(key);
    }

    @Override
    protected void doPut(String key, Object value) {
        cache.put(key, value);
    }

}

// 第3层：配置层（在start模块）
@Configuration
@ConditionalOnProperty(name = "cache.type", havingValue = "caffeine")
public class CacheConfigure {

    @Bean
    public CacheClient cacheClient(CacheProperties properties) {
        return new CaffeineCacheClient(properties.getCaffeine());
    }

}
```

**好处**

- 统一接口，便于切换实现
- 条件装配，灵活配置
- 模板方法，统一流程

**边界**

- 抽象层定义接口
- 实现层封装技术细节
- 配置层管理Bean

---

### 4.5 MyBatis-Flex自动生成代码

**作用**

- 自动生成DO实体
- 自动生成Mapper接口
- 避免手动编写重复代码

**职责**

- 根据DDL自动生成代码
- 提供基础的CRUD方法
- 支持条件构造器（QueryWrapper）

**使用方式**

```java
// DO实体（自动生成）
@Table("xxx_aggr")
public class XxxAggrDO {

    private Long   id;
    private String name;
    private String status;
    // getter/setter
}

// Mapper接口（自动生成）
public interface XxxAggrMapper extends BaseMapper<XxxAggrDO> {
    // 自动继承增删改查方法
    // selectOneById()
    // selectListByQuery()
    // insert()
    // update()
    // deleteByQuery()
}
```

**边界**

- 生成代码不修改
- DDL变更时重新生成
- 复杂查询在RepositoryImpl中实现

---

## 5. 设计模式和原则

### 5.1 核心设计模式

**仓储模式**

- 接口在领域层定义
- 实现在基础设施层
- 隐藏数据访问细节

**适配器模式**

- 实现端口接口
- 封装外部系统调用
- 异常转换

**模板方法模式**

- 中间件三层架构
- 抽象层定义流程
- 实现层填充细节

### 5.2 DDD原则应用

**依赖倒置**

- Domain层定义接口
- Infrastructure层实现接口
- 依赖接口而非实现

**技术隔离**

- 技术细节隔离在Infrastructure层
- Domain层不依赖框架
- 便于技术替换

---

## 6. 开发指南

### 6.1 实现Repository

**步骤1：定义Repository接口（Domain层）**

```java
public interface XxxAggrRepository extends BaseRepository<XxxAggr, XxxId> {

    Optional<XxxAggr> findByBusinessNo(String businessNo);

    List<XxxAggr> findByCondition(XxxQuery query);

}
```

**步骤2：创建RepositoryImpl**

```java
public class XxxAggrRepositoryImpl implements XxxAggrRepository {

    private final XxxAggrMapper    mapper;
    private final XxxAggrConverter converter;

    @Override
    public XxxAggr save(XxxAggr aggr) {
        // 持久化逻辑
    }

}
```

**步骤3：在start模块配置Bean**

```java

@Bean
public XxxAggrRepository xxxAggrRepository(...) {
    return new XxxAggrRepositoryImpl(...)
}
```

### 6.2 创建适配器

**步骤1：定义端口接口（Domain层）**

```java
public interface PaymentService {

    PaymentResult pay(PaymentRequest request);

}
```

**步骤2：实现适配器**

```java
public class StripePaymentAdapter implements PaymentService {

    @Override
    public PaymentResult pay(PaymentRequest request) {
        // 调用外部API
    }

}
```

**步骤3：配置Bean**

```java

@Bean
@ConditionalOnProperty(name = "payment.type", havingValue = "stripe")
public PaymentService paymentService(...) {
    return new StripePaymentAdapter(...)
}
```

---

## 7. 配置说明

Infrastructure层的配置类在start模块完成，详细配置说明见[start/README.md](../start/README.md)。

### 配置原则

1. **配置类位置**：配置类放在start模块的`config`包下
2. **按技术命名**：配置类按技术命名（如`EventConfigure`、`CacheConfigure`）
3. **使用@Bean方法**：使用`@Configuration` + `@Bean`模式
4. **条件装配**：使用`@ConditionalOnProperty`控制Bean加载

---

## 8. 常见问题FAQ

### Q1: DO对象和Domain对象的区别？

**A**:

| 特征 | DO对象（Data Object） | Domain对象（领域对象） |
|----|-------------------|----------------|
| 层级 | Infrastructure层   | Domain层        |
| 用途 | 数据库映射             | 业务逻辑封装         |
| 特点 | 包含数据库字段           | 包含业务方法         |
| 依赖 | 依赖MyBatis-Flex    | 不依赖框架          |
| 示例 | OrderAggrDO       | OrderAggr      |

---

### Q2: 为什么使用MapStruct？

**A**: MapStruct的优势：

1. **类型安全**：编译时生成代码，类型安全
2. **性能优秀**：无反射开销，直接调用
3. **简洁优雅**：自动生成转换代码
4. **易于维护**：重构友好，编译期检查

---

### Q3: 适配器的职责是什么？

**A**: 适配器的职责：

1. **实现端口接口**：实现Domain层定义的接口
2. **封装外部调用**：封装外部系统调用细节
3. **异常转换**：将外部异常转换为领域异常
4. **重试和降级**：处理重试逻辑和降级策略

---

### Q4: 如何实现条件装配？

**A**: 使用Spring的条件装配注解：

```java

@Bean
@ConditionalOnProperty(
        prefix = "middleware.event.publisher",
        name = "type",
        havingValue = "kafka"
)
public KafkaEventPublisher kafkaEventPublisher(...) {
    return new KafkaEventPublisher(...)
}
```

---

### Q5: 为什么不在Repository中写业务逻辑？

**A**: Repository只负责数据持久化，不负责业务逻辑：

- **职责单一**：Repository只负责数据访问
- **业务逻辑位置**：业务逻辑在Domain层的聚合根中
- **易于测试**：职责单一，更容易单元测试
- **符合DDD**：符合DDD的分层原则

---

## 9. 相关文档

- [项目根README.md](../README.md) - 项目整体架构说明
- [业务代码编写规范.md](../业务代码编写规范.md) - 编码标准详细参考
- [domain/README.md](../domain/README.md) - 领域层开发指南
- [app/README.md](../app/README.md) - 应用层开发指南
- [start/README.md](../start/README.md) - 启动模块配置指南

---

**文档版本**: v2.0 (概念指导版)
**最后更新**: 2026-01-13
**维护者**: Leonardo

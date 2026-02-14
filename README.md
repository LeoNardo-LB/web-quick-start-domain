# DDD Web Quick Start Domain

> **符合DDD规范的Maven骨架项目** - 快速启动高质量的企业级应用开发

[![Java](https://img.shields.io/badge/Java-25-orange.svg)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.2-green.svg)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

## 📋 目录

- [项目特色](#项目特色)
- [技术栈](#技术栈)
- [项目架构](#项目架构)
- [核心概念](#核心概念)
- [目录结构](#目录结构)
- [架构路线图](#架构路线图architecture-roadmap)
- [开发指南](#开发指南)
- [文档导航](#文档导航)
- [常见问题](#常见问题)

---

## 项目特色

本项目严格遵循DDD（领域驱动设计）原则，提供：

- ✅ **清晰的四层架构**：Domain → Application → Infrastructure → Adapter
- ✅ **完整的DDD基础设施**：聚合根、值对象、领域事件、仓储、规格模式
- ✅ **CQRS模式支持**：Command/Query分离，读写优化
- ✅ **事件驱动架构**：支持Kafka和Spring事件，灵活切换
- ✅ **丰富的示例代码**：订单模块75个类，涵盖所有DDD概念
- ✅ **完善的文档体系**：从入门到精通的完整指南

**DDD符合度评分**：⭐ 8.6/10

---

## 快速开始 ⭐

### 5分钟上手

```bash
# 1. 克隆项目
git clone <repository-url>
cd web-quick-start-domain

# 2. 编译项目
mvn clean compile

# 3. 运行测试
mvn test

# 4. 启动应用
mvn spring-boot:run -pl start

# 5. 访问API
curl http://localhost:9102/quickstart/api/orders
```

### 环境要求

- **JDK**: 25+（项目使用JDK 25）
- **Maven**: 3.8+
- **MySQL**: 8.0+

### 验证代码质量

```bash
# 1. 编译验证
mvn clean compile

# 2. 单元测试验证
mvn test

# 3. 启动验证（最关键）⭐
mvn test -Dtest=ApplicationStartupTests -pl test
```

详细流程：使用 `/tdd-workflow` 命令加载 TDD 验证流程

---

## 技术栈

| 分类       | 技术                | 版本          | 说明              |
|----------|-------------------|-------------|-----------------|
| **语言**   | Java              | 25          | 虚拟线程支持          |
| **核心框架** | Spring Boot       | 4.0.2       | 基础框架            |
| **持久层**  | MyBatis-Flex      | 1.11.5      | ORM框架           |
| **消息队列** | Kafka             | -           | 事件驱动（可选）        |
| **缓存**   | Redis             | -           | 分布式缓存           |
| **搜索**   | Elasticsearch     | -           | 全文搜索（可选）        |
| **工具库**  | Lombok            | latest      | 简化代码            |
| **工具库**  | MapStruct         | 1.5.5.Final | 对象映射            |
| **测试**   | JUnit 5 + Mockito | -           | 单元测试            |
| **测试**   | JaCoCo            | 0.8.14      | 代码覆盖率（支持JDK 25） |

---

## 项目架构

### 四层架构

```
┌─────────────────────────────────────────────────────────────┐
│                        Adapter 层                            │
│  (Controller、EventListener、Schedule、Request/Response DTO)  │
└────────────────────┬────────────────────────────────────────┘
                     │ 依赖
┌────────────────────▼────────────────────────────────────────┐
│                     Application 层                             │
│         (ApplicationService、CQRS、DTO转换、事务管理)          │
└────────────────────┬────────────────────────────────────────┘
                     │ 依赖
┌────────────────────▼────────────────────────────────────────┐
│                      Domain 层                               │
│     (聚合根、实体、值对象、领域事件、仓储接口、领域服务)         │
│                      ↕                                      │
│              (纯净业务逻辑，无外部依赖)                        │
└────────────────────┬────────────────────────────────────────┘
                     │ 接口
┌────────────────────▼────────────────────────────────────────┐
│                  Infrastructure 层                            │
│   (Repository实现、EventPublisher、CacheService、外部服务)      │
└─────────────────────────────────────────────────────────────┘
```

**依赖规则**：
- ✅ Adapter → Application → Domain ← Infrastructure
- ✅ Domain层无外部依赖，纯净的业务逻辑
- ✅ 接口在Domain层，实现在Infrastructure层

### 各层职责

| 层 | 职责 | 详解 |
|---|------|------|
| **Domain** | 领域模型 | 聚合根、实体、值对象、领域事件、仓储接口 |
| **Application** | 用例编排 | ApplicationService、CQRS、DTO转换、事务边界 |
| **Infrastructure** | 基础设施 | Repository实现、EventPublisher、CacheService |
| **Adapter** | 接口适配 | Controller、EventListener、Schedule |



---

## 核心概念

### DDD核心元素

#### 1. 聚合根（Aggregate Root）

**定义**：聚合根是领域模型中的一致性边界，负责维护业务规则。

**示例**：OrderAggr（订单聚合根）

```java
public class OrderAggr extends AggregateRoot<OrderAggr, OrderId> {
    private OrderId id;
    private List<OrderItem> items;
    private OrderStatus status;

    // 创建订单
    public static OrderAggr create(String customerId, Money totalAmount) {
        OrderAggr order = new OrderAggr();
        order.id = OrderId.generate();
        order.status = OrderStatus.CREATED;
        order.recordEvent(new OrderCreatedEvent(order.id));
        return order;
    }

    // 支付订单
    public void pay(PaymentMethod method) {
        if (this.status != OrderStatus.CREATED) {
            throw new IllegalStateException("只有已创建的订单可以支付");
        }
        this.status = OrderStatus.PAID;
        this.recordEvent(new OrderPaidEvent(this.id));
    }
}
```

#### 2. 值对象（Value Object）

**定义**：值对象通过属性值来标识，没有身份标识，不可变。

**示例**：Money（金额值对象）

```java
@AllArgsConstructor
@FieldDefaults(makeFinal = true, level = PRIVATE)
public class Money {
    private BigDecimal amount;
    private String currency;

    public Money add(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("币种不同");
        }
        return new Money(this.amount.add(other.amount), this.currency);
    }
}
```

#### 3. 领域事件（Domain Event）

**定义**：领域事件表示领域中发生的事情，用于实现事件驱动架构。

**示例**：OrderCreatedEvent

```java
public class OrderCreatedEvent extends DomainEvent {
    private OrderId orderId;

    @Override
    public String getEventTypeName() {
        return "OrderCreated";
    }
}
```

#### 4. 仓储（Repository）

**定义**：仓储负责聚合根的持久化和检索，隐藏数据访问细节。

**示例**：OrderAggrRepository

```java
public interface OrderAggrRepository {
    void save(OrderAggr order);  // 保存聚合根
    OrderAggr findById(OrderId id);  // 根据ID查找
    List<OrderAggr> findByCustomerId(String customerId);  // 查询
}
```

### 设计模式

#### CQRS（Command Query Responsibility Segregation）

**定义**：命令查询职责分离，将读写操作分离。

**示例**：

```java
// Command：创建订单
public class CreateOrderCommand {
    private String customerId;
    private Money totalAmount;
}

// Query：查询订单
public class OrderQuery {
    private String customerId;
}

// ApplicationService
public class OrderAppService {
    public OrderId create(CreateOrderCommand command) { ... }
    public List<OrderDTO> query(OrderQuery query) { ... }
}
```

#### Specification（规格模式）

**定义**：封装业务规则，可组合、可复用。

**示例**：

```java
public interface OrderSpecification extends Specification<OrderAggr> {
    // 可支付订单规格
    static OrderSpecification payable() {
        return order -> order.getStatus() == OrderStatus.CREATED;
    }
}

// 使用
List<OrderAggr> payableOrders = orderRepository.findAll()
    .stream()
    .filter(OrderSpecification.payable().toPredicate())
    .toList();
```

---

## 目录结构

```
web-quick-start-domain/
├── domain/              # 领域层（核心业务逻辑）
│   └── src/main/java/org/smm/archetype/domain/
│       ├── common/      # 通用领域对象
│       │   ├── file/    # 文件管理
│       │   ├── search/  # 搜索
│       │   └── event/   # 领域事件
│       ├── _example/    # 示例代码（订单模块75个类）
│       │   └── order/
│       └── _shared/     # 共享领域对象
│           ├── base/    # 领域对象基类
│           ├── client/  # 技术客户端接口
│           └── event/   # 事件相关
│
├── app/                 # 应用层（用例编排）
│   └── src/main/java/org/smm/archetype/app/
│       ├── _shared/     # 共享应用服务
│       │   ├── event/   # 事件处理器
│       │   └── query/   # 查询服务
│       └── _example/    # 示例代码
│           └── order/
│
├── infrastructure/      # 基础设施层（技术实现）
│   └── src/main/java/org/smm/archetype/infrastructure/
│       ├── common/      # 通用基础设施
│       │   ├── cache/   # 缓存实现
│       │   ├── file/    # 文件存储
│       │   ├── log/     # 日志服务
│       │   ├── notification/ # 通知服务
│       │   └── search/  # 搜索实现
│       ├── _shared/     # 共享基础设施
│       │   ├── event/   # 事件发布
│       │   ├── generated/ # MyBatis-Flex生成代码
│       │   └── retry/   # 重试策略
│       └── config/      # 配置类
│
├── adapter/             # 接口层（对外接口）
│   └── src/main/java/org/smm/archetype/adapter/
│       ├── _example/    # 示例代码
│       │   └── order/
│       │       └── web/api/  # Controller
│       ├── access/      # 接入层
│       │   ├── listener/  # 事件监听器
│       │   └── schedule/  # 定时任务
│       └── config/      # 配置类
│
├── start/               # 启动模块
│   └── src/main/java/org/smm/archetype/
│       ├── config/      # 配置类（Bean装配）
│       └── Application.java
│
├── test/                # 测试模块
│   ├── src/test/java/
│   │   ├── org/smm/archetype/test/
│   │   │   ├── base/    # 测试基类
│   │   │   ├── unit/    # 单元测试
│   │   │   └── integration/ # 集成测试
│   │   └── resources/   # 测试资源
│   └── pom.xml          # JaCoCo配置
│
├── openspec/             # OpenSpec 规范目录
│   ├── changes/         # 变更记录
│   └── specs/           # 功能规格
│
├── AGENTS.md            # 项目知识库（架构概览、模块指南）
├── README.md            # 本文件
└── pom.xml              # Maven配置
```

---

## 架构路线图（Architecture Roadmap）

以下模块是计划中但尚未实现的模块：

### 📦 Product 模块 [尚未实现]

**状态：** 计划中，代码示例仅作演示

**目标功能：**
- 产品管理的核心业务逻辑
- 产品创建、查询、更新、删除
- 产品库存管理

**示例代码：**

```java
// 创建聚合根
public class ProductAggr extends AggregateRoot<ProductAggr, ProductId> {
    // 业务逻辑
}

// 创建仓储接口
public interface ProductRepository {
    Product findById(ProductId id);
    void save(Product product);
}
```

> **注意**：上述代码仅为架构示例，实际实现尚未开始。

### 👤 User 模块 [尚未实现]

**状态：** 计划中，尚未开始设计

**目标功能：**
- 用户注册、登录
- 用户信息管理
- 用户权限管理

> **注意**：该模块目前仅处于规划阶段。

---

## 开发指南

### 开发新功能的步骤

#### 1. 创建领域模型（Domain层）

```java
// 1.1 创建聚合根
public class OrderAggr extends AggregateRoot<OrderAggr, OrderId> {
    private OrderId id;
    private List<OrderItem> items;
    private OrderStatus status;

    // 业务逻辑方法
    public static OrderAggr create(String customerId, Money totalAmount) {
        OrderAggr order = new OrderAggr();
        order.id = OrderId.generate();
        order.status = OrderStatus.CREATED;
        order.recordEvent(new OrderCreatedEvent(order.id));
        return order;
    }

    public void pay(PaymentMethod method) {
        if (this.status != OrderStatus.CREATED) {
            throw new IllegalStateException("只有已创建的订单可以支付");
        }
        this.status = OrderStatus.PAID;
        this.recordEvent(new OrderPaidEvent(this.id));
    }
}

// 1.2 创建实体和值对象
@ValueObject
public class Money {
    private BigDecimal amount;
    private String currency;
    // 值对象逻辑
}

// 1.3 创建仓储接口
public interface OrderAggrRepository {
    void save(OrderAggr order);
    OrderAggr findById(OrderId id);
    List<OrderAggr> findByCustomerId(String customerId);
}
```

> **详细规范**：[Domain 层指南](domain/AGENTS.md) - 领域层设计模式



#### 2. 实现应用服务（Application层）

```java
// 2.1 创建ApplicationService
@Configuration
public class OrderConfigure {
    @Bean
    public OrderAppService orderAppService(
        OrderAggrRepository orderRepository) {
        return new OrderAppService(orderRepository);
    }
}

// 2.2 实现用例编排
public class OrderAppService {
    public OrderId create(CreateOrderCommand command) {
        // 编排业务逻辑
        OrderAggr order = OrderAggr.create(command.getCustomerId(), command.getTotalAmount());
        orderRepository.save(order);
        return order.getId();
    }

    public List<OrderDTO> query(OrderQuery query) {
        // 查询逻辑
        return orderRepository.findByCustomerId(query.getCustomerId())
            .stream()
            .map(this::toDTO)
            .toList();
    }
}
```

> **详细规范**：[Start 模块指南](start/AGENTS.md) - 配置类命名和Bean装配



#### 3. 开发Controller（Adapter层）

```java
// 3.1 创建Controller
@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private OrderAppService orderAppService;

    @PostMapping
    public Response<OrderDTO> create(@RequestBody CreateOrderRequest request) {
        // 调用应用服务
        OrderId orderId = orderAppService.create(new CreateOrderCommand(request));
        return Response.success(orderAppService.queryById(orderId));
    }

    @GetMapping
    public Response<List<OrderDTO>> list(@RequestParam String customerId) {
        return Response.success(orderAppService.query(new OrderQuery(customerId)));
    }
}
```



#### 4. 实现Repository（Infrastructure层）

```java
// 4.1 实现仓储
public class OrderAggrRepositoryImpl implements OrderAggrRepository {
    private OrderMapper orderMapper;
    private OrderBusinessConverter converter;

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

    @Override
    public List<OrderAggr> findByCustomerId(String customerId) {
        List<OrderDO> orderDOList = orderMapper.selectByCustomerId(customerId);
        return converter.toDomainList(orderDOList);
    }
}
```



#### 5. 编写测试

```java
// 5.1 单元测试
class OrderAppServiceTest extends UnitTestBase {
    @Mock
    private OrderAggrRepository orderRepository;

    @Test
    void testCreateOrder() {
        // Given-When-Then
        CreateOrderCommand command = new CreateOrderCommand("customer123", new Money("100.00", "CNY"));
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        OrderId orderId = orderAppService.create(command);

        assertNotNull(orderId);
        verify(orderRepository, times(1)).save(any());
    }
}

// 5.2 集成测试
class OrderControllerTest extends IntegrationTestBase {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void testCreateOrderApi() throws Exception {
        String requestBody = "{\"customerId\":\"customer123\",\"totalAmount\":{\"amount\":\"100.00\",\"currency\":\"CNY\"}}";

        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }
}
```

> **详细规范**：[Test 模块指南](test/AGENTS.md) - 测试编写最佳实践



#### 6. 验证代码质量

```bash
# 6.1 编译验证
mvn clean compile

# 6.2 单元测试验证
mvn test

# 6.3 代码风格检查（Checkstyle）⭐
mvn checkstyle:check -Dcheckstyle.config.location=config/checkstyle/checkstyle.xml

# 6.4 启动验证（最关键）⭐
mvn test -Dtest=ApplicationStartupTests -pl test
```

**Checkstyle 代码风格检查**：

项目使用 Checkstyle 统一代码风格，包括：

- Javadoc 注释规范（类、方法必须有文档注释）
- 行长度限制（120 字符）
- 文件末尾换行
- 日志格式规范

配置文件位置：`config/checkstyle/checkstyle.xml`

详细流程：使用 `/tdd-workflow` 命令加载 TDD 验证流程

---

## 文档导航

### 📚 完整文档索引

| 文档                                                     | 用途                   | 读者      |
|--------------------------------------------------------|----------------------|---------|
| **[项目知识库](AGENTS.md)**                                 | 项目架构概览和架构偏差分析        | 开发者、架构师 |
| **[AI开发指南](CLAUDE.md)**                                | AI开发元指南（包含文档导航和快速参考） | 开发者、AI  |
| **[Maven Archetype使用指南](ARCHETYPE%20用法.md)**           | 快速生成基于DDD架构的Java项目骨架 | 开发者     |
| **TDD 验证流程** - 使用 `/tdd-workflow` 命令 | TDD验证流程          | 开发者、AI  |

### 📚 项目知识库（AGENTS.md）

| 文档                                                | 用途              | 读者      |
|---------------------------------------------------|-----------------|---------|
| **[项目知识库概览](AGENTS.md)**                          | 项目架构概览和架构偏差分析   | 开发者、架构师 |
| **[Domain层指南](domain/AGENTS.md)**                 | 领域层核心业务逻辑和约定    | 后端开发者   |
| **[Infrastructure层指南](infrastructure/AGENTS.md)** | 基础设施层技术实现和约定    | 后端开发者   |
| **[Application层指南](app/AGENTS.md)**               | 应用层CQRS和用例编排    | 后端开发者   |
| **[Adapter层指南](adapter/AGENTS.md)**               | 接口层REST控制器和事件监听 | 后端开发者   |
| **[Start模块指南](start/AGENTS.md)**                  | 启动模块Bean装配和配置   | 后端开发者   |
| **[Test模块指南](test/AGENTS.md)**                    | 测试模块测试规范和最佳实践   | 测试开发者   |

### 🎯 按角色查找文档

#### 初学者

1. 阅读 [项目知识库](AGENTS.md) - 编码规范
2. 参考订单示例代码 - 学习DDD概念

#### 有经验开发者

1. 阅读 [项目知识库](AGENTS.md) - 编码规范
2. 参考订单示例代码 - 学习DDD概念

#### 架构师
1. 阅读 [README.md](README.md) - 项目架构
2. 查看订单模块示例 - DDD实战

---

## 常见问题

### Q1: 如何快速上手？

**A**: 阅读 README.md - 项目架构和快速开始部分。

### Q2: 代码修改后如何验证？

**A**: 按照以下顺序验证：
```bash
mvn clean compile  # 编译验证
mvn test          # 单元测试验证
mvn test -Dtest=ApplicationStartupTests -pl test  # 启动验证
```

详细流程：使用 `/tdd-workflow` 命令加载 TDD 验证流程

### Q3: 如何解决循环依赖？

**A**: 绝对禁止使用@Lazy、ObjectProvider等，必须通过重构解决：
- 跨配置类：使用构造器注入 + Optional
- 同配置类：使用@Bean方法参数注入

参考：[Start 模块指南](start/AGENTS.md) - 依赖隔离原则

### Q4: 如何查看测试覆盖率？

**A**:
```bash
mvn verify -pl test
# 报告位置：test/target/site/jacoco/index.html
```

覆盖率要求：行≥95%，分支=100%

### Q5: 测试环境配置说明？

**A**: 测试环境使用独立配置，与生产环境解耦：

| 配置文件               | 位置                         | 说明         |
|--------------------|----------------------------|------------|
| `application.yaml` | `test/src/test/resources/` | H2 内存数据库配置 |
| `logback-test.xml` | `test/src/test/resources/` | 测试专用日志配置   |

**H2 数据库配置要点**：

- 使用 MySQL 兼容模式（`MODE=MySQL`）
- 启用大小写不敏感（`CASE_INSENSITIVE_IDENTIFIERS=TRUE`）
- 单元测试无需启动 Spring 上下文

**日志配置要点**：

- `logback-test.xml` 优先级高于 `logback-spring.xml`
- 不依赖 Spring Profile，单元测试直接可用

### Q6: 如何学习DDD？

**A**: 推荐阅读顺序：
1. [README.md](README.md) - 项目架构和快速开始部分
2. 订单示例代码 - 75个类，涵盖所有DDD概念

### Q6: 配置类应该放哪里？

**A**: **必须在start模块**的config包下：
```
start/src/main/java/org/smm/archetype/config/
├── OrderConfigure.java
└── ...
```

**命名规范**：使用 `{Aggregate}Configure` 格式，如 `OrderConfigure`。

**禁止**：在adapter/infrastructure模块创建配置类。

参考：[Start 模块指南](start/AGENTS.md) - 依赖隔离原则

### Q7: 如何使用外部调度框架（XXL-JOB、PowerJob）？

**A**: 使用项目内置的 `ExternalSchedulerRetryStrategy`：

**配置文件（application.yml）**：
```yaml
middleware:
  event:
    retry:
      strategy: external-scheduler  # 启用外部调度策略
      interval-minutes: 5           # 重试间隔（分钟）
```

**支持的框架**：
- XXL-JOB
- PowerJob
- SchedulerX
- 其他分布式任务调度框架

详细指南：参见 `infrastructure/_shared/event/retry/ExternalSchedulerRetryStrategy.java`

### Q8: 日志文件在哪里？如何配置？

**A**: 日志文件默认输出到项目内部的 `.logs` 隐藏文件夹：

```
.logs/
├── app.log              # 主日志（30天保留）
├── current.log          # 当前会话日志
├── error.log            # 错误日志
└── audit.log            # 审计日志（180天保留）
```

**配置文件位置**：

- `start/src/main/resources/application.yaml` - 日志路径和级别配置
- `start/src/main/resources/logback-spring.xml` - 日志格式和轮转策略

**关键特性**：

- ✅ 敏感信息自动脱敏（密码、token、手机号、身份证号、银行卡号、邮箱）
- ✅ 异步日志记录（队列大小2048，永不阻塞）
- ✅ 日志轮转（按日期和大小分割）
- ✅ 审计日志独立存储（180天保留，满足GDPR和等保要求）

详细指南：[日志配置快速开始](specs/1-fix-logging-output/quickstart.md)

---

## 🎯 总结

本项目提供了一个**符合DDD规范、生产就绪**的Maven骨架项目：

- ✅ **架构清晰**：四层架构，职责明确
- ✅ **文档完善**：从入门到精通的完整指南
- ✅ **示例丰富**：订单模块75个类
- ✅ **质量保障**：完善的测试和验证流程

**适合场景**：
- ⭐⭐⭐⭐⭐ 学习DDD
- ⭐⭐⭐⭐ 快速原型
- ⭐⭐ 企业级应用（需要根据业务调整）

**下一步**：

1. 阅读 [项目知识库](AGENTS.md) 学习各模块规范
2. 参考订单示例代码，开始开发

---

**文档版本**: v2.0
**最后更新**: 2026-02-14
**维护者**: Leonardo

# CLAUDE.md

> **AI 开发元指南** - 为 Claude Code (claude.ai/code) 在本仓库工作提供指导

## 项目概览

这是一个基于 **DDD（领域驱动设计）的 Maven archetype**，用于 Java 企业级应用，采用严格的四层架构。项目强制执行清晰架构原则、测试驱动开发和灵活的中间件集成。

**关键技术**：Java 25、Spring Boot 4.0.2、MyBatis-Flex 1.11.5、MapStruct 1.5.5.Final、虚拟线程

**DDD 符合度评分**：8.6/10

---

## 构建与测试命令

### 核心命令

```bash
# 编译（验证 MapStruct 代码生成）
mvn clean compile

# 运行所有测试
mvn test

# 启动验证（最关键 - 验证 Spring 上下文，检测循环依赖）
mvn test -Dtest=ApplicationStartupTests -pl test

# 运行单个测试类
mvn test -Dtest=OrderAppServiceTest

# 运行单个测试方法
mvn test -Dtest=OrderAppServiceTest#testCreateOrder

# 生成覆盖率报告（要求 95%+ 行覆盖，100% 分支覆盖）
mvn verify -pl test
# 报告位置：test/target/site/jacoco/index.html

# 运行应用
mvn spring-boot:run -pl start
```

### 测试覆盖率要求

- 行覆盖率：**≥95%**
- 分支覆盖率：**100%**
- 通过率：**100%**

---

## 架构：四层 DDD

### 依赖规则

```
Adapter → Application → Domain ← Infrastructure
```

**关键**：Domain 层零外部依赖，是纯业务逻辑。

### 模块结构

| 模块                 | 用途     | 核心内容                                                 |
|--------------------|--------|------------------------------------------------------|
| **domain**         | 核心业务逻辑 | 聚合根、实体、值对象、领域事件、仓储接口、规格模式                            |
| **app**            | 用例编排   | 应用服务、CQRS 处理器、DTO 转换器、事务边界                           |
| **infrastructure** | 技术实现   | 仓储实现、事件发布器、客户端抽象（缓存/SMS/邮件/OSS/搜索）、MyBatis-Flex 生成代码 |
| **adapter**        | 接口适配   | REST 控制器、事件监听器、定时任务                                  |
| **start**          | 启动引导   | 所有 `@Configuration` 类（Bean 装配）、应用入口点                 |
| **test**           | 独立测试   | 单元测试（纯 Mock）、集成测试（Spring + H2）、启动验证                  |

### 层级职责

**Domain** (`domain/`):

- `bizshared/base/` - 基类：`AggregateRoot<T, ID>`、`Entity`、`ValueObject`、`Identifier`
- `bizshared/client/` - 技术客户端接口（Cache、Email、SMS、OSS、Search）
- `common/` - 通用领域对象（文件、搜索、事件）

**Application** (`app/`):

- 编排多个聚合根/服务
- CQRS 命令/查询分离
- 事务管理（`@Transactional`）
- DTO 转换（MapStruct）
- **反模式**：无业务规则（委托给 Domain），无直接基础设施访问

**Infrastructure** (`infrastructure/`):

- `bizshared/dal/generated/` - **MyBatis-Flex 自动生成，禁止编辑**
- `bizshared/event/` - 事件发布（Spring/Kafka 自动检测）、重试策略
- `bizshared/client/` - 带自动检测的客户端实现（Caffeine/Redis、Local/RustFS、阿里云 SMS/Email）

**Adapter** (`adapter/`):

- `access/listener/` - 事件监听器（Spring Events & Kafka）
- `access/schedule/` - 事件重试调度器（支持 XXL-JOB/PowerJob/SchedulerX）
- `web/api/` - REST 控制器
- **反模式**：无业务逻辑、无直接仓储调用、无配置类

**Start** (`start/`):

- `config/` - **所有 @Configuration 类必须在这里**（命名 `*Configure` 模式）
- `ApplicationBootstrap.java` - Spring Boot 主类

---

## Critical Coding Rules

### Configuration Classes (CRITICAL)

**ALL `@Configuration` classes must be in `start/src/main/java/{groupId}/config/`**

**Naming**: Use `{Aggregate}Configure` pattern, NOT layer-based names:

| ✅ Correct                | ❌ Forbidden                             |
|--------------------------|-----------------------------------------|
| `OrderConfigure.java`    | `OrderAppConfigure.java`                |
| `FileConfigure.java`     | `OrderInfraConfigure.java`              |
| `EventLogConfigure.java` | `OrderConfig.java` (must use Configure) |

**Bean Registration**:

- ✅ Use `@Bean` methods in `@Configuration` classes
- ❌ **FORBIDDEN**: `@Service`, `@Component`, `@Repository` (except `@Controller` in adapter web layer)

### Dependency Injection

**Preferred**: Constructor injection with `@RequiredArgsConstructor`

```java
// ✅ Correct
@RequiredArgsConstructor
public class OrderServiceImpl {
    private final OrderRepository orderRepository;
}
```

**❌ FORBIDDEN**:

- `@Autowired` on fields (except in test code)
- `@Lazy` for circular dependencies (must refactor instead)
- Using `@Component`, `@Service`, `@Repository` annotations

### Circular Dependencies

**❌ ABSOLUTELY FORBIDDEN**:

- `@Lazy` annotation
- `ObjectProvider` lazy injection
- `ApplicationContext.getBean()` dependency lookup

**✅ Correct resolution**:

- Cross-config: Constructor injection + `Optional`
- Same-config: `@Bean` method parameter injection
- **Refactor code to eliminate the cycle**

### Lombok Rules

| Annotation                 | Usage                                           |
|----------------------------|-------------------------------------------------|
| `@Data`                    | ❌ **FORBIDDEN** (unpredictable equals/hashCode) |
| `@Getter` / `@Setter`      | ✅ Allowed                                       |
| `@Builder`                 | ✅ Recommended                                   |
| `@RequiredArgsConstructor` | ✅ Recommended for DI                            |

### Client Interface Pattern

All external clients (cache, email, SMS, OSS, search) follow this pattern:

1. **Interface in Domain**: `domain/bizshared/client/{Xxx}Client.java`
2. **Abstract Base in Infrastructure**: `infrastructure/bizshared/client/{Xxx}/Abstract{Xxx}Client.java`
3. **Implementation in Infrastructure**: `infrastructure/bizshared/client/{Xxx}/impl/{Xxx}ClientImpl.java`
4. **Bean Registration in Start**: `start/config/{Xxx}Configure.java`

**Auto-detection**: Uses `@ConditionalOnBean` to detect Redis/Kafka/RustFS availability

### MapStruct Converters

**Naming**: `*BusinessConverter` (e.g., `OrderBusinessConverter`)
**Location**: In infrastructure module
**Pattern**: DO ↔ Domain, DTO ↔ App DTO conversions

---

## TDD Workflow (MANDATORY 5-Phase Process)

This project enforces strict TDD. Follow this workflow for ANY code changes:

### Phase 0: Requirements & TDD Planning

- **Ask user repeatedly** for ALL details (business, technical, architectural)
- Write test cases (TDD red phase)
- Confirm test case completeness with user

### Phase 1: Write Unit Tests (Red)

- Write test cases that FAIL
- Verify failure before proceeding

### Phase 2: Compile Verification

```bash
mvn clean compile
```

- Verify MapStruct code generation
- Fix compilation errors

### Phase 3: Execute Unit Tests (Green)

```bash
mvn test
```

- Implement business logic
- Ensure 100% pass rate
- Verify coverage (≥95% line, 100% branch)

### Phase 4: Startup Validation (MOST CRITICAL)

```bash
mvn test -Dtest=ApplicationStartupTests -pl test
```

- Verify Spring context startup
- Check for circular dependencies
- Ensure all beans loaded correctly

### Phase 5: Code-Documentation Alignment

- Identify changed files (`git status`)
- Identify affected documentation
- Update relevant documentation
- Verify consistency

**Shortcut**: `mvn clean compile test && mvn test -Dtest=ApplicationStartupTests -pl test && git status`

---

## Module-Specific Guidelines

### Domain Module

- **Pure business logic** - No external dependencies
- All aggregates extend `AggregateRoot<T, ID>`
- Use value objects for domain concepts
- Define Repository interfaces only (implementations in Infrastructure)

### Application Module

- **Orchestration only** - No business rules
- Use `@Transactional` for transaction boundaries
- CQRS: Separate Command and Query handlers
- DTO conversions via MapStruct

### Infrastructure Module

- **DO NOT EDIT** `generated/` package (MyBatis-Flex auto-generated)
- Implement Repository interfaces from Domain
- Use `@ConditionalOnBean` for optional middleware
- Event publishing with auto-detection (Spring Events vs Kafka)

### Adapter Module

- **NO configuration classes** - All in `start/config/`
- Controllers delegate to Application layer
- Event listeners handle both Spring Events and Kafka
- Schedulers support XXL-JOB/PowerJob/SchedulerX

### Start Module

- **ONLY location for `@Configuration` classes**
- All Bean assembly via `@Bean` methods
- Constructor injection preferred
- Resolve circular dependencies through refactoring (NEVER use `@Lazy`)

### Test Module

- **Independent module** (not co-located with production code)
- `UnitTestBase` - Pure mocks, no Spring context
- `IntegrationTestBase` - Spring + H2 + DBUnit
- `ApplicationStartupTests` - **Critical for startup validation**

---

## Common Patterns

### Aggregate Creation

```java
public class OrderAggr extends AggregateRoot<OrderAggr, OrderId> {
    public static OrderAggr create(String customerId, Money totalAmount) {
        OrderAggr order = new OrderAggr();
        order.id = OrderId.generate();
        order.status = OrderStatus.CREATED;
        order.recordEvent(new OrderCreatedEvent(order.id));
        return order;
    }
}
```

### Repository Implementation

```java
public class OrderAggrRepositoryImpl implements OrderAggrRepository {
    private final OrderMapper orderMapper;
    private final OrderBusinessConverter converter;

    @Override
    public void save(OrderAggr order) {
        OrderDO orderDO = converter.toDO(order);
        orderMapper.insertOrUpdate(orderDO);
    }
}
```

### Application Service

```java
@RequiredArgsConstructor
public class OrderAppService {
    private final OrderAggrRepository orderRepository;

    @Transactional
    public OrderId create(CreateOrderCommand command) {
        OrderAggr order = OrderAggr.create(command.getCustomerId(), command.getTotalAmount());
        orderRepository.save(order);
        return order.getId();
    }
}
```

### Controller

```java
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderAppService orderAppService;

    @PostMapping
    public Response<OrderDTO> create(@RequestBody CreateOrderRequest request) {
        OrderId orderId = orderAppService.create(new CreateOrderCommand(request));
        return Response.success(orderAppService.queryById(orderId));
    }
}
```

---

## Middleware Auto-Detection

The project auto-detects optional middleware:

| Middleware    | Detection Method                          | Fallback                   |
|---------------|-------------------------------------------|----------------------------|
| Kafka         | `@ConditionalOnBean(KafkaTemplate.class)` | Spring Events (in-memory)  |
| Redis         | `@ConditionalOnBean(RedisTemplate.class)` | Caffeine (local cache)     |
| Elasticsearch | `@ConditionalOnBean(Client.class)`        | `DisabledSearchClientImpl` |
| RustFS (S3)   | `@ConditionalOnBean(S3Client.class)`      | Local file system          |

---

## Forbidden Practices Summary

| Forbidden                                     | Reason                                  |
|-----------------------------------------------|-----------------------------------------|
| `@Data` annotation                            | Unpredictable equals/hashCode           |
| `@Service`, `@Component`, `@Repository`       | Use `@Bean` in `@Configuration` instead |
| `@Lazy` for circular dependencies             | Must refactor instead                   |
| Configuration classes outside `start/config/` | Centralized Bean assembly required      |
| Editing `generated/` package                  | Auto-generated by MyBatis-Flex          |
| Domain layer with external dependencies       | Must remain pure                        |
| Controller → Repository direct calls          | Must go through Application layer       |
| Business logic in Adapter/Application         | Belongs in Domain layer                 |
| `System.out.println`                          | Use `@Slf4j` logging                    |

---

## Test Structure

```
test/src/test/java/org/smm/archetype/test/cases/
├── unittest/          # Unit tests (pure mocks)
│   └── {package-path}/
│       └── {ClassName}UTest.java
└── integrationtest/   # Integration tests (Spring context)
    └── {package-path}/
        └── {ClassName}ITest.java
```

**Naming**: `{ClassName}UTest` for unit, `{ClassName}ITest` for integration

---

## 文档导航

### 📚 完整文档索引

| 文档                                                    | 用途                       | 读者           |
|-------------------------------------------------------|--------------------------|--------------|
| **[项目知识库](AGENTS.md)**                                | 项目架构概览和架构偏差分析            | 开发者、架构师      |
| **[Maven Archetype使用指南](ARCHETYPE_USAGE.md)**         | 快速生成基于 DDD 架构的 Java 项目骨架 | 开发者          |
| **[验证流程指南](_docs/specification/业务代码生成(AI)流程.md)**     | TDD 代码验证强制流程             | 开发者、AI       |
| **[业务代码编写规范](_docs/specification/业务代码编写规范.md)**       | 业务代码编码标准                 | 开发者、AI       |
| **[测试代码编写与示例指南](_docs/specification/测试代码编写与示例指南.md)** | 测试代码编写标准和完整示例            | 开发者、AI       |
| **[业务文档索引](_docs/business/README.md)**                | 业务开发文档（需求、设计、技术选型等）      | 产品经理、架构师、开发者 |

### 📚 项目知识库（AGENTS.md）

| 文档                                                 | 用途                | 读者    |
|----------------------------------------------------|-------------------|-------|
| **[Domain 层指南](domain/AGENTS.md)**                 | 领域层核心业务逻辑和约定      | 后端开发者 |
| **[Infrastructure 层指南](infrastructure/AGENTS.md)** | 基础设施层技术实现和约定      | 后端开发者 |
| **[Application 层指南](app/AGENTS.md)**               | 应用层 CQRS 和用例编排    | 后端开发者 |
| **[Adapter 层指南](adapter/AGENTS.md)**               | 接口层 REST 控制器和事件监听 | 后端开发者 |
| **[Start 模块指南](start/AGENTS.md)**                  | 启动模块 Bean 装配和配置   | 后端开发者 |
| **[Test 模块指南](test/AGENTS.md)**                    | 测试模块测试规范和最佳实践     | 测试开发者 |

### 📚 业务文档模板

| 文档                                 | 用途               | 目标读者          |
|------------------------------------|------------------|---------------|
| **[业务需求](_docs/business/业务需求.md)** | 记录业务需求、功能列表      | 产品经理、开发者      |
| **[产品设计](_docs/business/产品设计.md)** | 产品设计方案、原型、交互设计   | 产品经理、UI/UX    |
| **[技术选型](_docs/business/技术选型.md)** | 技术方案对比、选型依据      | 架构师、Tech Lead |
| **[数据设计](_docs/business/数据设计.md)** | 数据库设计、表结构、索引     | 后端开发、DBA      |
| **[接口文档](_docs/business/接口文档.md)** | API 接口定义、请求/响应格式 | 后端、测试         |
| **[实现方案](_docs/business/实现方案.md)** | 技术实现细节、架构设计      | 开发者           |
| **[开发计划](_docs/business/开发计划.md)** | 开发任务分解、时间安排      | 项目经理、开发团队     |
| **[进度记录](_docs/business/进度记录.md)** | 开发进度跟踪、问题记录      | 开发者、项目经理      |
| **[发版说明](_docs/business/发版说明.md)** | 版本发布内容、变更说明      | 项目经理、运维       |

### 📚 规格文档

| 文档                                                    | 用途            | 目标读者   |
|-------------------------------------------------------|---------------|--------|
| **[验证流程指南](_docs/specification/业务代码生成(AI)流程.md)**     | TDD 代码验证强制流程  | 开发者、AI |
| **[业务代码编写规范](_docs/specification/业务代码编写规范.md)**       | 业务代码编码标准      | 开发者、AI |
| **[测试代码编写与示例指南](_docs/specification/测试代码编写与示例指南.md)** | 测试代码编写标准和完整示例 | 开发者、AI |

---

## 参考文档

- **TDD 工作流**：`_docs/specification/业务代码生成(AI)流程.md`
- **编码标准**：`_docs/specification/业务代码编写规范.md`
- **测试指南**：`_docs/specification/测试代码编写与示例指南.md`
- **项目 README**：`README.md`

---

## Example Module Reference

The `order` module (`_example/order/`) contains **75 classes** demonstrating all DDD concepts:

- Aggregates, Entities, Value Objects
- Repository interfaces and implementations
- Application services with CQRS
- Controllers, event listeners
- Complete test coverage

Use this as the primary reference for implementation patterns.

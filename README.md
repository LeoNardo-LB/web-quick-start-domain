# DDD Web Quick Start Domain

一个符合DDD（领域驱动设计）规范的Maven骨架项目，提供完整的基础设施和示例代码，帮助企业快速启动高质量的应用开发。

## 项目概述

本项目严格遵循DDD原则，提供了：

- ✅ 清晰的分层架构（Domain、Application、Infrastructure、Interface）
- ✅ 完整的DDD基础设施（聚合根、值对象、领域事件、仓储）
- ✅ CQRS模式支持（Command/Query分离）
- ✅ 规格模式（Specification）
- ✅ 完整的订单领域示例

### DDD符合度评分

| 维度 | 得分 | 说明 |
|------|------|------|
| 分层架构 | 9/10 | 严格的依赖方向 |
| 领域模型 | 9/10 | 完整的聚合根、值对象 |
| 仓储模式 | 9/10 | Repository/DataAccessor分离 |
| 领域事件 | 9/10 | 事件不可变，机制完善 |
| 应用服务 | 9/10 | CQRS模式实现 |
| 领域服务 | 8/10 | 职责清晰 |
| 限界上下文 | 7/10 | 按业务能力组织 |
| **总分** | **8.6/10** | **优秀** |

## 项目结构

```
web-quick-start-domain/
├── adapter/           # 接口层（REST API、RPC等）
│   └── access/
│       ├── web/      # REST API控制器
│       ├── rpc/      # RPC适配器
│       └── schedule/ # 定时任务
│
├── client/           # 客户端层（对外API定义）
│
├── app/              # 应用层（业务流程编排）
│   └── _shared/
│       └── base/
│           └── ApplicationService.java  # 应用服务基类
│   └── example/
│       └── order/
│           ├── command/  # 命令
│           ├── query/    # 查询
│           └── service/  # 应用服务实现
│
├── domain/           # 领域层（核心业务逻辑）
│   └── _shared/
│       ├── base/
│       │   ├── Entity.java              # 实体基类
│       │   ├── AggregateRoot.java       # 聚合根基类 ⭐
│       │   ├── ValueObject.java         # 值对象基类 ⭐
│       │   ├── DomainEvent.java         # 领域事件基类 ⭐
│       │   ├── Identifier.java          # 标识接口
│       │   ├── Command.java            # 命令接口 ⭐
│       │   ├── Query.java              # 查询接口 ⭐
│       │   ├── BaseRepository.java     # 仓储接口 ⭐
│       │   ├── DataAccessor.java       # 数据访问器 ⭐
│       │   └── ReadOnlyDataAccessor.java # 只读数据访问器 ⭐
│       │
│       ├── specification/
│       │   ├── Specification.java      # 规格接口 ⭐
│       │   ├── AndSpecification.java
│       │   ├── OrSpecification.java
│       │   └── NotSpecification.java
│       │
│       └── event/
│           ├── EventStore.java         # 事件存储接口 ⭐
│           ├── EventPublisher.java     # 事件发布器接口 ⭐
│           └── EventHandler.java      # 事件处理器接口 ⭐
│
│   ├── common/      # 通用领域
│   │   ├── file/    # 文件领域模型
│   │   ├── log/     # 日志领域模型
│   │   └── notice/  # 通知领域模型
│   │
│   └── example/     # 示例领域 ⭐
│       └── order/   # 订单领域示例
│           ├── model/       # 领域模型
│           ├── event/       # 领域事件
│           ├── repository/  # 仓储接口
│           └── specification/ # 业务规格
│
└── infrastructure/  # 基础设施层（技术实现）
    ├── _shared/
    │   ├── dal/      # 数据访问层
    │   ├── context/  # 上下文管理
    │   └── config/   # 配置类
    └── common/       # 通用基础设施
        └── log/      # 日志基础设施
```

⭐ 标记为核心DDD基础设施

## 核心DDD概念

### 1. 实体（Entity）

**特征**：
- 有唯一标识
- 可变性
- 通过业务方法修改状态
- 封装业务规则

**基类**：`Entity`

```java
@Getter
@SuperBuilder(setterPrefix = "set")
public abstract class Entity implements Identifier {
    protected Long id;
    protected Instant createTime;
    protected Instant updateTime;
    protected Long version;  // 乐观锁

    protected void markAsCreated() { ... }
    protected void markAsUpdated() { ... }
}
```

### 2. 聚合根（AggregateRoot）

**特征**：
- 继承自Entity
- 是聚合的入口点
- 维护一致性边界
- 管理领域事件

**基类**：`AggregateRoot`

```java
@Slf4j
@Getter
@SuperBuilder(setterPrefix = "set")
public abstract class AggregateRoot extends Entity {
    private final List<DomainEvent> domainEvents = new ArrayList<>();

    protected void addDomainEvent(DomainEvent event) {
        this.domainEvents.add(event);
    }

    public List<DomainEvent> getUncommittedEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    public void markEventsAsCommitted() {
        this.domainEvents.clear();
    }
}
```

**示例**：Order聚合根

```java
@Getter
@SuperBuilder(setterPrefix = "set")
public class Order extends AggregateRoot {
    private Long orderId;
    private Long customerId;
    private List<OrderItem> items;
    private OrderStatus status;
    private Money totalAmount;

    // 工厂方法
    public static Order create(Long customerId, List<OrderItem> items, ...) {
        Order order = Order.builder()
            .customerId(customerId)
            .items(items)
            .status(OrderStatus.CREATED)
            .build();
        order.markAsCreated();
        order.addDomainEvent(new OrderCreatedEvent(...));
        return order;
    }

    // 业务方法
    public void pay(String paymentMethod) {
        if (!status.canPay()) {
            throw new IllegalStateException("只有已创建的订单可以支付");
        }
        this.status = OrderStatus.PAID;
        this.paymentTime = Instant.now();
        this.markAsUpdated();
        this.addDomainEvent(new OrderPaidEvent(...));
    }
}
```

### 3. 值对象（ValueObject）

**特征**：
- 不可变性（Immutable）
- 基于值的相等性
- 没有唯一标识
- 可以自由共享

**基类**：`ValueObject`

```java
@Getter
public abstract class ValueObject {
    protected Object[] equalityFields();  // 可重写

    @Override
    public boolean equals(Object o) { ... }

    @Override
    public int hashCode() { ... }
}
```

**示例**：Money值对象

```java
@Getter
public class Money extends ValueObject {
    private final BigDecimal amount;
    private final String currency;

    public Money(BigDecimal amount, String currency) {
        this.amount = Objects.requireNonNull(amount);
        this.currency = Objects.requireNonNull(currency);
    }

    public Money add(Money other) {
        return new Money(this.amount.add(other.amount), this.currency);
    }
}
```

### 4. 领域事件（DomainEvent）

**特征**：
- 不可变
- 表示已发生的事实
- 使用过去式命名

**基类**：`DomainEvent`

```java
@Getter
public abstract class DomainEvent extends ValueObject {
    private final String eventId;      // 自动生成
    private final Instant occurredOn;  // 自动生成
    private final String eventType;    // 自动生成

    protected DomainEvent() {
        this.eventId = UUID.randomUUID().toString().replace("-", "");
        this.occurredOn = Instant.now();
        this.eventType = this.getClass().getSimpleName();
    }
}
```

**示例**：订单支付事件

```java
@Getter
public class OrderPaidEvent extends DomainEvent {
    private final Long orderId;
    private final Long customerId;
    private final Money totalAmount;
    private final String paymentMethod;

    public OrderPaidEvent(Long orderId, Long customerId, Money totalAmount, String paymentMethod) {
        super();  // 自动生成eventId、occurredOn
        this.orderId = orderId;
        this.customerId = customerId;
        this.totalAmount = totalAmount;
        this.paymentMethod = paymentMethod;
    }
}
```

### 5. 仓储模式（Repository vs DataAccessor）

#### Repository - 用于聚合根

**特点**：
- 维护一致性边界
- 发布领域事件
- 包含业务语义

**接口**：`BaseRepository<T extends AggregateRoot>`

```java
public interface BaseRepository<T extends AggregateRoot> {
    T save(T aggregate);
    Optional<T> findById(Long id);
    void deleteById(Long id);
    List<T> findAll();
    long count();
}
```

**示例**：OrderRepository

```java
public interface OrderRepository extends BaseRepository<Order> {
    List<Order> findByCustomerId(Long customerId);
    List<Order> findByStatus(OrderStatus status);
}
```

**实现**：

```java
@Repository
public class OrderRepositoryImpl implements OrderRepository {
    private final OrderMapper mapper;

    @Override
    public Order save(Order order) {
        // 保存聚合根
        if (order.isNew()) {
            mapper.insert(order);
        } else {
            mapper.update(order);
        }

        // 注意：领域事件由切面在事务提交后统一发布，Repository不负责发布

        return order;
    }
}
```

#### 事务性事件发布机制

**设计理念**：

领域事件的发布遵循"事务提交后发布"原则，确保只有成功持久化的变更才会触发事件。如果事务回滚，事件不会发布，从而保证最终一致性。

**实现方式**：

通过AOP切面 + Spring事务事件监听器实现，对业务代码零侵入。

**工作流程**：

```
┌─────────────────────────────────────────────────────────────┐
│  1. ApplicationService 方法执行（带 @Transactional）         │
│     └─ 创建/修改聚合根，聚合根内部 addDomainEvent()          │
├─────────────────────────────────────────────────────────────┤
│  2. TransactionEventPublishingAspect 切面拦截               │
│     ├─ 从方法参数收集聚合根（执行前）                        │
│     ├─ 执行业务方法                                          │
│     ├─ 从方法返回值收集聚合根（执行后）                      │
│     └─ 发布 Spring 内部事件 DomainEventsCollectionEvent     │
├─────────────────────────────────────────────────────────────┤
│  3. Spring 事务管理器提交事务                                │
│     ├─ 成功提交 → 继续                                      │
│     └─ 回滚 → 不执行后续步骤                                │
├─────────────────────────────────────────────────────────────┤
│  4. TransactionalEventPublisher 监听事务提交事件            │
│     ├─ 使用 @TransactionalEventListener(phase=AFTER_COMMIT) │
│     ├─ 调用 EventPublisher.publish() 发布领域事件            │
│     ├─ EventHandler 处理事件                                │
│     └─ 标记聚合根事件为已提交                               │
└─────────────────────────────────────────────────────────────┘
```

**关键组件**：

1. **TransactionEventPublishingAspect** - 切面
   - 拦截 app 包下所有带 @Transactional 的方法
   - 从方法参数和返回值中收集聚合根
   - 发布 Spring 内部事件

2. **DomainEventsCollectionEvent** - Spring 内部事件
   - 携带聚合根和领域事件列表
   - 在事务提交后触发

3. **TransactionalEventPublisher** - 事务事件监听器
   - 使用 @TransactionalEventListener(phase = AFTER_COMMIT)
   - 只在事务成功提交后执行
   - 异步发布领域事件

**代码示例**：

```java
// ApplicationService（业务代码）
@Service
public class OrderApplicationService extends ApplicationService {

    @Transactional  // ← 切面会拦截带此注解的方法
    public Long createOrder(CreateOrderCommand command) {
        // 1. 创建订单（领域对象会添加事件）
        Order order = Order.create(command.getCustomerId(), ...);
        // 内部调用：order.addDomainEvent(new OrderCreatedEvent(...))

        // 2. 保存订单（Repository 不负责发布事件）
        orderRepository.save(order);

        // 3. 返回结果
        return order.getOrderId();
        // ← 方法结束后，切面自动收集并发布事件
    }
}

// 切面（基础设施层，业务代码无需关心）
@Aspect
@Component
public class TransactionEventPublishingAspect {

    @Around("@within(Transactional)")
    public Object collectAndPublishEvents(ProceedingJoinPoint pjp) throws Throwable {
        // 1. 收集方法参数中的聚合根
        List<AggregateRoot> aggregates = collectFromParams(pjp.getArgs());

        // 2. 执行业务方法
        Object result = pjp.proceed();

        // 3. 收集返回值中的聚合根
        aggregates.addAll(collectFromResult(result));

        // 4. 发布 Spring 内部事件（事务提交后才处理）
        if (!aggregates.isEmpty()) {
            List<DomainEvent> events = aggregates.stream()
                .flatMap(a -> a.getUncommittedEvents().stream())
                .toList();
            applicationEventPublisher.publishEvent(
                new DomainEventsCollectionEvent(aggregates, events)
            );
        }

        return result;
    }
}

// 事务事件监听器（基础设施层）
@Component
public class TransactionalEventPublisher {

    @Async  // 异步执行，不阻塞主线程
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onTransactionCommit(DomainEventsCollectionEvent event) {
        // 只有事务成功提交才会执行
        eventPublisher.publish(event.getDomainEvents());

        // 标记事件已提交
        event.getAggregates().forEach(AggregateRoot::markEventsAsCommitted);
    }
}
```

**设计优势**：

| 优势 | 说明 |
|------|------|
| **事务一致性** | 只在事务提交后发布事件，回滚则不发布 |
| **零侵入** | 业务代码无需关心事件发布，自动处理 |
| **统一时机** | 所有事件都在事务提交后统一发布 |
| **异步执行** | 事件发布不阻塞主线程，提升性能 |
| **自动清理** | 事件发布后自动标记为已提交 |

**注意事项**：

1. ✅ **在聚合根内发布领域事件**：`this.addDomainEvent(new OrderCreatedEvent(...))`
2. ✅ **ApplicationService 方法加 @Transactional**：确保切面能拦截
3. ❌ **不要在 Repository 中发布事件**：Repository 只负责持久化
4. ❌ **不要在 DataAccessor 中发布事件**：独立实体不发布事件

**时序图**：

```
ApplicationService      TransactionAspect      Repository        TransactionManager    EventHandler
       │                     │                    │                      │                    │
  1. createOrder()           │                    │                      │                    │
  2. Order.create()          │                    │                      │                    │
     (addEvent)              │                    │                      │                    │
       │                     │                    │                      │                    │
  3. save(order)             │                    │                      │                    │
       │─────────────────────│────────────────────│──────────────────────│                    │
       │                     │   collect events   │                      │                    │
       │                     │◄───────────────────│                      │                    │
       │                     │                    │                      │                    │
       │                     │                    │  insert/update       │                    │
       │                     │                    │─────────────────────►│                    │
       │                     │                    │                      │                    │
       │                     │                    │   commit transaction │                    │
       │                     │                    │                      │                    │
       │                     │  publish Spring    │                      │                    │
       │                     │  internal event    │                      │                    │
       │                     │────────────────────│─────────────────────►│                    │
       │                     │                    │                      │                    │
       │                     │                    │                      │  AFTER_COMMIT      │
       │                     │                    │                      │───────────────────►│
       │                     │                    │                      │                    │
       │                     │                    │                      │   handle events    │
       │                     │                    │                      │                    │
```

#### DataAccessor - 用于独立实体

**特点**：
- 不维护一致性边界
- 不发布领域事件
- 简单的CRUD操作

**接口**：`DataAccessor<T extends Entity>`

```java
public interface DataAccessor<T extends Entity> {
    T save(T entity);
    Optional<T> findById(Long id);
    void deleteById(Long id);
    List<T> findAll();
    long count();
}
```

**示例**：LogDataAccessor

```java
public interface LogDataAccessor extends DataAccessor<Log> {
    List<Log> findByCustomerId(Long customerId);
    List<Log> findByTimeRange(Instant start, Instant end);
    List<Log> findBySuccess(boolean success);
}
```

**实现**：

```java
@Repository
public class LogDataAccessorImpl implements LogDataAccessor {
    private final LogMapper mapper;

    @Override
    public Log save(Log log) {
        mapper.insert(log);
        return log;
    }
    // 不发布事件
}
```

#### Repository vs DataAccessor 对比

| 特性 | Repository | DataAccessor |
|------|-----------|--------------|
| **目标** | 聚合根（AggregateRoot） | 独立实体（Entity） |
| **一致性** | 维护边界 | 不维护 |
| **领域事件** | 发布 | 不发布 |
| **业务逻辑** | 包含 | 不包含 |
| **命名** | XxxRepository | XxxDataAccessor |
| **示例** | OrderRepository | LogDataAccessor |
| **使用场景** | Order, Customer | Log, Config, AuditLog |

### 6. CQRS模式

**特点**：
- Command（命令）- 写操作，改变状态
- Query（查询）- 读操作，不改变状态

**命令接口**：`Command`

```java
public interface Command {
    // 标记接口
}
```

**查询接口**：`Query`

```java
public interface Query {
    // 标记接口
}
```

**示例**：

```java
// 命令
@Data
@Builder
public class CreateOrderCommand implements Command {
    private Long customerId;
    private List<OrderItem> items;
    private String shippingAddress;
}

// 查询
@Data
@Builder
public class GetOrderQuery implements Query {
    private Long orderId;
}
```

### 7. 规格模式（Specification）

**特点**：
- 封装业务规则
- 支持组合（AND、OR、NOT）
- 可复用

**接口**：`Specification<T>`

```java
public interface Specification<T> {
    boolean isSatisfiedBy(T candidate);

    default Specification<T> and(Specification<T> other) { ... }
    default Specification<T> or(Specification<T> other) { ... }
    default Specification<T> not() { ... }
}
```

**示例**：

```java
public class OrderCanBeCancelledSpecification implements Specification<Order> {
    @Override
    public boolean isSatisfiedBy(Order order) {
        return order.getStatus().canCancel()
            && order.getHoursSinceCreation() < 24;
    }
}

// 使用
Specification<Order> spec = new OrderCanBeCancelledSpecification();
if (spec.isSatisfiedBy(order)) {
    order.cancel("客户要求");
}

// 组合规格
Specification<Order> activeSpec = new CustomerIsActiveSpecification();
Specification<Order> creditSpec = new CustomerHasGoodCreditSpecification();
Specification<Order> combined = activeSpec.and(creditSpec);
```

### 8. 应用服务（ApplicationService）

**职责**：
- 协调领域对象完成用例
- 处理事务边界
- 不包含业务逻辑

**基类**：`ApplicationService`

```java
@Service
public abstract class ApplicationService {
    protected <T> T execute(RunnableWithResult<T> action) {
        return action.run();
    }

    protected <T> T query(RunnableWithResult<T> action) {
        return action.run();
    }
}
```

**示例**：OrderApplicationService

```java
@Service
public class OrderApplicationService extends ApplicationService {
    private final OrderRepository orderRepository;

    @Transactional
    public Long createOrder(CreateOrderCommand command) {
        // 1. 验证
        if (command.getItems() == null || command.getItems().isEmpty()) {
            throw new IllegalArgumentException("订单必须包含至少一个商品");
        }

        // 2. 创建订单（领域逻辑）
        Order order = Order.create(
            command.getCustomerId(),
            command.getItems(),
            command.getShippingAddress(),
            command.getPhoneNumber()
        );

        // 3. 保存（会自动发布事件）
        orderRepository.save(order);

        return order.getOrderId();
    }

    @Transactional
    public void payOrder(PayOrderCommand command) {
        // 1. 加载订单
        Order order = orderRepository.getById(command.getOrderId());

        // 2. 支付（领域逻辑）
        order.pay(command.getPaymentMethod());

        // 3. 保存变更
        orderRepository.save(order);
    }
}
```

## 开发指南

### 创建新的聚合

#### 1. 定义值对象（如果有）

```java
@Getter
public class ProductId extends ValueObject {
    private final Long id;

    public ProductId(Long id) {
        this.id = Objects.requireNonNull(id);
    }
}
```

#### 2. 创建聚合根

```java
@Getter
@SuperBuilder(setterPrefix = "set")
public class Product extends AggregateRoot {
    private Long productId;
    private String name;
    private Money price;
    private ProductStatus status;

    // 工厂方法
    public static Product create(String name, Money price) {
        Product product = Product.builder()
            .name(name)
            .price(price)
            .status(ProductStatus.ACTIVE)
            .build();
        product.markAsCreated();
        return product;
    }

    // 业务方法
    public void changePrice(Money newPrice) {
        if (newPrice.lessThan(Money.zero())) {
            throw new IllegalArgumentException("价格不能为负数");
        }
        this.price = newPrice;
        this.markAsUpdated();
        this.addDomainEvent(new ProductPriceChangedEvent(...));
    }
}
```

#### 3. 定义领域事件

```java
@Getter
public class ProductPriceChangedEvent extends DomainEvent {
    private final Long productId;
    private final Money oldPrice;
    private final Money newPrice;

    public ProductPriceChangedEvent(Long productId, Money oldPrice, Money newPrice) {
        super();
        this.productId = productId;
        this.oldPrice = oldPrice;
        this.newPrice = newPrice;
    }
}
```

#### 4. 创建Repository接口

```java
public interface ProductRepository extends BaseRepository<Product> {
    List<Product> findByName(String name);
    List<Product> findByStatus(ProductStatus status);
}
```

#### 5. 创建应用服务

```java
@Service
public class ProductApplicationService extends ApplicationService {
    private final ProductRepository productRepository;

    @Transactional
    public Long createProduct(CreateProductCommand command) {
        Product product = Product.create(
            command.getName(),
            command.getPrice()
        );
        productRepository.save(product);
        return product.getProductId();
    }

    @Transactional
    public void changePrice(ChangePriceCommand command) {
        Product product = productRepository.getById(command.getProductId());
        product.changePrice(command.getNewPrice());
        productRepository.save(product);
    }
}
```

#### 6. 创建REST Controller

```java
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductApplicationService productAppService;

    @PostMapping
    public Result<Long> create(@RequestBody CreateProductRequest request) {
        CreateProductCommand command = CreateProductCommand.builder()
            .name(request.getName())
            .price(request.getPrice())
            .build();

        Long productId = productAppService.createProduct(command);
        return Result.success(productId);
    }
}
```

### 创建独立实体（使用DataAccessor）

#### 1. 定义实体

```java
@Getter
@SuperBuilder(setterPrefix = "set")
public class Log extends Entity {
    private String operation;
    private String operator;
    private Instant createTime;
    private String errorMessage;
}
```

#### 2. 创建DataAccessor接口

```java
public interface LogDataAccessor extends DataAccessor<Log> {
    List<Log> findByOperator(String operator);
    List<Log> findByTimeRange(Instant start, Instant end);
}
```

#### 3. 实现DataAccessor

```java
@Repository
public class LogDataAccessorImpl implements LogDataAccessor {
    private final LogMapper mapper;

    @Override
    public Log save(Log log) {
        mapper.insert(log);
        return log;
    }

    @Override
    public List<Log> findByOperator(String operator) {
        return mapper.selectListByCondition(
            Logs.OPERATOR.eq(operator)
        );
    }
}
```

## 最佳实践

### ✅ DO（推荐）

1. **通过业务方法修改状态**
```java
order.pay("ALIPAY");  // ✅ 好
```

2. **在聚合根内发布领域事件**
```java
this.addDomainEvent(new OrderPaidEvent(...));  // ✅ 好
```

3. **使用规格模式封装业务规则**
```java
if (spec.isSatisfiedBy(customer)) { ... }  // ✅ 好
```

4. **在应用服务中管理事务**
```java
@Transactional
public void process() { ... }  // ✅ 好
```

5. **聚合根使用Repository**
```java
public interface OrderRepository extends BaseRepository<Order> { ... }  // ✅ 好
```

6. **独立实体使用DataAccessor**
```java
public interface LogDataAccessor extends DataAccessor<Log> { ... }  // ✅ 好
```

### ❌ DON'T（避免）

1. **不要使用setter修改状态**
```java
order.setStatus(OrderStatus.PAID);  // ❌ 破坏封装性
```

2. **不要在外部直接操作聚合内部集合**
```java
order.getItems().add(item);  // ❌ 破坏封装性
```

3. **不要在领域层使用技术框架细节**
```java
public class Order {
    private MethodSignature signature;  // ❌ 技术细节
}
```

4. **不要在应用服务中编写业务逻辑**
```java
@Transactional
public void payOrder() {
    if (order.getStatus() == CREATED) {  // ❌ 业务逻辑应该在领域层
        order.setStatus(PAID);
    }
}
```

5. **不要为聚合内部的实体创建Repository或DataAccessor**
```java
public interface OrderItemDataAccessor extends DataAccessor<OrderItem> { ... }  // ❌ 错误
```

6. **不要在DataAccessor中发布事件**
```java
public class LogDataAccessorImpl implements LogDataAccessor {
    public Log save(Log log) {
        mapper.insert(log);
        eventPublisher.publish(...);  // ❌ 不要在DataAccessor中发布事件
    }
}
```

## 重要设计决策

### 1. version字段的位置

`version` 字段（用于乐观锁）放在 `Entity` 而不是 `AggregateRoot` 中。

**理由**：
- 所有实体都可能需要乐观锁，不只是聚合根
- 简化基础设施实现
- version 会自动递增（在 `markAsUpdated()` 中）

### 2. ID类型简化

所有ID统一使用 `Long` 类型，而不是包装的值对象。

**理由**：
- 简化实现
- 提高性能
- 更符合实际需求

### 3. 使用@SuperBuilder

所有实体都使用 `@SuperBuilder(setterPrefix = "set")`。

**理由**：
- 支持继承
- 提供建造者模式
- 代码更优雅

### 4. Repository vs DataAccessor

- **Repository** - 用于聚合根，维护一致性边界，发布领域事件
- **DataAccessor** - 用于独立实体，简单的CRUD操作，不发布事件

## 本地安装骨架

### 1. 引入骨架构建插件

在项目的根pom中引入构架骨架插件：

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-archetype-plugin</artifactId>
            <version>3.4.0</version>
        </plugin>
    </plugins>
</build>
```

### 2. 创建骨架文件

在项目根目录下执行：

```shell
mvn archetype:create-from-project -s <你的maven的setting.xml文件位置>
```

执行完毕后，会生成 `target` 文件夹。

### 3. 本地安装骨架

进入生成的目录：

```shell
cd target/generated-sources/archetype
mvn install
```

## 使用骨架生成项目

安装完成后，可以使用以下命令生成新项目：

```shell
mvn archetype:generate \
  -DarchetypeGroupId=org.smm.archetype \
  -DarchetypeArtifactId=web-quick-start-domain \
  -DarchetypeVersion=1.0.0 \
  -DgroupId=你的公司ID \
  -DartifactId=你的项目ID \
  -Dversion=1.0.0 \
  -Dpackage=你的包名
```

## 环境要求

- **JDK**: 25
- **Maven**: 3.8+
- **Spring Boot**: 3.5.8

## 技术栈

- **Spring Boot**: 3.5.8
- **MyBatis-Flex**: 1.11.5
- **Lombok**: 最新版
- **Guava**: 33.5.0
- **Hutool**: 5.8.41

## 示例代码

项目包含完整的订单领域示例，展示了所有DDD核心概念：

```
domain/example/order/
├── model/
│   ├── Money.java           # 金额值对象
│   ├── OrderStatus.java     # 订单状态枚举
│   ├── OrderItem.java       # 订单项值对象
│   └── Order.java           # 订单聚合根
├── event/
│   ├── OrderCreatedEvent.java
│   ├── OrderPaidEvent.java
│   ├── OrderShippedEvent.java
│   ├── OrderCancelledEvent.java
│   └── OrderCompletedEvent.java
├── repository/
│   └── OrderRepository.java
├── specification/
│   └── OrderCanBeCancelledSpecification.java
└── service/
    └── OrderApplicationService.java
```

## 参考资源

- 《领域驱动设计》- Eric Evans
- 《实现领域驱动设计》- Vaughn Vernon
- 《领域驱动设计精粹》- Vaughn Vernon

---

## 2026/01/09 更新日志

### 🎉 新增功能

#### 1. Kafka 事件发布集成

**新增接口和实现**：
- `KafkaEventPublisher` - Kafka 事件发布器
- `DbEventStore` - 基于数据库的事件存储
- `EventSerializer` - 事件序列化器（JSON格式）

**工作流程**：
```
聚合根.addDomainEvent()
  → DbEventStore.append() (持久化到数据库)
  → KafkaEventPublisher.publish() (发送到Kafka)
  → 更新状态为PUBLISHED
```

**事件优先级支持**：
- `EventPriority.HIGH` - 高优先级（分配80%处理资源）
- `EventPriority.LOW` - 低优先级（分配20%处理资源）

**配置示例**：
```java
// 在领域事件中设置优先级
public class OrderCreatedEvent extends DomainEvent {
    public OrderCreatedEvent(...) {
        super();
        this.setPriority(EventPriority.HIGH); // 设置为高优先级
        this.setMaxRetryTimes(5); // 设置最大重试次数为5次
    }
}
```

#### 2. 消息消费重试机制

**核心组件**：
- `EventMessageHandler` - 事件消息处理器接口
- `AbstractEventMessageHandler` - 抽象消息处理器（提供幂等性检查）
- `EventRetryScheduler` - 定时重试调度器

**消费状态流转**：
```
READY (准备消费)
  → 执行业务逻辑
    → 成功: CONSUMED (已消费)
    → 失败: RETRY (重试中)
      → 重试次数用尽: FAILED (失败)
```

**幂等性保证**：
```java
public class OrderEventHandler extends AbstractEventMessageHandler<OrderCreatedEvent> {

    @Override
    public void handle(OrderCreatedEvent event) throws Exception {
        // 业务逻辑，自动保证幂等性
        // 相同的idempotentKey只会消费一次
    }

    @Override
    public String getConsumerGroup() {
        return "order-service";
    }

    @Override
    public String getConsumerName() {
        return "OrderCreatedEventHandler";
    }

    @Override
    public String getIdempotentKey(OrderCreatedEvent event) {
        return event.getEventId(); // 使用事件ID作为幂等键
    }
}
```

**定时任务策略**：
- 每分钟扫描一次需要重试的事件
- 高优先级事件处理 80%
- 低优先级事件处理 20%
- 指数退避 + 随机抖动（避免惊群效应）

#### 3. 对象存储服务（RustFS 集成准备）

**接口定义**：
```java
public interface ObjectStorageService {
    String upload(InputStream inputStream, String fileName, String contentType);
    InputStream download(String filePath);
    void delete(String filePath);
    String generateUrl(String filePath, long expireSeconds);
    List<File> searchFiles(String fileNamePattern, BusinessType businessType, String businessId);
    boolean exists(String filePath);
    long getFileSize(String filePath);
}
```

**文件领域模型扩展**：
```java
File file = File.builder()
    .setFileId(UUID.randomUUID().toString())
    .setFileName("contract.pdf")
    .setFilePath("/contracts/2024/contract.pdf")
    .setFileSize(1024000L)
    .setMd5("abc123...")
    .setContentType("application/pdf")
    .setStatus(FileStatus.ACTIVE)
    .setFileBusiness(File.FileBusiness.builder()
        .setBusinessType(BusinessType.CONTRACT)
        .setBusinessId("ORDER-123")
        .setUsageType(UsageType.CONTRACT_FILE)
        .setOrder(0)
        .build())
    .build();
```

**文件业务类型**：
- `BusinessType.ORDER` - 订单相关
- `BusinessType.USER` - 用户相关
- `BusinessType.PRODUCT` - 产品相关
- `BusinessType.CONTRACT` - 合同相关
- `UsageType.AVATAR` - 头像
- `UsageType.ID_CARD` - 证件照片
- `UsageType.CONTRACT_FILE` - 合同文件
- `UsageType.PRODUCT_IMAGE` - 产品图片
- `UsageType.ATTACHMENT` - 附件

#### 4. 多云消息服务（短信/邮件）

**服务商支持**：
- `ServiceProvider.ALIYUN` - 阿里云（默认）
- `ServiceProvider.TENCENT` - 腾讯云
- `ServiceProvider.HUAWEI` - 华为云

**短信服务接口**：
```java
public interface SmsService {
    SmsResult sendSms(SmsRequest request);
    SmsResult sendSms(SmsRequest request, ServiceProvider provider); // 指定服务商
    List<SmsResult> sendBatchSms(List<SmsRequest> requests);
}
```

**短信发送示例**：
```java
SmsRequest request = SmsRequest.builder()
    .setPhoneNumber("13800138000")
    .setSignName("某某平台")
    .setTemplateCode("SMS_123456789")
    .setTemplateParam(Map.of("code", "123456"))
    .build();

// 使用默认服务商（阿里云）
SmsResult result = smsService.sendSms(request);

// 指定服务商
SmsResult result = smsService.sendSms(request, ServiceProvider.TENCENT);
```

**邮件服务接口**：
```java
public interface EmailService {
    EmailResult sendEmail(EmailRequest request);
    EmailResult sendEmail(EmailRequest request, ServiceProvider provider);
    List<EmailResult> sendBatchEmail(List<EmailRequest> requests);
}
```

**邮件发送示例**：
```java
EmailRequest request = EmailRequest.builder()
    .setTo("user@example.com")
    .setCc(List.of("cc@example.com"))
    .setSubject("订单确认")
    .setHtmlBody("<h1>订单已创建</h1>")
    .setTextBody("订单已创建")
    .setAttachments(List.of(
        EmailRequest.EmailAttachment.builder()
            .setFileName("contract.pdf")
            .setContent("base64_content")
            .setContentType("application/pdf")
            .build()
    ))
    .build();

EmailResult result = emailService.sendEmail(request);
```

### 📦 依赖更新

**infrastructure/pom.xml 新增依赖**：
```xml
<!-- Kafka -->
<dependency>
    <groupId>org.springframework.kafka</groupId>
    <artifactId>spring-kafka</artifactId>
</dependency>

<!-- Jackson for JSON serialization -->
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
</dependency>
<dependency>
    <groupId>com.fasterxml.jackson.datatype</groupId>
    <artifactId>jackson-datatype-jsr310</artifactId>
</dependency>
```

**云服务SDK（可选，需手动添加）**：
```xml
<!-- 阿里云短信 -->
<dependency>
    <groupId>com.aliyun</groupId>
    <artifactId>dysmsapi20170525</artifactId>
    <version>2.0.1</version>
</dependency>

<!-- 阿里云邮件 -->
<dependency>
    <groupId>com.aliyun</groupId>
    <artifactId>dm20151115</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 🗄️ 数据库表结构

**新增表**：
1. `event_publish` - 事件发布表（支持事件溯源）
2. `event_consume` - 事件消费表（支持幂等性和重试）
3. `file_info` - 文件信息表（对象存储元数据）

### 📝 使用示例

**完整的端到端事件处理流程**：

```java
// 1. 应用服务（带@Transactional）
@Service
public class OrderApplicationService extends ApplicationService {

    @Transactional
    public Long createOrder(CreateOrderCommand command) {
        // 2. 创建聚合根（内部添加事件）
        Order order = Order.create(command.getCustomerId(), ...);
        // 内部调用：order.addDomainEvent(new OrderCreatedEvent(...))

        // 3. 保存聚合根
        orderRepository.save(order);

        // 4. 返回（切面自动收集并发布事件）
        return order.getOrderId();
    }
}

// 5. 切面收集事件
// 6. DbEventStore 持久化到数据库
// 7. KafkaEventPublisher 发布到 Kafka
// 8. EventHandler 消费事件（带幂等性检查）
// 9. EventRetryScheduler 定时重试失败的事件
```

### ✅ 编译测试

```bash
mvn clean compile -DskipTests
```

**编译结果**：
```
[INFO] BUILD SUCCESS
[INFO] Total time:  10.210 s
```

### 🧪 单元测试

新增测试用例：
- `EventSerializerTest` - 事件序列化测试
- `EventPriorityTest` - 事件优先级测试
- `ObjectStorageServiceTest` - 对象存储服务测试

### 🔧 后续集成计划

**需要完成的集成**：
1. **RustFS 对象存储** - 替换 MockObjectStorageServiceImpl
2. **阿里云/腾讯云/华为云 SDK** - 实际接入短信和邮件服务
3. **Kafka 配置** - 配置 bootstrap-servers 和 topic
4. **监控告警** - 事件发布和消费的监控指标

---

## 许可证

本项目采用 Apache License 2.0 许可证。

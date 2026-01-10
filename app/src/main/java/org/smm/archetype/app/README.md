# Application Layer - 应用层

> **Purpose**: 应用服务层，负责用例编排和事务管理
>
> **Core Principle**: 编排业务流程，不包含核心业务逻辑

---

## 📋 目录

1. [核心职责](#核心职责)
2. [目录结构](#目录结构)
3. [关键概念](#关键概念)
4. [配置类](#配置类)
5. [通用模式](#通用模式)
6. [架构约束](#架构约束)
7. [最佳实践](#最佳实践)

---

## 核心职责

- **用例编排（Orchestration）**: 协调多个领域对象完成业务用例
- **事务管理（Transaction Management）**: 管理事务边界
- **DTO转换（DTO Conversion）**: Request/Entity/VO之间的转换
- **调用领域服务（Domain Service Invocation）**: 委托给领域层执行业务逻辑
- **事件发布（Event Publishing）**: 收集并发布领域事件

---

## 目录结构

```
app/
├── _shared/                         # 共享应用层组件
│   ├── annotation/                 # 应用层注解
│   │   └── ApplicationService.java # 应用服务注解
│   ├── exception/                  # 应用层异常
│   │   └── ApplicationException.java
│   └── converter/                  # DTO转换器
│       └── XxxConverter.java
├── _example/                        # 示例业务模块
│   └── order/
│       ├── service/                # 应用服务
│       │   └── OrderApplicationService.java
│       ├── command/                # 命令对象（CQRS）
│       │   ├── CreateOrderCommand.java
│       │   └── PayOrderCommand.java
│       └── query/                  # 查询对象（CQRS）
│           └── OrderQueryService.java
└── common/                          # 通用应用组件
    ├── file/                       # 文件应用服务
    ├── log/                        # 日志应用服务
    └── notice/                     # 通知应用服务
```

---

## 关键概念

### 1. ApplicationService（应用服务）

**职责**: 编排业务流程，管理事务边界

**示例**: `OrderApplicationService`

```java

@ApplicationService
@RequiredArgsConstructor
@Slf4j
public class OrderApplicationService {

    private final OrderRepository    orderRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository  productRepository;
    private final EventPublisher     eventPublisher;

    /**
     * 创建订单用例
     */
    @Transactional(rollbackFor = Exception.class)
    public Order createOrder(OrderCreateRequest request) {
        log.info("创建订单: customerId={}", request.getCustomerId());

        // 1. 验证客户存在
        Customer customer = customerRepository.findById(CustomerId.of(request.getCustomerId()))
                                    .orElseThrow(() -> new CustomerNotFoundException(request.getCustomerId()));

        // 2. 验证产品并计算价格
        List<OrderItem> items = new ArrayList<>();
        for (OrderItemRequest itemRequest : request.getItems()) {
            Product product = productRepository.findById(ProductId.of(itemRequest.getProductId()))
                                      .orElseThrow(() -> new ProductNotFoundException(itemRequest.getProductId()));

            OrderItem item = OrderItem.create(
                    product.getProductId(),
                    product.getPrice(),
                    itemRequest.getQuantity()
            );
            items.add(item);
        }

        // 3. 创建订单（领域逻辑）
        Order order = Order.create(customer.getCustomerId(), items);

        // 4. 保存聚合
        orderRepository.save(order);

        // 5. 收集并发布事件
        List<DomainEvent> events = order.getUncommittedEvents();
        eventPublisher.publish(events);
        order.markEventsAsCommitted();

        log.info("订单创建成功: orderId={}", order.getOrderId());
        return order;
    }

    /**
     * 支付订单用例
     */
    @Transactional(rollbackFor = Exception.class)
    public void payOrder(String orderId, String paymentMethod) {
        log.info("支付订单: orderId={}, paymentMethod={}", orderId, paymentMethod);

        // 1. 查询订单
        Order order = orderRepository.findById(OrderId.of(orderId))
                              .orElseThrow(() -> new OrderNotFoundException(orderId));

        // 2. 执行支付（领域逻辑）
        order.pay(paymentMethod);

        // 3. 保存聚合
        orderRepository.save(order);

        // 4. 发布事件
        eventPublisher.publish(order.getUncommittedEvents());
        order.markEventsAsCommitted();

        log.info("订单支付成功: orderId={}", orderId);
    }

}
```

**ApplicationService命名规范**:

- 格式: `{模块名}ApplicationService`
- 位置: `app/{模块}/service/`
- 注解: `@ApplicationService`, `@Transactional`

### 2. CQRS（命令查询职责分离）

**Command（命令）**: 执行写操作

```java

@Getter
@Setter
@Builder(setterPrefix = "set")
public class CreateOrderCommand {

    @NotBlank(message = "客户ID不能为空")
    private String customerId;

    @NotEmpty(message = "订单项不能为空")
    private List<OrderItemCommand> items;

    @Data
    public static class OrderItemCommand {

        @NotBlank(message = "产品ID不能为空")
        private String productId;

        @Min(value = 1, message = "数量必须大于0")
        private Integer quantity;

    }

}
```

**Query（查询）**: 执行读操作

```java

@ApplicationService
@RequiredArgsConstructor
public class OrderQueryService {

    private final OrderRepository orderRepository;

    /**
     * 查询订单详情
     */
    public OrderVO getOrderById(String orderId) {
        Order order = orderRepository.findById(OrderId.of(orderId))
                              .orElseThrow(() -> new OrderNotFoundException(orderId));
        return OrderVO.from(order);
    }

    /**
     * 查询订单列表
     */
    public List<OrderVO> getOrdersByCustomer(String customerId) {
        List<Order> orders = orderRepository.findByCustomerId(CustomerId.of(customerId));
        return orders.stream()
                       .map(OrderVO::from)
                       .collect(Collectors.toList());
    }

}
```

**CQRS优势**:

- 读和写分离，性能优化
- 命令专注于业务逻辑
- 查询专注于数据展示

### 3. 事务边界管理

**原则**: 事务边界在ApplicationService方法上

```java

@ApplicationService
@RequiredArgsConstructor
public class OrderApplicationService {

    /**
     * ✅ 正确：事务边界在应用服务方法
     */
    @Transactional(rollbackFor = Exception.class)
    public void createOrder(OrderCreateRequest request) {
        // 1. 验证
        // 2. 创建领域对象
        // 3. 保存
        // 4. 发布事件
        // 整个过程在一个事务中
    }

}
```

**事务管理原则**:

- ✅ 事务边界在ApplicationService
- ✅ 使用`@Transactional(rollbackFor = Exception.class)`
- ✅ 事务尽量短小（避免大事务）
- ❌ 不要在Domain层使用事务
- ❌ 不要跨多个ApplicationService管理事务

---

## 配置类

### AppConfigure

**职责**: 应用层配置类，注册ApplicationService Bean

```java

@Configuration
@RequiredArgsConstructor
public class AppConfigure {

    private final OrderRepository    orderRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository  productRepository;
    private final EventPublisher     eventPublisher;

    /**
     * 订单应用服务
     */
    @Bean
    public OrderApplicationService orderApplicationService() {
        return new OrderApplicationService(
                orderRepository,
                customerRepository,
                productRepository,
                eventPublisher
        );
    }

    /**
     * 订单查询服务
     */
    @Bean
    public OrderQueryService orderQueryService() {
        return new OrderQueryService(orderRepository);
    }

}
```

---

## 通用模式

### 1. 用例编排模式

**场景**: 一个用例需要协调多个领域对象

```java

@ApplicationService
@RequiredArgsConstructor
public class OrderApplicationService {

    @Transactional(rollbackFor = Exception.class)
    public void placeOrder(OrderPlaceRequest request) {
        // 1. 验证客户
        Customer customer = customerRepository.findById(request.getCustomerId())
                                    .orElseThrow(() -> new CustomerNotFoundException(request.getCustomerId()));

        // 2. 检查库存
        for (OrderItemRequest itemRequest : request.getItems()) {
            Product product = productRepository.findById(itemRequest.getProductId())
                                      .orElseThrow(() -> new ProductNotFoundException(itemRequest.getProductId()));
            product.checkStock(itemRequest.getQuantity());
        }

        // 3. 创建订单
        Order order = Order.create(customer, request.getItems());

        // 4. 扣减库存
        for (OrderItem item : order.getItems()) {
            Product product = productRepository.findById(item.getProductId())
                                      .orElseThrow();
            product.decreaseStock(item.getQuantity());
            productRepository.save(product);
        }

        // 5. 保存订单
        orderRepository.save(order);

        // 6. 发布事件
        eventPublisher.publish(order.getUncommittedEvents());
        order.markEventsAsCommitted();
    }

}
```

### 2. DTO转换模式

**Request → Entity**: 在ApplicationService中转换

```java

@ApplicationService
@RequiredArgsConstructor
public class OrderApplicationService {

    public Order createOrder(OrderCreateRequest request) {
        // 手动转换或使用Converter
        CustomerId customerId = CustomerId.of(request.getCustomerId());
        List<OrderItem> items = request.getItems().stream()
                                        .map(itemReq -> OrderItem.create(
                                                ProductId.of(itemReq.getProductId()),
                                                itemReq.getQuantity()
                                        ))
                                        .collect(Collectors.toList());

        return Order.create(customerId, items);
    }

}
```

**Entity → VO**: 在VO中提供转换方法

```java
public class OrderVO {

    public static OrderVO from(Order order) {
        return OrderVO.builder()
                       .setOrderId(order.getOrderId().getValue())
                       .setStatus(order.getStatus().name())
                       .setTotalAmount(order.getTotalAmount().getAmount())
                       .build();
    }

}
```

### 3. 事件发布模式

**场景**: 领域对象发布事件，ApplicationService收集并发布

```java

@ApplicationService
@RequiredArgsConstructor
public class OrderApplicationService {

    private final EventPublisher eventPublisher;

    @Transactional(rollbackFor = Exception.class)
    public void payOrder(String orderId, String paymentMethod) {
        // 1. 执行业务逻辑（领域对象会发布事件）
        Order order = orderRepository.findById(OrderId.of(orderId))
                              .orElseThrow(() -> new OrderNotFoundException(orderId));
        order.pay(paymentMethod); // 内部会addDomainEvent

        // 2. 保存聚合
        orderRepository.save(order);

        // 3. 收集并发布事件
        List<DomainEvent> events = order.getUncommittedEvents();
        eventPublisher.publish(events);

        // 4. 清空事件
        order.markEventsAsCommitted();
    }

}
```

---

## 架构约束

### ✅ 允许

- ✅ 调用Domain层
- ✅ 调用Infrastructure层（Repository、EventPublisher等）
- ✅ 包含事务管理
- ✅ DTO转换
- ✅ 用例编排
- ✅ 发布领域事件

### ❌ 禁止

- ❌ 包含核心业务逻辑（应在Domain层）
- ❌ 直接访问数据库（应通过Repository）
- ❌ 处理HTTP相关逻辑（应在Adapter层）
- ❌ 被Domain层依赖

---

## 最佳实践

### 1. 应用服务保持薄薄的一层

**✅ 推荐**: 只做编排，委托给领域层

```java

@ApplicationService
public class OrderApplicationService {

    @Transactional
    public void createOrder(OrderCreateRequest request) {
        // 1. 验证依赖
        Customer customer = customerRepository.findById(...).orElseThrow();

        // 2. 调用领域逻辑
        Order order = Order.create(customer, request.getItems());

        // 3. 保存
        orderRepository.save(order);

        // 4. 发布事件
        eventPublisher.publish(order.getUncommittedEvents());
    }

}
```

**❌ 禁止**: 在应用服务中包含业务规则

```java
// ❌ 禁止：业务逻辑在应用服务中
@ApplicationService
public class OrderApplicationService {

    @Transactional
    public void createOrder(OrderCreateRequest request) {
        Order order = new Order();

        // 业务规则判断（应该在Domain层）
        if (request.getItems().size() > 100) {
            throw new IllegalArgumentException("订单项不能超过100个");
        }

        orderRepository.save(order);
    }

}
```

### 2. 事务边界清晰

**✅ 推荐**: 一个事务完成一个用例

```java

@ApplicationService
public class OrderApplicationService {

    @Transactional(rollbackFor = Exception.class)
    public void placeOrder(OrderPlaceRequest request) {
        // 整个用例在一个事务中完成
        // 1. 验证
        // 2. 创建订单
        // 3. 扣减库存
        // 4. 保存
        // 5. 发布事件
    }

}
```

**❌ 禁止**: 大事务或事务嵌套

```java
// ❌ 禁止：大事务
@Transactional
public void batchProcess(List<Order> orders) {
    for (Order order : orders) {
        // 处理1000个订单，事务太长
    }
}

// ❌ 禁止：事务嵌套
@Transactional
public void method1() {
    method2(); // 事务嵌套
}

@Transactional
public void method2() {
    // ...
}
```

### 3. 异常处理

**✅ 推荐**: 抛出业务异常，由Adapter层统一处理

```java

@ApplicationService
public class OrderApplicationService {

    public Order getOrderById(String orderId) {
        return orderRepository.findById(OrderId.of(orderId))
                       .orElseThrow(() -> new OrderNotFoundException(orderId));
    }

}
```

**❌ 禁止**: 吞噬异常或返回null

```java
// ❌ 禁止：返回null
public Order getOrderById(String orderId) {
    return orderRepository.findById(OrderId.of(orderId)).orElse(null);
}

// ❌ 禁止：吞噬异常
public Order getOrderById(String orderId) {
    try {
        return orderRepository.findById(OrderId.of(orderId)).orElseThrow();
    } catch (Exception e) {
        log.error("查询失败", e);
        return null; // 吞噬异常
    }
}
```

### 4. DTO转换

**✅ 推荐**: 使用MapStruct或手动转换

```java
// 使用MapStruct
@Mapper(componentModel = "spring")
public interface OrderConverter {

    Order toOrder(OrderCreateRequest request);

}

// 或手动转换
public class OrderApplicationService {

    private Order toOrder(OrderCreateRequest request) {
        return Order.builder()
                       .customerId(CustomerId.of(request.getCustomerId()))
                       .items(convertItems(request.getItems()))
                       .build();
    }

}
```

**❌ 禁止**: 在Domain对象中添加转换方法

```java
// ❌ 禁止：在Domain对象中添加转换方法
public class Order extends AggregateRoot {

    public OrderCreateRequest toRequest() {
        // 领域对象不应该知道DTO
    }

}
```

---

## See Also

- [代码编写规范.md - 分层开发规范](../../代码编写规范.md#5-分层开发规范)
- [代码编写规范.md - CQRS模式](../../代码编写规范.md#8-代码设计原则)
- [domain/README.md - 领域层](../domain/README.md)
- [infrastructure/README.md - 基础设施层](../infrastructure/README.md)
- [README.md - 架构概览](../../README.md#1-架构概览)

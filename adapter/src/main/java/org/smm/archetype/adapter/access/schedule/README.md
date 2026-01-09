# 事件重试处理器使用指南

## 概述

`EventRetryHandler` 是用于处理特定领域事件类型重试逻辑的策略接口。每个处理器负责处理一种特定的领域事件类型（如`OrderCreatedEvent`、
`PaymentCompletedEvent`等）。

## 设计原则

### HandlerType = 事件类型

`getHandlerType()` 方法返回的值**必须与领域事件的类名完全一致**：

```java
// ✅ 正确示例
@Override
public String getHandlerType() {
    return "OrderCreatedEvent";  // 对应 OrderCreatedEvent.java
}

// ❌ 错误示例
@Override
public String getHandlerType() {
    return "ORDER_PUBLISH";  // 不是事件类型！
}
```

### 通用处理器 vs 专用处理器

**通用处理器**（已有的）：

- `EventPublishRetryHandler` - 处理所有事件发布的失败重试
- `EventConsumeRetryHandler` - 处理所有事件消费的失败重试

**专用处理器**（按需添加）：

- `OrderCreatedEventRetryHandler` - 专门处理订单创建事件
- `PaymentCompletedEventRetryHandler` - 专门处理支付完成事件
- `InventoryUpdatedEventRetryHandler` - 专门处理库存更新事件

## 创建专用处理器

### 步骤1：创建处理器类

```java
package org.smm.archetype.adapter.access.schedule;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class YourDomainEventRetryHandler implements EventRetryScheduler {

    @Override
    public void retryFailedEvents() {
        // 实现重试逻辑
    }

    @Override
    public String getHandlerType() {
        return "YourDomainEvent";  // 必须与事件类名一致
    }

    @Override
    public int getPriority() {
        return 10;  // 根据业务重要性设置
    }

    @Override
    public boolean isEnabled() {
        return true;  // 可以从配置读取
    }

}
```

### 步骤2：添加@Component注解

Spring会自动发现并注册到`EventRetryHandlerFactory`。

### 步骤3：配置优先级

```java

@Override
public int getPriority() {
    // 核心业务：1-10
    // 重要业务：11-50
    // 一般业务：51-100
    return 10;
}
```

### 步骤4：实现重试逻辑

```java

@Override
@Async("ioTaskExecutor")
public void retryFailedEvents() {
    // 1. 查询失败的YourDomainEvent
    List<EventPublishDO> failedEvents = eventPublishMapper.selectListByQuery(
            QueryWrapper.create()
                    .where("status = ? AND type = ?",
                            EventStatus.CREATED.name(),
                            getHandlerType())  // 按事件类型过滤
                    .orderBy("occurred_on", false)
                    .limit(BATCH_SIZE)
    );

    // 2. 并行处理
    CompletableFuture.allOf(
            failedEvents.stream()
                    .map(eventDO -> CompletableFuture.runAsync(() -> {
                        // 3. 调用EventPublisher重新发布
                        retriablePublisher.republish(eventDO.getEventId());
                    }))
                    .toArray(CompletableFuture[]::new)
    ).join();
}
```

## 完整示例

参考 `OrderCreatedEventRetryHandler.java`：

- 位置：`adapter/src/main/java/org/smm/archetype/adapter/access/schedule/example/`
- 功能：专门处理订单创建事件的重试
- 特点：
    - 优先级设置为5（核心业务）
    - 最大重试次数为5（可定制）
    - 失败后触发告警和人工介入
    - 使用`@Profile("!prod")`仅在非生产环境启用

## 工厂方法使用

### 根据事件类型获取处理器

```java

@Autowired
private EventRetryHandlerFactory handlerFactory;

public void someMethod() {
    // 获取OrderCreatedEvent的处理器
    EventRetryHandler handler = handlerFactory.getHandlerByEventType("OrderCreatedEvent");

    if (handler != null && handler.isEnabled()) {
        handler.retryFailedEvents();
    }
}
```

### 检查是否支持某事件类型

```java
if(handlerFactory.hasHandlerForEvent("OrderCreatedEvent")){
        // 存在专用处理器
        }
```

### 获取所有支持的事件类型

```java
Set<String> eventTypes = handlerFactory.getSupportedEventTypes();
// ["OrderCreatedEvent", "PaymentCompletedEvent", ...]
```

### 手动触发重试

```java

@Autowired
private EventRetryScheduler scheduler;

public void manualRetry() {
    // 手动触发OrderCreatedEvent的重试
    scheduler.retryEventByType("OrderCreatedEvent");
}
```

## 配置管理

### 启用/禁用处理器

**方式1：代码控制**

```java

@Override
public boolean isEnabled() {
    return true;  // 从配置或数据库读取
}
```

**方式2：Profile控制**

```java

@Component
@Profile("feature-order-retry")  // 仅在特定Profile启用
public class OrderCreatedEventRetryHandler implements EventRetryHandler {
    // ...
}
```

**方式3：配置文件**

```yaml
event:
  retry:
    OrderCreatedEvent:
      enabled: true
      maxRetryTimes: 5
      priority: 5
```

## 最佳实践

### ✅ DO

1. **HandlerType与事件类名完全一致**
2. **设置合理的优先级**（核心业务1-10，重要业务11-50，一般51-100）
3. **使用@Async注解**，异步执行不阻塞定时任务
4. **添加详细日志**，便于问题排查
5. **实现失败处理逻辑**，特别是核心业务事件
6. **使用CompletableFuture并行处理**，提高吞吐量

### ❌ DON'T

1. **不要将HandlerType设置为处理器的功能分类**（如"EVENT_PUBLISH"）
2. **不要在循环中进行数据库查询**（应该批量查询后并行处理）
3. **不要忽略异常处理**（每个事件都要try-catch）
4. **不要使用synchronized**（使用数据库锁或分布式锁）
5. **不要硬编码配置值**（从配置文件读取）

## 注意事项

1. **循环依赖**：不能通过构造函数注入`EventPublisher`，使用`@Autowired`字段注入
2. **事务**：重试方法不需要`@Transactional`，因为这是异步执行
3. **性能**：使用IO线程池，避免阻塞定时任务线程
4. **监控**：添加Metrics统计重试次数、成功率等
5. **告警**：核心业务事件重试失败应该触发告警

## 扩展阅读

- 策略模式：[Design Patterns - Strategy Pattern](https://refactoring.guru/design-patterns/strategy)
- Spring异步：[@Async Documentation](https://docs.spring.io/spring-framework/reference/integration/scheduling.html)
- 事件驱动架构：[Event-Driven Architecture](https://martinfowler.com/articles/201701-event-driven.html)

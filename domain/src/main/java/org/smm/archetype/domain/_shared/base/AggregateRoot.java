package org.smm.archetype.domain._shared.base;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * 聚合根基类
 *
 * <p>聚合根特征：
 * <ul>
 *   <li>是聚合的入口点</li>
 *   <li>有全局唯一标识</li>
 *   <li>负责维护聚合内部的一致性边界</li>
 *   <li>外部对象只能持有聚合根的引用</li>
 * </ul>
 *
 * <p>使用示例：
 * <pre>{@code
 * public class Order extends AggregateRoot {
 *     private Long orderId;
 *     private CustomerId customerId;
 *     private List<OrderItem> items;
 *     private OrderStatus status;
 *
 *     public static Order create(CustomerId customerId, List<OrderItem> items) {
 *         Order order = new Order();
 *         order.orderId = Long.generate();
 *         order.customerId = customerId;
 *         order.items = items;
 *         order.status = OrderStatus.CREATED;
 *
 *         // 发布领域事件
 *         order.addDomainEvent(new OrderCreatedEvent(order.orderId, customerId));
 *
 *         return order;
 *     }
 * }
 * }</pre>
 * @author Leonardo
 * @since 2025/12/30
 */
@Slf4j
@Getter
@SuperBuilder(setterPrefix = "set")
public abstract class AggregateRoot extends Entity {

    /**
     * 受保护的默认构造函数
     * <p>供子类工厂方法使用
     */
    protected AggregateRoot() {
        super();
    }

    /**
     * 领域事件列表
     */
    private final List<DomainEvent> domainEvents = new ArrayList<>();

    /**
     * 添加领域事件
     *
     * <p>在聚合根的业务方法中调用此方法来记录领域事件。
     * 事件会在聚合根保存时通过EventPublisher发布。
     * @param event 领域事件
     */
    protected void addDomainEvent(DomainEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("Domain event cannot be null");
        }
        this.domainEvents.add(event);
        log.debug("Added domain event: {} to aggregate: {}", event.getClass().getSimpleName(), this.getClass().getSimpleName());
    }

    /**
     * 获取未提交的领域事件
     *
     * <p>此方法由基础设施层在保存聚合后调用，用于发布事件。
     * @return 未提交的领域事件列表（不可修改）
     */
    public List<DomainEvent> getUncommittedEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    /**
     * 标记所有事件为已提交
     *
     * <p>此方法由基础设施层在成功发布事件后调用。
     */
    public void markEventsAsCommitted() {
        this.domainEvents.clear();
        log.debug("All domain events committed for aggregate: {}", this.getClass().getSimpleName());
    }

    /**
     * 检查是否有未提交的事件
     * @return 如果有未提交的事件返回true
     */
    public boolean hasUncommittedEvents() {
        return !domainEvents.isEmpty();
    }

    /**
     * 生成唯一ID
     *
     * <p>默认使用UUID，子类可以重写使用其他ID生成策略。
     * @return 唯一ID
     */
    protected String generateId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

}

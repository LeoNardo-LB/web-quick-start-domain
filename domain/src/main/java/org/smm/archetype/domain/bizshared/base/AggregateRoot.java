package org.smm.archetype.domain.bizshared.base;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import org.smm.archetype.domain.bizshared.event.DomainEventPublisher;
import org.smm.archetype.domain.bizshared.event.Event;
import org.smm.archetype.domain.bizshared.event.Status;
import org.smm.archetype.domain.bizshared.event.dto.DomainEventDTO;

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
 *         order.addDomainEvent(new OrderCreatedEventDTO(order.orderId, customerId));
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
@SuperBuilder(setterPrefix = "set", builderMethodName = "ABuilder")
public abstract class AggregateRoot extends Entity {

    private final DomainEventPublisher domainEventPublisher;

    /**
     * 添加领域事件
     *
     * <p>在聚合根的业务方法中调用此方法来记录领域事件。
     * 事件会在聚合根保存时通过EventPublisher发布。
     * @param eventDTO 领域事件DTO
     */
    protected void addEvent(DomainEventDTO eventDTO) {
        if (eventDTO == null) {
            throw new IllegalArgumentException("Domain event cannot be null");
        }
        // 如果没有配置事件发布器，仅记录日志（支持单元测试场景）
        if (domainEventPublisher == null) {
            log.debug("DomainEventPublisher not set, skipping event publish: {} from aggregate: {}",
                    eventDTO.getClass().getSimpleName(), this.getClass().getSimpleName());
            return;
        }
        Event<DomainEventDTO> event = Event.<DomainEventDTO>builder()
                                              .setEid(UUID.randomUUID().toString())
                                              .setOccurredOn(java.time.Instant.now())
                                              .setStatus(Status.CREATED)
                                              .setPayload(eventDTO)
                                              .build();
        domainEventPublisher.publish(event);
        log.debug("Added domain event: {} to aggregate: {}", eventDTO.getClass().getSimpleName(), this.getClass().getSimpleName());
    }

    /**
     * 聚合根类型枚举
     *
     * <p>定义系统中所有聚合根的类型，用于事件溯源和领域事件处理。
     * @author Leonardo
     * @since 2026/01/09
     */
    @Getter
    public enum AggregateType {

        /**
         * 未知类型
         */
        UNKNOWN

    }

}

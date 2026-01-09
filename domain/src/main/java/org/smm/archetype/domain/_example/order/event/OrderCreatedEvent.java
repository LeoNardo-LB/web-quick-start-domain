package org.smm.archetype.domain._example.order.event;

import lombok.Getter;
import org.smm.archetype.domain._example.order.model.Money;
import org.smm.archetype.domain._shared.base.DomainEvent;

/**
 * 订单创建事件
 *
 * <p>领域事件命名：使用过去式，表示已经发生的事实
 * @author Leonardo
 * @since 2025/12/30
 */
@Getter
public class OrderCreatedEvent extends DomainEvent {

    private final Long  orderId;
    private final Long  customerId;
    private final Money totalAmount;

    public OrderCreatedEvent(Long orderId, Long customerId, Money totalAmount) {
        super();
        this.orderId = orderId;
        this.customerId = customerId;
        this.totalAmount = totalAmount;
    }

}

package org.smm.archetype.domain.example.order.event;

import lombok.Getter;
import org.smm.archetype.domain._shared.base.DomainEvent;

/**
 * 订单完成事件
 * @author Leonardo
 * @since 2025/12/30
 */
@Getter
public class OrderCompletedEvent extends DomainEvent {

    private final Long orderId;
    private final Long customerId;

    public OrderCompletedEvent(Long orderId, Long customerId) {
        super();
        this.orderId = orderId;
        this.customerId = customerId;
    }

}

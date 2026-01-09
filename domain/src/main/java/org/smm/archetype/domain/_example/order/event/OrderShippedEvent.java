package org.smm.archetype.domain._example.order.event;

import lombok.Getter;
import org.smm.archetype.domain._shared.base.DomainEvent;

/**
 * 订单发货事件
 * @author Leonardo
 * @since 2025/12/30
 */
@Getter
public class OrderShippedEvent extends DomainEvent {

    private final Long   orderId;
    private final Long   customerId;
    private final String trackingNumber;

    public OrderShippedEvent(Long orderId, Long customerId, String trackingNumber) {
        super();
        this.orderId = orderId;
        this.customerId = customerId;
        this.trackingNumber = trackingNumber;
    }

}

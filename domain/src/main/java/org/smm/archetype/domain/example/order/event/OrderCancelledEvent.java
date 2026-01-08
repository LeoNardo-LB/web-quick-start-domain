package org.smm.archetype.domain.example.order.event;

import lombok.Getter;
import org.smm.archetype.domain._shared.base.DomainEvent;

/**
 * 订单取消事件
 * @author Leonardo
 * @since 2025/12/30
 */
@Getter
public class OrderCancelledEvent extends DomainEvent {

    private final Long   orderId;
    private final Long   customerId;
    private final String reason;

    public OrderCancelledEvent(Long orderId, Long customerId, String reason) {
        super();
        this.orderId = orderId;
        this.customerId = customerId;
        this.reason = reason;
    }

}

package org.smm.archetype.domain.example.order.event;

import lombok.Getter;
import org.smm.archetype.domain._shared.base.DomainEvent;
import org.smm.archetype.domain.example.order.model.Money;

/**
 * 订单支付事件
 * @author Leonardo
 * @since 2025/12/30
 */
@Getter
public class OrderPaidEvent extends DomainEvent {

    private final Long   orderId;
    private final Long   customerId;
    private final Money  totalAmount;
    private final String paymentMethod;

    public OrderPaidEvent(Long orderId, Long customerId, Money totalAmount, String paymentMethod) {
        super();
        this.orderId = orderId;
        this.customerId = customerId;
        this.totalAmount = totalAmount;
        this.paymentMethod = paymentMethod;
    }

}

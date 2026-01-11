package org.smm.archetype.domain._example.order.model.event;

import lombok.Getter;
import org.smm.archetype.domain._shared.base.DomainEvent;

/**
 * 订单发货事件
 *
 * <p>当订单发货时发布此事件
 * @author Leonardo
 * @since 2026/1/11
 */
@Getter
public class OrderShippedEvent extends DomainEvent {

    /**
     * 订单ID
     */
    private final Long orderId;

    /**
     * 订单编号
     */
    private final String orderNo;

    /**
     * 客户ID
     */
    private final String customerId;

    /**
     * 物流单号
     */
    private final String trackingNumber;

    /**
     * 发货时间
     */
    private final String shippedTime;

    /**
     * 构造函数
     */
    public OrderShippedEvent(
            Long orderId,
            String orderNo,
            String customerId,
            String trackingNumber,
            String shippedTime) {
        super();
        this.orderId = orderId;
        this.orderNo = orderNo;
        this.customerId = customerId;
        this.trackingNumber = trackingNumber;
        this.shippedTime = shippedTime;
    }

    @Override
    public String toString() {
        return String.format("OrderShippedEvent{orderId=%d, orderNo='%s', trackingNumber='%s'}",
                orderId, orderNo, trackingNumber);
    }

}

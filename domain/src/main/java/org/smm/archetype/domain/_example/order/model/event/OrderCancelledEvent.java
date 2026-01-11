package org.smm.archetype.domain._example.order.model.event;

import lombok.Getter;
import org.smm.archetype.domain._shared.base.DomainEvent;
import org.smm.archetype.domain._shared.event.EventPriority;

import java.time.Instant;

/**
 * 订单取消事件
 *
 * <p>当订单取消时发布此事件
 * @author Leonardo
 * @since 2026/1/11
 */
@Getter
public class OrderCancelledEvent extends DomainEvent {

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
     * 取消原因
     */
    private final String reason;

    /**
     * 取消时间
     */
    private final Instant cancelledTime;

    /**
     * 构造函数
     */
    public OrderCancelledEvent(
            Long orderId,
            String orderNo,
            String customerId,
            String reason,
            Instant cancelledTime) {
        super();
        this.orderId = orderId;
        this.orderNo = orderNo;
        this.customerId = customerId;
        this.reason = reason;
        this.cancelledTime = cancelledTime;
        // 订单取消事件为高优先级，需要立即处理库存释放
        setPriority(EventPriority.HIGH);
    }

    @Override
    public String toString() {
        return String.format("OrderCancelledEvent{orderId=%d, orderNo='%s', reason='%s'}",
                orderId, orderNo, reason);
    }

}

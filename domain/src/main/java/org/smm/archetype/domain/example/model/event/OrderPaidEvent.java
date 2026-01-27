package org.smm.archetype.domain.example.model.event;

import lombok.Getter;
import org.smm.archetype.domain.example.model.PaymentMethod;
import org.smm.archetype.domain.example.model.valueobject.Money;
import org.smm.archetype.domain.bizshared.base.DomainEvent;

import java.time.Instant;

/**
 * 订单支付事件
 *
 * <p>当订单支付成功时发布此事件
 * @author Leonardo
 * @since 2026/1/11
 */
@Getter
public class OrderPaidEvent extends DomainEvent {

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
     * 支付金额
     */
    private final Money paymentAmount;

    /**
     * 支付方式
     */
    private final PaymentMethod paymentMethod;

    /**
     * 支付时间
     */
    private final Instant paymentTime;

    /**
     * 第三方交易ID
     */
    private final String transactionId;

    /**
     * 构造函数
     */
    public OrderPaidEvent(
            Long orderId,
            String orderNo,
            String customerId,
            Money paymentAmount,
            PaymentMethod paymentMethod,
            Instant paymentTime,
            String transactionId) {
        super();
        this.orderId = orderId;
        this.orderNo = orderNo;
        this.customerId = customerId;
        this.paymentAmount = paymentAmount;
        this.paymentMethod = paymentMethod;
        this.paymentTime = paymentTime;
        this.transactionId = transactionId;
    }

    @Override
    public String toString() {
        return String.format("OrderPaidEvent{orderId=%d, orderNo='%s', paymentAmount=%s, paymentMethod=%s}",
                orderId, orderNo, paymentAmount, paymentMethod);
    }

}

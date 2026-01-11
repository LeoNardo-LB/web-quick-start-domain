package org.smm.archetype.app._example.order.command;

import org.smm.archetype.domain._example.order.model.PaymentMethod;
import org.smm.archetype.domain._example.order.model.valueobject.Money;
import org.smm.archetype.domain._shared.base.Command;

/**
 * 支付订单命令
 * @author Leonardo
 * @since 2026/1/11
 */
public class PayOrderCommand implements Command {

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 支付方式
     */
    private PaymentMethod paymentMethod;

    /**
     * 支付金额
     */
    private Money paymentAmount;

    public PayOrderCommand() {
    }

    public PayOrderCommand(Long orderId, PaymentMethod paymentMethod, Money paymentAmount) {
        this.orderId = orderId;
        this.paymentMethod = paymentMethod;
        this.paymentAmount = paymentAmount;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public Money getPaymentAmount() {
        return paymentAmount;
    }

    public void setPaymentAmount(Money paymentAmount) {
        this.paymentAmount = paymentAmount;
    }

}

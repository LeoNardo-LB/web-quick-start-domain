package org.smm.archetype.app.exampleorder.command;

import lombok.Getter;
import lombok.Setter;
import org.smm.archetype.domain.exampleorder.model.PaymentMethod;
import org.smm.archetype.domain.exampleorder.model.valueobject.Money;
import org.smm.archetype.domain.shared.base.Command;

/**
 * 支付订单命令，包含订单ID和支付信息。
 */
@Setter
@Getter
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

    /**
     * 全参数构造方法
     * @param orderId       订单ID
     * @param paymentMethod 支付方式
     * @param paymentAmount 支付金额
     */
    public PayOrderCommand(Long orderId, PaymentMethod paymentMethod, Money paymentAmount) {
        this.orderId = orderId;
        this.paymentMethod = paymentMethod;
        this.paymentAmount = paymentAmount;
    }

}

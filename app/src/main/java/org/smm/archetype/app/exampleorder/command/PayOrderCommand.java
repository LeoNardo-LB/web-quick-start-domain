package org.smm.archetype.app.exampleorder.command;

import lombok.Getter;
import lombok.Setter;
import org.smm.archetype.domain.shared.base.Command;
import org.smm.archetype.domain.exampleorder.model.PaymentMethod;
import org.smm.archetype.domain.exampleorder.model.valueobject.Money;

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

    public PayOrderCommand(Long orderId, PaymentMethod paymentMethod, Money paymentAmount) {
        this.orderId = orderId;
        this.paymentMethod = paymentMethod;
        this.paymentAmount = paymentAmount;
    }

}

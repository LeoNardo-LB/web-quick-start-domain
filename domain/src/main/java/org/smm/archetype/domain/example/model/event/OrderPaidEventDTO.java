package org.smm.archetype.domain.example.model.event;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.smm.archetype.domain.bizshared.event.dto.DomainEventDTO;
import org.smm.archetype.domain.example.model.PaymentMethod;
import org.smm.archetype.domain.example.model.valueobject.Money;

import java.time.Instant;

/**
 * 订单支付事件
 *
 * <p>当订单支付成功时发布此事件
 * @author Leonardo
 * @since 2026/1/11
 */
@Getter
@SuperBuilder(setterPrefix = "set", builderMethodName = "OPEBuilder")
public class OrderPaidEventDTO extends DomainEventDTO {

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

}

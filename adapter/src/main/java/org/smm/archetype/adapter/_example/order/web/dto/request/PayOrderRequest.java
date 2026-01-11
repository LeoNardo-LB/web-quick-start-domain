package org.smm.archetype.adapter._example.order.web.dto.request;

import lombok.Getter;
import lombok.Setter;
import org.smm.archetype.domain._example.order.model.PaymentMethod;
import org.smm.archetype.domain._example.order.model.valueobject.Money;

/**
 * 支付订单请求
 * @author Leonardo
 * @since 2026/1/11
 */
@Getter
@Setter
public class PayOrderRequest {

    /**
     * 支付方式
     */
    private PaymentMethod paymentMethod;

    /**
     * 支付金额
     */
    private Money paymentAmount;

}

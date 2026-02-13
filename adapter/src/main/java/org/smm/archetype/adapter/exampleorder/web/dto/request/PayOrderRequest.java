package org.smm.archetype.adapter.exampleorder.web.dto.request;

import lombok.Getter;
import lombok.Setter;
import org.smm.archetype.domain.exampleorder.model.PaymentMethod;
import org.smm.archetype.domain.exampleorder.model.valueobject.Money;

/**
 * 支付订单请求


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

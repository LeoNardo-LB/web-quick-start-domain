package org.smm.archetype.app.exampleorder.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * 退款订单结果DTO
 */
@Getter
@SuperBuilder(setterPrefix = "set", builderMethodName = "RORBuilder")
public class RefundOrderResultDTO {

    /**
     * 订单ID
     */
    private final Long orderId;

    /**
     * 订单编号
     */
    private final String orderNo;

    /**
     * 订单状态
     */
    private final String status;

    /**
     * 本次退款金额
     */
    private final BigDecimal refundAmount;

    /**
     * 币种
     */
    private final String currency;

    /**
     * 退款类型
     */
    private final String refundType;

    /**
     * 退款原因
     */
    private final String refundReason;

    /**
     * 退款时间
     */
    private final Instant refundedTime;

    /**
     * 累计已退款金额
     */
    private final BigDecimal totalRefundedAmount;

}

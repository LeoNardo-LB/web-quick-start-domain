package org.smm.archetype.domain.exampleorder.model.event;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.smm.archetype.domain.exampleorder.model.RefundType;
import org.smm.archetype.domain.exampleorder.model.valueobject.Money;
import org.smm.archetype.domain.shared.event.dto.DomainEventDTO;

import java.time.Instant;

/**
 * 订单退款事件
 *
 * 当订单退款时发布此事件
 */
@Getter
@SuperBuilder(setterPrefix = "set", builderMethodName = "OREBuilder")
public class OrderRefundEventDTO extends DomainEventDTO {

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
     * 退款金额
     */
    private final Money refundAmount;

    /**
     * 退款类型（全额/部分）
     */
    private final RefundType refundType;

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
    private final Money totalRefundedAmount;

}

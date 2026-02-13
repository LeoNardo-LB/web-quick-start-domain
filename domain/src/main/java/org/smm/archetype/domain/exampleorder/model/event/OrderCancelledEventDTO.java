package org.smm.archetype.domain.exampleorder.model.event;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.smm.archetype.domain.shared.event.dto.DomainEventDTO;

import java.time.Instant;

/**
 * 订单取消事件
 *
当订单取消时发布此事件


 */
@Getter
@SuperBuilder(setterPrefix = "set", builderMethodName = "OCEBuilder")
public class OrderCancelledEventDTO extends DomainEventDTO {

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

}

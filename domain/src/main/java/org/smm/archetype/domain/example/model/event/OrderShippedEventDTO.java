package org.smm.archetype.domain.example.model.event;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.smm.archetype.domain.bizshared.event.dto.DomainEventDTO;

/**
 * 订单发货事件
 *
 * <p>当订单发货时发布此事件


 */
@Getter
@SuperBuilder(setterPrefix = "set", builderMethodName = "OSEBuilder")
public class OrderShippedEventDTO extends DomainEventDTO {

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
     * 物流单号
     */
    private final String trackingNumber;

    /**
     * 发货时间
     */
    private final String shippedTime;

}

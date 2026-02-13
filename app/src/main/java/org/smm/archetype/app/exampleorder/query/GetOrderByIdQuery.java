package org.smm.archetype.app.exampleorder.query;

import lombok.Builder;
import lombok.Getter;
import org.smm.archetype.domain.shared.base.Query;

/**
 * 根据ID查询订单的查询对象。
 */
@Getter
@Builder(setterPrefix = "set")
public class GetOrderByIdQuery implements Query {

    /**
     * 订单ID
     */
    private Long orderId;

}

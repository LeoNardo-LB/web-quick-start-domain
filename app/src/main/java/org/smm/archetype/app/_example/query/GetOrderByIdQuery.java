package org.smm.archetype.app._example.query;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.smm.archetype.domain.bizshared.base.Query;

/**
 * 根据ID查询订单
 * @author Leonardo
 * @since 2026/1/11
 */
@Getter
@Builder(setterPrefix = "set")
public class GetOrderByIdQuery implements Query {

    /**
     * 订单ID
     */
    private Long orderId;

}

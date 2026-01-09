package org.smm.archetype.app._example.order.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.smm.archetype.domain._shared.base.Query;

/**
 * 查询订单查询
 * @author Leonardo
 * @since 2025/12/30
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetOrderQuery implements Query {

    /**
     * 订单ID
     */
    private Long orderId;

}

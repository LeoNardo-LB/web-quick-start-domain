package org.smm.archetype.app.example.order.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.smm.archetype.domain._shared.base.Query;

/**
 * 查询订单查询
 * @author Leonardo
 * @since 2025/12/30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetOrderQuery implements Query {

    /**
     * 订单ID
     */
    private Long orderId;

}

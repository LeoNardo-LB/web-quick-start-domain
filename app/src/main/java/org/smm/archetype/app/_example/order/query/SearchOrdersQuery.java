package org.smm.archetype.app._example.order.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.smm.archetype.domain._example.order.model.OrderStatus;
import org.smm.archetype.domain._shared.base.Query;

/**
 * 搜索订单查询
 * @author Leonardo
 * @since 2025/12/30
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchOrdersQuery implements Query {

    /**
     * 客户ID
     */
    private Long customerId;

    /**
     * 订单状态
     */
    private OrderStatus status;

    /**
     * 页码（从1开始）
     */
    private Integer pageNumber;

    /**
     * 每页大小
     */
    private Integer pageSize;

}

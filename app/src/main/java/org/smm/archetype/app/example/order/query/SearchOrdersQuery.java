package org.smm.archetype.app.example.order.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.smm.archetype.domain._shared.base.Query;
import org.smm.archetype.domain.example.order.model.OrderStatus;

/**
 * 搜索订单查询
 * @author Leonardo
 * @since 2025/12/30
 */
@Data
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

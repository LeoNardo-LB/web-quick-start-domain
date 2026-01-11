package org.smm.archetype.app._example.order.query;

import org.smm.archetype.domain._shared.base.Query;

/**
 * 根据ID查询订单
 * @author Leonardo
 * @since 2026/1/11
 */
public class GetOrderByIdQuery implements Query {

    /**
     * 订单ID
     */
    private Long orderId;

    public GetOrderByIdQuery() {
    }

    public GetOrderByIdQuery(Long orderId) {
        this.orderId = orderId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

}

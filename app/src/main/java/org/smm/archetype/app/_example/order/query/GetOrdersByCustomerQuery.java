package org.smm.archetype.app._example.order.query;

import org.smm.archetype.domain._shared.base.Query;

/**
 * 查询客户订单列表
 * @author Leonardo
 * @since 2026/1/11
 */
public class GetOrdersByCustomerQuery implements Query {

    /**
     * 客户ID
     */
    private String customerId;

    public GetOrdersByCustomerQuery() {
    }

    public GetOrdersByCustomerQuery(String customerId) {
        this.customerId = customerId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

}

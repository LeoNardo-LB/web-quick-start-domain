package org.smm.archetype.app._example.query;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import org.smm.archetype.domain.bizshared.base.Query;

/**
 * 查询客户订单列表
 * @author Leonardo
 * @since 2026/1/11
 */
@Getter
@Builder(setterPrefix = "set")
public class GetOrdersByCustomerQuery implements Query {

    /**
     * 客户ID
     */
    private String customerId;

}

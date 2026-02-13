package org.smm.archetype.app.exampleorder.query;

import lombok.Builder;
import lombok.Getter;
import org.smm.archetype.domain.shared.base.Query;

/**
 * 查询客户订单列表的查询对象。
 */
@Getter
@Builder(setterPrefix = "set")
public class GetOrdersByCustomerQuery implements Query {

    /**
     * 客户ID
     */
    private String customerId;

}

package org.smm.archetype.app.exampleorder.query;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.smm.archetype.domain.shared.base.PageRequest;

/**
 * 订单列表查询对象，支持分页。
 */
@Getter
@SuperBuilder(setterPrefix = "set", builderMethodName = "OLQBuilder")
public class OrderListQuery extends PageRequest {

    /**
     * 客户ID（可选）
     */
    private String customerId;

}

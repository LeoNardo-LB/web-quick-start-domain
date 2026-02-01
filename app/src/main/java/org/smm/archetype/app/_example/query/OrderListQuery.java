package org.smm.archetype.app._example.query;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.smm.archetype.domain.bizshared.base.PageRequest;

/**
 * 订单列表查询
 * @author Leonardo
 * @since 2026/1/11
 */
@Getter
@SuperBuilder(setterPrefix = "set", builderMethodName = "OLQBuilder")
public class OrderListQuery extends PageRequest {

    /**
     * 客户ID（可选）
     */
    private String customerId;

}

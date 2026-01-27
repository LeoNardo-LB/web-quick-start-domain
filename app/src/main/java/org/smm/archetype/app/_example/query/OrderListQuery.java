package org.smm.archetype.app._example.query;

import lombok.Data;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.smm.archetype.domain.bizshared.base.BasePageRequest;

/**
 * 订单列表查询
 * @author Leonardo
 * @since 2026/1/11
 */
@Getter
@SuperBuilder(builderMethodName = "orderListQueryBuilder", setterPrefix = "set")
public class OrderListQuery extends BasePageRequest {

    /**
     * 客户ID（可选）
     */
    private String customerId;

}

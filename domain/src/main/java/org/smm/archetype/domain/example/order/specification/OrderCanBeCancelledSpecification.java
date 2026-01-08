package org.smm.archetype.domain.example.order.specification;

import org.smm.archetype.domain._shared.specification.Specification;
import org.smm.archetype.domain.example.order.model.Order;

/**
 * 订单可以取消的规格
 *
 * <p>示例：展示如何使用规格模式封装业务规则
 * @author Leonardo
 * @since 2025/12/30
 */
public class OrderCanBeCancelledSpecification implements Specification<Order> {

    @Override
    public boolean isSatisfiedBy(Order order) {
        // 规则1: 订单状态必须是已创建或已支付
        if (order.getStatus().canCancel()) {
            return false;
        }

        // 规则2: 订单创建时间不超过24小时
        if (order.getCreateTime() != null) {
            long hoursSinceCreation = java.time.Duration.between(
                    order.getCreateTime(),
                    java.time.Instant.now()
            ).toHours();

            return hoursSinceCreation <= 24;
        }

        return true;
    }

}

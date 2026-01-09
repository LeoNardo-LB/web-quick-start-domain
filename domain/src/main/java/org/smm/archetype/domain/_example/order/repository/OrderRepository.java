package org.smm.archetype.domain._example.order.repository;

import org.smm.archetype.domain._example.order.model.Order;
import org.smm.archetype.domain._example.order.model.OrderStatus;
import org.smm.archetype.domain._shared.base.BaseRepository;

import java.util.List;

/**
 * 订单仓储接口
 *
 * <p>示例：展示如何定义特定聚合的仓储
 * @author Leonardo
 * @since 2025/12/30
 */
public interface OrderRepository extends BaseRepository<Order> {

    /**
     * 根据客户ID查询订单列表
     * @param customerId 客户ID
     * @return 订单列表
     */
    List<Order> findByCustomerId(Long customerId);

    /**
     * 根据订单状态查询订单列表
     * @param status 订单状态
     * @return 订单列表
     */
    List<Order> findByStatus(OrderStatus status);

    /**
     * 根据客户ID和状态查询订单列表
     * @param customerId 客户ID
     * @param status     订单状态
     * @return 订单列表
     */
    List<Order> findByCustomerIdAndStatus(Long customerId, OrderStatus status);

}

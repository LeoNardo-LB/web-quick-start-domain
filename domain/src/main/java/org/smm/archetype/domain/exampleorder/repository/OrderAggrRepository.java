package org.smm.archetype.domain.exampleorder.repository;

import org.smm.archetype.domain.shared.base.PageResult;
import org.smm.archetype.domain.exampleorder.model.OrderAggr;

import java.util.List;
import java.util.Optional;

/**
 * 订单仓储接口，提供订单聚合根的持久化操作。
 */
public interface OrderAggrRepository {

    /**
     * 保存订单聚合根
     * @param order 订单聚合根
     * @return 保存后的订单聚合根
     */
    OrderAggr save(OrderAggr order);

    /**
     * 根据ID查询订单
     * @param orderId 订单ID
     * @return 订单聚合根（可能为空）
     */
    Optional<OrderAggr> findById(Long orderId);

    /**
     * 根据订单号查询订单
     * @param orderNo 订单编号
     * @return 订单聚合根（可能为空）
     */
    Optional<OrderAggr> findByOrderNo(String orderNo);

    /**
     * 根据客户ID查询订单列表
     * @param customerId 客户ID
     * @return 订单列表
     */
    List<OrderAggr> findByCustomerId(String customerId);

    /**
     * 分页查询订单列表
     * @param customerId 客户ID（可选）
     * @param pageNumber 页码（从1开始）
     * @param pageSize   每页大小
     * @return 分页结果
     */
    PageResult<OrderAggr> findOrders(String customerId, Long pageNumber, Long pageSize);

    /**
     * 检查订单号是否存在
     * @param orderNo 订单编号
     * @return 存在返回true
     */
    boolean existsByOrderNo(String orderNo);

}

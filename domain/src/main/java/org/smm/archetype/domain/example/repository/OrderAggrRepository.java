package org.smm.archetype.domain.example.repository;

import org.smm.archetype.domain.example.model.OrderAggr;
import org.smm.archetype.domain.bizshared.base.BasePageResult;

import java.util.List;
import java.util.Optional;

/**
 * 订单仓储接口
 *
 * <p>职责：
 * <ul>
 *   <li>持久化和查询订单聚合根</li>
 *   <li>封装数据访问细节</li>
 *   <li>提供聚合根级别的操作</li>
 * </ul>
 * @author Leonardo
 * @since 2026/1/11
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
    BasePageResult<OrderAggr> findOrders(String customerId, Long pageNumber, Long pageSize);

    /**
     * 检查订单号是否存在
     * @param orderNo 订单编号
     * @return 存在返回true
     */
    boolean existsByOrderNo(String orderNo);

}

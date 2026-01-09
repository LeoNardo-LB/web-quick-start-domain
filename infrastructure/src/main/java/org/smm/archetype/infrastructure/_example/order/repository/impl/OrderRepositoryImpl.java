package org.smm.archetype.infrastructure._example.order.repository.impl;

import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smm.archetype.domain._example.order.model.Order;
import org.smm.archetype.domain._example.order.model.OrderItem;
import org.smm.archetype.domain._example.order.model.OrderStatus;
import org.smm.archetype.domain._example.order.repository.OrderRepository;
import org.smm.archetype.infrastructure._example.order.repository.converter.OrderConverter;
import org.smm.archetype.infrastructure._example.order.repository.converter.OrderItemConverter;
import org.smm.archetype.infrastructure._example.order.repository.entity.OrderDO;
import org.smm.archetype.infrastructure._example.order.repository.entity.OrderItemDO;
import org.smm.archetype.infrastructure._example.order.repository.mapper.OrderItemMapper;
import org.smm.archetype.infrastructure._example.order.repository.mapper.OrderMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 订单仓储实现
 *
 * <p>实现说明：
 * <ul>
 *   <li>负责Order聚合根的持久化</li>
 *   <li>处理Order与OrderItem的关联关系</li>
 *   <li>使用乐观锁保证并发安全</li>
 *   <li>使用MapStruct进行对象转换</li>
 *   <li>领域事件由切面在事务提交后统一发布</li>
 * </ul>
 *
 * @author Leonardo
 * @since 2025-12-30
 */
@Slf4j
@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepository {

    private final OrderMapper        orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final OrderConverter     orderConverter;
    private final OrderItemConverter orderItemConverter;

    @Override
    public Order save(Order aggregate) {
        log.debug("Saving order: {}", aggregate.getOrderId());

        // 使用MapStruct转换为DO
        OrderDO orderDO = orderConverter.toDataObject(aggregate);

        if (aggregate.isNew()) {
            // 新增
            orderMapper.insert(orderDO);

            // 重建Order以设置ID
            Order rebuilt = Order.reconstruct(
                    aggregate.getOrderId(),
                    aggregate.getCustomerId(),
                    aggregate.getItems(),
                    aggregate.getTotalAmount(),
                    aggregate.getStatus(),
                    aggregate.getShippingAddress(),
                    aggregate.getPhoneNumber(),
                    aggregate.getPaymentTime(),
                    aggregate.getShippingTime(),
                    aggregate.getCompletedTime(),
                    aggregate.getCancelledTime(),
                    aggregate.getCancelReason(),
                    orderDO.getId(),
                    aggregate.getCreateTime(),
                    aggregate.getUpdateTime(),
                    aggregate.getCreateUser(),
                    aggregate.getUpdateUser(),
                    aggregate.getVersion()
            );

            aggregate = rebuilt;
        } else {
            // 更新（使用乐观锁）
            int updated = orderMapper.update(orderDO);
            if (updated == 0) {
                throw new IllegalStateException("Order update failed due to optimistic lock");
            }
        }

        // 保存订单项
        saveOrderItems(aggregate);

        log.debug("Order saved successfully: {}", aggregate.getOrderId());
        return aggregate;
    }

    @Override
    public Optional<Order> findById(Long id) {
        OrderDO orderDO = orderMapper.selectOneById(id);
        if (orderDO == null) {
            return Optional.empty();
        }

        // 查询订单项
        List<OrderItemDO> itemDOs = orderItemMapper.selectListByQuery(
                QueryWrapper.create()
                        .where("order_id = ?", orderDO.getOrderId())
        );

        // 使用MapStruct重建聚合根
        return Optional.of(orderConverter.reconstructOrder(orderDO, itemDOs, orderItemConverter));
    }

    @Override
    public void deleteById(Long id) {
        log.debug("Deleting order: {}", id);

        // 删除订单项
        orderItemMapper.deleteByQuery(
                QueryWrapper.create()
                        .where("order_id = ?", id)
        );

        // 删除订单
        orderMapper.deleteById(id);

        log.debug("Order deleted successfully: {}", id);
    }

    @Override
    public List<Order> findAll() {
        List<OrderDO> orderDOList = orderMapper.selectAll();
        List<Order> orders = new ArrayList<>();

        for (OrderDO orderDO : orderDOList) {
            List<OrderItemDO> itemDOs = orderItemMapper.selectListByQuery(
                    QueryWrapper.create()
                            .where("order_id = ?", orderDO.getOrderId())
            );
            orders.add(orderConverter.reconstructOrder(orderDO, itemDOs, orderItemConverter));
        }

        return orders;
    }

    @Override
    public long count() {
        return orderMapper.selectCountByQuery(QueryWrapper.create());
    }

    @Override
    public List<Order> findByCustomerId(Long customerId) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                                            .where("customer_id = ?", customerId)
                                            .orderBy("create_time", false);

        List<OrderDO> orderDOList = orderMapper.selectListByQuery(queryWrapper);
        return toOrderList(orderDOList);
    }

    @Override
    public List<Order> findByStatus(OrderStatus status) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                                            .where("status = ?", status.name())
                                            .orderBy("create_time", false);

        List<OrderDO> orderDOList = orderMapper.selectListByQuery(queryWrapper);
        return toOrderList(orderDOList);
    }

    @Override
    public List<Order> findByCustomerIdAndStatus(Long customerId, OrderStatus status) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                                            .where("customer_id = ? AND status = ?", customerId, status.name())
                                            .orderBy("create_time", false);

        List<OrderDO> orderDOList = orderMapper.selectListByQuery(queryWrapper);
        return toOrderList(orderDOList);
    }

    /**
     * 保存订单项
     */
    private void saveOrderItems(Order order) {
        // 先删除旧的订单项
        orderItemMapper.deleteByQuery(
                QueryWrapper.create()
                        .where("order_id = ?", order.getOrderId())
        );

        // 插入新的订单项（使用MapStruct转换）
        for (OrderItem item : order.getItems()) {
            OrderItemDO itemDO = orderItemConverter.toDataObject(item);
            itemDO.setOrderId(order.getOrderId());
            orderItemMapper.insert(itemDO);
        }
    }

    /**
     * 批量转换DO列表为领域对象列表
     */
    private List<Order> toOrderList(List<OrderDO> orderDOList) {
        List<Order> orders = new ArrayList<>();
        for (OrderDO orderDO : orderDOList) {
            List<OrderItemDO> itemDOs = orderItemMapper.selectListByQuery(
                    QueryWrapper.create()
                            .where("order_id = ?", orderDO.getOrderId())
            );
            orders.add(orderConverter.reconstructOrder(orderDO, itemDOs, orderItemConverter));
        }
        return orders;
    }

}

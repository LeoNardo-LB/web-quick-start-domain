package org.smm.archetype.app._example.order.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smm.archetype.app._example.order.command.CancelOrderCommand;
import org.smm.archetype.app._example.order.command.CreateOrderCommand;
import org.smm.archetype.app._example.order.command.PayOrderCommand;
import org.smm.archetype.app._example.order.query.GetOrderQuery;
import org.smm.archetype.app._example.order.query.SearchOrdersQuery;
import org.smm.archetype.app._shared.base.ApplicationService;
import org.smm.archetype.domain._example.order.model.Order;
import org.smm.archetype.domain._example.order.repository.OrderRepository;
import org.smm.archetype.domain._example.order.specification.OrderCanBeCancelledSpecification;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 订单应用服务
 *
 * <p>应用服务职责：
 * <ul>
 *   <li>协调领域对象完成用例</li>
 *   <li>处理事务边界</li>
 *   <li>调用领域服务和仓储</li>
 *   <li>不包含业务逻辑</li>
 * </ul>
 *
 * <p>设计原则：
 * <ul>
 *   <li>薄应用服务，胖领域模型</li>
 *   <li>每个方法对应一个用例</li>
 *   <li>方法名表达业务意图</li>
 * </ul>
 * @author Leonardo
 * @since 2025/12/30
 */
@Slf4j
@RequiredArgsConstructor
public class OrderApplicationService extends ApplicationService {

    private final OrderRepository orderRepository;

    /**
     * 创建订单
     * @param command 创建订单命令
     * @return 订单ID
     */
    @Transactional
    public Long createOrder(CreateOrderCommand command) {
        log.info("Creating order for customer: {}", command.getCustomerId());

        // 1. 验证
        if (command.getItems() == null || command.getItems().isEmpty()) {
            throw new IllegalArgumentException("Order must have at least one item");
        }

        // 2. 创建订单（领域逻辑）
        Order order = Order.create(
                command.getCustomerId(),
                command.getItems(),
                command.getShippingAddress(),
                command.getPhoneNumber()
        );

        // 3. 保存订单
        orderRepository.save(order);

        // 4. 返回结果
        log.info("Order created successfully: {}", order.getOrderId());
        return order.getOrderId();
    }

    /**
     * 支付订单
     * @param command 支付订单命令
     */
    @Transactional
    public void payOrder(PayOrderCommand command) {
        log.info("Paying order: {}", command.getOrderId());

        // 1. 加载订单
        Order order = orderRepository.getById(command.getOrderId());

        // 2. 支付订单（领域逻辑）
        order.pay(command.getPaymentMethod());

        // 3. 保存变更
        orderRepository.save(order);

        log.info("Order paid successfully: {}", command.getOrderId());
    }

    /**
     * 取消订单
     * @param command 取消订单命令
     */
    @Transactional
    public void cancelOrder(CancelOrderCommand command) {
        log.info("Cancelling order: {}", command.getOrderId());

        // 1. 加载订单
        Order order = orderRepository.getById(command.getOrderId());

        // 2. 验证订单是否可以取消（规格模式）
        OrderCanBeCancelledSpecification spec = new OrderCanBeCancelledSpecification();
        if (!spec.isSatisfiedBy(order)) {
            throw new IllegalStateException("Order cannot be cancelled");
        }

        // 3. 取消订单（领域逻辑）
        order.cancel(command.getReason());

        // 4. 保存变更
        orderRepository.save(order);

        log.info("Order cancelled successfully: {}", command.getOrderId());
    }

    /**
     * 发货订单
     * @param orderId        订单ID
     * @param trackingNumber 物流单号
     */
    @Transactional
    public void shipOrder(Long orderId, String trackingNumber) {
        log.info("Shipping order: {}", orderId);

        // 1. 加载订单
        Order order = orderRepository.getById(orderId);

        // 2. 发货（领域逻辑）
        order.ship(trackingNumber);

        // 3. 保存变更
        orderRepository.save(order);

        log.info("Order shipped successfully: {}", orderId);
    }

    /**
     * 完成订单
     * @param orderId 订单ID
     */
    @Transactional
    public void completeOrder(Long orderId) {
        log.info("Completing order: {}", orderId);

        // 1. 加载订单
        Order order = orderRepository.getById(orderId);

        // 2. 完成（领域逻辑）
        order.complete();

        // 3. 保存变更
        orderRepository.save(order);

        log.info("Order completed successfully: {}", orderId);
    }

    /**
     * 查询订单
     * @param query 查询订单查询
     * @return 订单
     */
    @Transactional(readOnly = true)
    public Order getOrder(GetOrderQuery query) {
        return orderRepository.getById(query.getOrderId());
    }

    /**
     * 搜索订单
     * @param query 搜索订单查询
     * @return 订单列表
     */
    @Transactional(readOnly = true)
    public List<Order> searchOrders(SearchOrdersQuery query) {
        if (query.getCustomerId() != null && query.getStatus() != null) {
            return orderRepository.findByCustomerIdAndStatus(
                    query.getCustomerId(),
                    query.getStatus()
            );
        } else if (query.getCustomerId() != null) {
            return orderRepository.findByCustomerId(query.getCustomerId());
        } else if (query.getStatus() != null) {
            return orderRepository.findByStatus(query.getStatus());
        } else {
            return orderRepository.findAll();
        }
    }

}

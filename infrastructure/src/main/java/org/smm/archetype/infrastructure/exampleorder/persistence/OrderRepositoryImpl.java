package org.smm.archetype.infrastructure.exampleorder.persistence;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.smm.archetype.domain.exampleorder.model.OrderAggr;
import org.smm.archetype.domain.exampleorder.model.OrderStatus;
import org.smm.archetype.domain.exampleorder.model.valueobject.Money;
import org.smm.archetype.domain.exampleorder.repository.OrderAggrRepository;
import org.smm.archetype.domain.shared.base.PageResult;
import org.smm.archetype.domain.shared.base.SimplePageResult;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 订单仓储内存实现，用于测试环境。
 * <p>
 * 注意：这是测试用的内存实现，生产环境应使用数据库实现。
 */
@Slf4j
public class OrderRepositoryImpl implements OrderAggrRepository {

    /**
     * 内存存储 - 线程安全
     */
    private final Map<Long, OrderAggr> orderStore = new ConcurrentHashMap<>();

    /**
     * 初始化测试数据
     */
    @PostConstruct
    public void initTestData() {
        log.info("初始化订单测试数据...");

        // 创建测试订单1 - ID=1，已支付状态，可退款
        OrderAggr order1 = OrderAggr.OABuilder()
                                   .setId(1L)
                                   .setOrderNo("ORD_TEST_001")
                                   .setCustomerId("customer_001")
                                   .setCustomerName("测试客户1")
                                   .setStatus(OrderStatus.PAID)
                                   .setTotalAmount(Money.of(new BigDecimal("100.00"), "CNY"))
                                   .setCurrency("CNY")
                                   .setRemark("测试订单1 - 已支付")
                                   .build();
        orderStore.put(1L, order1);

        // 创建测试订单2 - ID=2，已支付状态，可部分退款
        OrderAggr order2 = OrderAggr.OABuilder()
                                   .setId(2L)
                                   .setOrderNo("ORD_TEST_002")
                                   .setCustomerId("customer_001")
                                   .setCustomerName("测试客户1")
                                   .setStatus(OrderStatus.PAID)
                                   .setTotalAmount(Money.of(new BigDecimal("100.00"), "CNY"))
                                   .setCurrency("CNY")
                                   .setRemark("测试订单2 - 已支付")
                                   .build();
        orderStore.put(2L, order2);

        log.info("订单测试数据初始化完成，共{}条", orderStore.size());
    }

    @Override
    public OrderAggr save(OrderAggr order) {
        if (order == null) {
            return null;
        }
        Long orderId = order.getId();
        if (orderId == null) {
            log.warn("订单ID为空，无法保存");
            return null;
        }
        orderStore.put(orderId, order);
        log.debug("订单保存成功: orderId={}", orderId);
        return order;
    }

    @Override
    public Optional<OrderAggr> findById(Long orderId) {
        if (orderId == null) {
            return Optional.empty();
        }
        OrderAggr order = orderStore.get(orderId);
        log.debug("查询订单: orderId={}, 结果={}", orderId, order != null ? "找到" : "未找到");
        return Optional.ofNullable(order);
    }

    @Override
    public Optional<OrderAggr> findByOrderNo(String orderNo) {
        if (orderNo == null || orderNo.isBlank()) {
            return Optional.empty();
        }
        return orderStore.values().stream()
                       .filter(order -> orderNo.equals(order.getOrderNo()))
                       .findFirst();
    }

    @Override
    public List<OrderAggr> findByCustomerId(String customerId) {
        if (customerId == null || customerId.isBlank()) {
            return List.of();
        }
        return orderStore.values().stream()
                       .filter(order -> customerId.equals(order.getCustomerId()))
                       .collect(Collectors.toList());
    }

    @Override
    public PageResult<OrderAggr> findOrders(String customerId, Long pageNumber, Long pageSize) {
        // 参数校验
        if (pageNumber == null || pageNumber < 1) {
            pageNumber = 1L;
        }
        if (pageSize == null || pageSize < 1) {
            pageSize = 10L;
        }

        // 过滤客户ID
        List<OrderAggr> filteredOrders;
        if (customerId == null || customerId.isBlank()) {
            filteredOrders = List.copyOf(orderStore.values());
        } else {
            filteredOrders = orderStore.values().stream()
                                     .filter(order -> customerId.equals(order.getCustomerId()))
                                     .collect(Collectors.toList());
        }

        // 计算总数
        long total = filteredOrders.size();

        // 分页计算
        long skip = (pageNumber - 1) * pageSize;
        List<OrderAggr> pagedOrders = filteredOrders.stream()
                                              .skip(skip)
                                              .limit(pageSize)
                                              .collect(Collectors.toList());

        log.debug("查询订单列表: customerId={}, pageNumber={}, pageSize={}, total={}",
                customerId, pageNumber, pageSize, total);

        return SimplePageResult.of(pageNumber, pageSize, pagedOrders, total);
    }

    @Override
    public boolean existsByOrderNo(String orderNo) {
        if (orderNo == null || orderNo.isBlank()) {
            return false;
        }
        return orderStore.values().stream()
                       .anyMatch(order -> orderNo.equals(order.getOrderNo()));
    }

}

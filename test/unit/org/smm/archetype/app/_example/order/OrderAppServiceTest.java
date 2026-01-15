package org.smm.archetype.app._example.order;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.smm.archetype.domain._example.order.model.aggregateroot.OrderAggr;
import org.smm.archetype.domain._example.order.model.valueobject.Money;
import org.smm.archetype.domain._example.order.model.valueobject.OrderId;
import org.smm.archetype.domain._example.order.repository.OrderAggrRepository;
import org.smm.archetype.domain._example.order.service.command.CreateOrderCommand;
import org.smm.archetype.domain._example.order.service.query.OrderQuery;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 订单应用服务单元测试示例
 *
 * <p>演示如何测试ApplicationService层：
 * <ul>
 *   <li>使用纯Mock测试（不启动Spring）</li>
 *   <li>使用@InjectMocks注入Mock依赖</li>
 *   <li>测试用例编排逻辑</li>
 *   <li>测试DTO转换</li>
 *   <li>测试异常处理</li>
 * </ul>
 *
 * @author Leonardo
 * @since 2026-01-16
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("订单应用服务单元测试示例")
class OrderAppServiceTest {

    @Mock
    private OrderAggrRepository orderRepository;

    @InjectMocks
    private OrderAppService orderAppService;

    @Test
    @DisplayName("创建订单 - 成功")
    void testCreateOrder_success() {
        // Given: 准备测试数据
        CreateOrderCommand command = new CreateOrderCommand(
            "CUST001",
            new Money(100.00, "CNY")
        );
        OrderId expectedOrderId = OrderId.generate();

        when(orderRepository.save(any(OrderAggr.class)))
            .thenReturn(expectedOrderId);

        // When: 执行被测试方法
        OrderId actualOrderId = orderAppService.createOrder(command);

        // Then: 验证结果
        assertThat(actualOrderId).isNotNull();
        assertThat(actualOrderId).isEqualTo(expectedOrderId);

        // 验证Repository方法被调用
        verify(orderRepository, times(1)).save(any(OrderAggr.class));
    }

    @Test
    @DisplayName("查询客户订单 - 成功")
    void testGetOrdersByCustomer_success() {
        // Given
        String customerId = "CUST001";
        OrderAggr order1 = OrderAggr.create(customerId, new Money(100.00, "CNY"));
        OrderAggr order2 = OrderAggr.create(customerId, new Money(200.00, "CNY"));

        when(orderRepository.findByCustomerId(customerId))
            .thenReturn(List.of(order1, order2));

        // When
        OrderQuery query = new OrderQuery(customerId);
        List<OrderDTO> orders = orderAppService.getOrdersByCustomer(query);

        // Then
        assertThat(orders).isNotNull();
        assertThat(orders).hasSize(2);
        assertThat(orders.get(0).getCustomerId()).isEqualTo(customerId);

        verify(orderRepository, times(1)).findByCustomerId(customerId);
    }

    @Test
    @DisplayName("创建订单 - 仓储异常")
    void testCreateOrder_repositoryException() {
        // Given
        CreateOrderCommand command = new CreateOrderCommand(
            "CUST001",
            new Money(100.00, "CNY")
        );

        when(orderRepository.save(any(OrderAggr.class)))
            .thenThrow(new RuntimeException("Database error"));

        // When & Then
        assertThatThrownBy(() -> orderAppService.createOrder(command))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Database error");

        verify(orderRepository, times(1)).save(any(OrderAggr.class));
    }

    @Test
    @DisplayName("查询订单 - 订单不存在")
    void testGetOrderById_notFound() {
        // Given
        OrderId orderId = OrderId.generate();
        when(orderRepository.findById(orderId))
            .thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> orderAppService.getOrderById(orderId))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("订单不存在");

        verify(orderRepository, times(1)).findById(orderId);
    }
}

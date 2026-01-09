package org.smm.archetype.domain._example.order.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 订单聚合根单元测试
 *
 * <p>测试订单的业务规则和不变性约束
 * @author Leonardo
 * @since 2025/12/30
 */
@DisplayName("订单聚合根测试")
class OrderTest {

    @Test
    @DisplayName("应该成功创建订单")
    void should_create_order_successfully() {
        // Given
        Long customerId = 1L;
        OrderItem item = OrderItem.of("P001", "商品1", Money.of(new BigDecimal("100")), 2);
        String address = "北京市朝阳区";
        String phone = "13800138000";

        // When
        Order order = Order.create(customerId, List.of(item), address, phone);

        // Then
        assertNotNull(order);
        assertEquals(OrderStatus.CREATED, order.getStatus());
        assertEquals(customerId, order.getCustomerId());
        assertEquals(1, order.getItemCount());
        assertEquals(address, order.getShippingAddress());
        assertEquals(phone, order.getPhoneNumber());
        assertTrue(order.hasUncommittedEvents());
        assertEquals(1, order.getUncommittedEvents().size());
    }

    @Test
    @DisplayName("应该成功支付订单")
    void should_pay_order_successfully() {
        // Given
        Order order = createTestOrder();

        // When
        order.pay("支付宝");

        // Then
        assertEquals(OrderStatus.PAID, order.getStatus());
        assertNotNull(order.getPaymentTime());
        assertTrue(order.hasUncommittedEvents());
    }

    @Test
    @DisplayName("不应该支付已支付的订单")
    void should_not_pay_paid_order() {
        // Given
        Order order = createTestOrder();
        order.pay("支付宝");

        // When & Then
        assertThrows(IllegalStateException.class, () -> order.pay("微信支付"));
    }

    @Test
    @DisplayName("应该成功取消订单")
    void should_cancel_order_successfully() {
        // Given
        Order order = createTestOrder();

        // When
        order.cancel("不想要了");

        // Then
        assertEquals(OrderStatus.CANCELLED, order.getStatus());
        assertEquals("不想要了", order.getCancelReason());
        assertNotNull(order.getCancelledTime());
    }

    @Test
    @DisplayName("不应该取消已发货的订单")
    void should_not_cancel_shipped_order() {
        // Given
        Order order = createTestOrder();
        order.pay("支付宝");
        order.ship("SF123456");

        // When & Then
        assertThrows(IllegalStateException.class, () -> order.cancel("不想要了"));
    }

    @Test
    @DisplayName("应该成功发货")
    void should_ship_order_successfully() {
        // Given
        Order order = createTestOrder();
        order.pay("支付宝");

        // When
        order.ship("SF123456");

        // Then
        assertEquals(OrderStatus.SHIPPED, order.getStatus());
        assertNotNull(order.getShippingTime());
    }

    @Test
    @DisplayName("应该成功完成订单")
    void should_complete_order_successfully() {
        // Given
        Order order = createTestOrder();
        order.pay("支付宝");
        order.ship("SF123456");

        // When
        order.complete();

        // Then
        assertEquals(OrderStatus.COMPLETED, order.getStatus());
        assertNotNull(order.getCompletedTime());
    }

    @Test
    @DisplayName("应该计算正确的总金额")
    void should_calculate_correct_total_amount() {
        // Given
        OrderItem item1 = OrderItem.of("P001", "商品1", Money.of(new BigDecimal("100")), 2);
        OrderItem item2 = OrderItem.of("P002", "商品2", Money.of(new BigDecimal("50")), 1);

        // When
        Order order = Order.create(1L, List.of(item1, item2), "地址", "电话");

        // Then
        Money expected = Money.of(new BigDecimal("250")); // 100*2 + 50*1 = 250
        assertEquals(expected, order.getTotalAmount());
    }

    @Test
    @DisplayName("应该正确修改收货地址")
    void should_change_shipping_address_successfully() {
        // Given
        Order order = createTestOrder();
        String newAddress = "上海市浦东新区";

        // When
        order.changeShippingAddress(newAddress);

        // Then
        assertEquals(newAddress, order.getShippingAddress());
    }

    @Test
    @DisplayName("不应该修改已支付订单的收货地址")
    void should_not_change_address_of_paid_order() {
        // Given
        Order order = createTestOrder();
        order.pay("支付宝");

        // When & Then
        assertThrows(IllegalStateException.class,
                () -> order.changeShippingAddress("新地址"));
    }

    @Test
    @DisplayName("订单项列表应该是防御性拷贝")
    void should_return_defensive_copy_of_items() {
        // Given
        OrderItem item = OrderItem.of("P001", "商品1", Money.of(new BigDecimal("100")), 1);
        Order order = Order.create(1L, List.of(item), "地址", "电话");

        // When
        List<OrderItem> items = order.getItems();

        // Then
        assertThrows(UnsupportedOperationException.class, () -> items.clear());
    }

    /**
     * 创建测试订单
     */
    private Order createTestOrder() {
        OrderItem item = OrderItem.of("P001", "测试商品", Money.of(new BigDecimal("100")), 1);
        return Order.create(1L, List.of(item), "测试地址", "13800138000");
    }

}

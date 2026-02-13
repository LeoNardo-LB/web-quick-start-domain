package org.smm.archetype.test.cases.unittest.domain.exampleorder;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.smm.archetype.domain.exampleorder.model.OrderAggr;
import org.smm.archetype.domain.exampleorder.model.OrderStatus;
import org.smm.archetype.domain.exampleorder.model.valueobject.Address;
import org.smm.archetype.domain.exampleorder.model.valueobject.ContactInfo;
import org.smm.archetype.domain.exampleorder.model.valueobject.Money;
import org.smm.archetype.test.support.UnitTestBase;

import java.math.BigDecimal;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 订单状态流转验证单元测试
 * <p>
 * 测试 OrderAggr.canTransitionTo() 方法的各种状态组合
 * </p>
 */
@DisplayName("订单状态流转验证")
class OrderAggrStatusTransitionUTest extends UnitTestBase {

    // ==================== 辅助方法 ====================

    private OrderAggr createOrderWithStatus(OrderStatus status) {
        return OrderAggr.OABuilder()
                .setOrderNo("ORD_TEST_001")
                .setCustomerId("CUST_001")
                .setCustomerName("测试客户")
                .setStatus(status)
                .setTotalAmount(Money.of(BigDecimal.valueOf(100), "CNY"))
                .setCurrency("CNY")
                .setItems(new ArrayList<>())
                .setShippingAddress(Address.ABuilder()
                        .setProvince("北京市")
                        .setCity("朝阳区")
                        .setDetailAddress("测试地址")
                        .setPostalCode("100000")
                        .build())
                .setContactInfo(ContactInfo.builder()
                        .setContactName("张三")
                        .setContactPhone("13800138000")
                        .setContactEmail("test@example.com")
                        .build())
                .build();
    }

    // ==================== CREATED 状态测试 ====================

    @Nested
    @DisplayName("CREATED 状态")
    class CreatedStatusTests {

        @Test
        @DisplayName("CREATED → PAID 应返回 true")
        void should_ReturnTrue_When_CreatedToPaid() {
            OrderAggr order = createOrderWithStatus(OrderStatus.CREATED);
            assertThat(order.canTransitionTo(OrderStatus.PAID)).isTrue();
        }

        @Test
        @DisplayName("CREATED → CANCELLED 应返回 true")
        void should_ReturnTrue_When_CreatedToCancelled() {
            OrderAggr order = createOrderWithStatus(OrderStatus.CREATED);
            assertThat(order.canTransitionTo(OrderStatus.CANCELLED)).isTrue();
        }

        @Test
        @DisplayName("CREATED → SHIPPED 应返回 false")
        void should_ReturnFalse_When_CreatedToShipped() {
            OrderAggr order = createOrderWithStatus(OrderStatus.CREATED);
            assertThat(order.canTransitionTo(OrderStatus.SHIPPED)).isFalse();
        }

        @Test
        @DisplayName("CREATED → COMPLETED 应返回 false")
        void should_ReturnFalse_When_CreatedToCompleted() {
            OrderAggr order = createOrderWithStatus(OrderStatus.CREATED);
            assertThat(order.canTransitionTo(OrderStatus.COMPLETED)).isFalse();
        }

        @Test
        @DisplayName("CREATED → CREATED 应返回 false (相同状态)")
        void should_ReturnFalse_When_CreatedToCreated() {
            OrderAggr order = createOrderWithStatus(OrderStatus.CREATED);
            assertThat(order.canTransitionTo(OrderStatus.CREATED)).isFalse();
        }
    }

    // ==================== PAID 状态测试 ====================

    @Nested
    @DisplayName("PAID 状态")
    class PaidStatusTests {

        @Test
        @DisplayName("PAID → SHIPPED 应返回 true")
        void should_ReturnTrue_When_PaidToShipped() {
            OrderAggr order = createOrderWithStatus(OrderStatus.PAID);
            assertThat(order.canTransitionTo(OrderStatus.SHIPPED)).isTrue();
        }

        @Test
        @DisplayName("PAID → CANCELLED 应返回 true")
        void should_ReturnTrue_When_PaidToCancelled() {
            OrderAggr order = createOrderWithStatus(OrderStatus.PAID);
            assertThat(order.canTransitionTo(OrderStatus.CANCELLED)).isTrue();
        }

        @Test
        @DisplayName("PAID → REFUNDED 应返回 true")
        void should_ReturnTrue_When_PaidToRefunded() {
            OrderAggr order = createOrderWithStatus(OrderStatus.PAID);
            assertThat(order.canTransitionTo(OrderStatus.REFUNDED)).isTrue();
        }

        @Test
        @DisplayName("PAID → PARTIALLY_REFUNDED 应返回 true")
        void should_ReturnTrue_When_PaidToPartiallyRefunded() {
            OrderAggr order = createOrderWithStatus(OrderStatus.PAID);
            assertThat(order.canTransitionTo(OrderStatus.PARTIALLY_REFUNDED)).isTrue();
        }

        @Test
        @DisplayName("PAID → COMPLETED 应返回 false")
        void should_ReturnFalse_When_PaidToCompleted() {
            OrderAggr order = createOrderWithStatus(OrderStatus.PAID);
            assertThat(order.canTransitionTo(OrderStatus.COMPLETED)).isFalse();
        }

        @Test
        @DisplayName("PAID → PAID 应返回 false (相同状态)")
        void should_ReturnFalse_When_PaidToPaid() {
            OrderAggr order = createOrderWithStatus(OrderStatus.PAID);
            assertThat(order.canTransitionTo(OrderStatus.PAID)).isFalse();
        }
    }

    // ==================== SHIPPED 状态测试 ====================

    @Nested
    @DisplayName("SHIPPED 状态")
    class ShippedStatusTests {

        @Test
        @DisplayName("SHIPPED → COMPLETED 应返回 true")
        void should_ReturnTrue_When_ShippedToCompleted() {
            OrderAggr order = createOrderWithStatus(OrderStatus.SHIPPED);
            assertThat(order.canTransitionTo(OrderStatus.COMPLETED)).isTrue();
        }

        @Test
        @DisplayName("SHIPPED → CANCELLED 应返回 false")
        void should_ReturnFalse_When_ShippedToCancelled() {
            OrderAggr order = createOrderWithStatus(OrderStatus.SHIPPED);
            assertThat(order.canTransitionTo(OrderStatus.CANCELLED)).isFalse();
        }

        @Test
        @DisplayName("SHIPPED → PAID 应返回 false")
        void should_ReturnFalse_When_ShippedToPaid() {
            OrderAggr order = createOrderWithStatus(OrderStatus.SHIPPED);
            assertThat(order.canTransitionTo(OrderStatus.PAID)).isFalse();
        }

        @Test
        @DisplayName("SHIPPED → SHIPPED 应返回 false (相同状态)")
        void should_ReturnFalse_When_ShippedToShipped() {
            OrderAggr order = createOrderWithStatus(OrderStatus.SHIPPED);
            assertThat(order.canTransitionTo(OrderStatus.SHIPPED)).isFalse();
        }
    }

    // ==================== COMPLETED 终态测试 ====================

    @Nested
    @DisplayName("COMPLETED 终态")
    class CompletedTerminalTests {

        @Test
        @DisplayName("COMPLETED → 任意状态应返回 false")
        void should_ReturnFalse_When_CompletedToAny() {
            OrderAggr order = createOrderWithStatus(OrderStatus.COMPLETED);
            
            assertThat(order.canTransitionTo(OrderStatus.CREATED)).isFalse();
            assertThat(order.canTransitionTo(OrderStatus.PAID)).isFalse();
            assertThat(order.canTransitionTo(OrderStatus.SHIPPED)).isFalse();
            assertThat(order.canTransitionTo(OrderStatus.CANCELLED)).isFalse();
            assertThat(order.canTransitionTo(OrderStatus.REFUNDED)).isFalse();
            assertThat(order.canTransitionTo(OrderStatus.PARTIALLY_REFUNDED)).isFalse();
            assertThat(order.canTransitionTo(OrderStatus.COMPLETED)).isFalse();
        }
    }

    // ==================== CANCELLED 终态测试 ====================

    @Nested
    @DisplayName("CANCELLED 终态")
    class CancelledTerminalTests {

        @Test
        @DisplayName("CANCELLED → 任意状态应返回 false")
        void should_ReturnFalse_When_CancelledToAny() {
            OrderAggr order = createOrderWithStatus(OrderStatus.CANCELLED);
            
            assertThat(order.canTransitionTo(OrderStatus.CREATED)).isFalse();
            assertThat(order.canTransitionTo(OrderStatus.PAID)).isFalse();
            assertThat(order.canTransitionTo(OrderStatus.SHIPPED)).isFalse();
            assertThat(order.canTransitionTo(OrderStatus.COMPLETED)).isFalse();
            assertThat(order.canTransitionTo(OrderStatus.REFUNDED)).isFalse();
            assertThat(order.canTransitionTo(OrderStatus.PARTIALLY_REFUNDED)).isFalse();
            assertThat(order.canTransitionTo(OrderStatus.CANCELLED)).isFalse();
        }
    }

    // ==================== REFUNDED 终态测试 ====================

    @Nested
    @DisplayName("REFUNDED 终态")
    class RefundedTerminalTests {

        @Test
        @DisplayName("REFUNDED → 任意状态应返回 false")
        void should_ReturnFalse_When_RefundedToAny() {
            OrderAggr order = createOrderWithStatus(OrderStatus.REFUNDED);
            
            assertThat(order.canTransitionTo(OrderStatus.CREATED)).isFalse();
            assertThat(order.canTransitionTo(OrderStatus.PAID)).isFalse();
            assertThat(order.canTransitionTo(OrderStatus.SHIPPED)).isFalse();
            assertThat(order.canTransitionTo(OrderStatus.COMPLETED)).isFalse();
            assertThat(order.canTransitionTo(OrderStatus.CANCELLED)).isFalse();
            assertThat(order.canTransitionTo(OrderStatus.PARTIALLY_REFUNDED)).isFalse();
            assertThat(order.canTransitionTo(OrderStatus.REFUNDED)).isFalse();
        }
    }

    // ==================== PARTIALLY_REFUNDED 状态测试 ====================

    @Nested
    @DisplayName("PARTIALLY_REFUNDED 状态")
    class PartiallyRefundedTests {

        @Test
        @DisplayName("PARTIALLY_REFUNDED → REFUNDED 应返回 true")
        void should_ReturnTrue_When_PartiallyRefundedToRefunded() {
            OrderAggr order = createOrderWithStatus(OrderStatus.PARTIALLY_REFUNDED);
            assertThat(order.canTransitionTo(OrderStatus.REFUNDED)).isTrue();
        }

        @Test
        @DisplayName("PARTIALLY_REFUNDED → PARTIALLY_REFUNDED 应返回 true (继续部分退款)")
        void should_ReturnTrue_When_PartiallyRefundedToPartiallyRefunded() {
            OrderAggr order = createOrderWithStatus(OrderStatus.PARTIALLY_REFUNDED);
            assertThat(order.canTransitionTo(OrderStatus.PARTIALLY_REFUNDED)).isTrue();
        }

        @Test
        @DisplayName("PARTIALLY_REFUNDED → SHIPPED 应返回 false")
        void should_ReturnFalse_When_PartiallyRefundedToShipped() {
            OrderAggr order = createOrderWithStatus(OrderStatus.PARTIALLY_REFUNDED);
            assertThat(order.canTransitionTo(OrderStatus.SHIPPED)).isFalse();
        }
    }

    // ==================== 边界情况测试 ====================

    @Nested
    @DisplayName("边界情况")
    class EdgeCaseTests {

        @Test
        @DisplayName("传入 null 应返回 false")
        void should_ReturnFalse_When_NullTarget() {
            OrderAggr order = createOrderWithStatus(OrderStatus.CREATED);
            assertThat(order.canTransitionTo(null)).isFalse();
        }
    }
}

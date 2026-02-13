package org.smm.archetype.test.cases.unittest.domain.exampleorder;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.smm.archetype.domain.exampleorder.OrderErrorCode;
import org.smm.archetype.domain.exampleorder.model.OrderAggr;
import org.smm.archetype.domain.exampleorder.model.OrderItem;
import org.smm.archetype.domain.exampleorder.model.OrderStatus;
import org.smm.archetype.domain.exampleorder.model.PaymentMethod;
import org.smm.archetype.domain.exampleorder.model.RefundType;
import org.smm.archetype.domain.exampleorder.model.valueobject.Address;
import org.smm.archetype.domain.exampleorder.model.valueobject.ContactInfo;
import org.smm.archetype.domain.exampleorder.model.valueobject.Money;
import org.smm.archetype.domain.shared.exception.BizException;
import org.smm.archetype.test.support.UnitTestBase;

import java.math.BigDecimal;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * OrderAggr.refund() 方法的单元测试
 *
 * <p>测试退款业务逻辑，包括：
 * <ul>
 *   <li>全额退款成功</li>
 *   <li>部分退款成功</li>
 *   <li>多次累计退款</li>
 *   <li>状态验证</li>
 *   <li>金额验证</li>
 *   <li>异常场景</li>
 * </ul>
 */
@DisplayName("OrderAggr.refund() 单元测试")
public class OrderAggrUTest extends UnitTestBase {

    private static final String ORDER_NO = "ORD202602130001";
    private static final String CUSTOMER_ID = "customer123";
    private static final String CUSTOMER_NAME = "测试客户";
    private static final Money TOTAL_AMOUNT = Money.of(new BigDecimal("100.00"));
    private static final String CURRENCY = "CNY";

    private Address shippingAddress;
    private ContactInfo contactInfo;
    private ArrayList<OrderItem> items;

    @BeforeEach
    void setUp() {
        shippingAddress = Address.ABuilder()
                .setProvince("北京市")
                .setCity("北京市")
                .setDistrict("朝阳区")
                .setDetailAddress("测试街道123号")
                .build();

        contactInfo = ContactInfo.builder()
                .setContactName("张三")
                .setContactPhone("13800138000")
                .build();

        items = new ArrayList<>();
        OrderItem item = OrderItem.builder()
                .setProductId("PROD001")
                .setProductName("测试商品")
                .setSkuCode("SKU001")
                .setUnitPrice(Money.of(new BigDecimal("100.00")))
                .setQuantity(1)
                .setCurrency(CURRENCY)
                .setSubtotal(Money.of(new BigDecimal("100.00")))
                .build();
        items.add(item);
    }

    // ==================== 辅助方法 ====================

    /**
     * 创建已支付状态的订单
     */
    private OrderAggr createPaidOrder() {
        OrderAggr order = OrderAggr.create(
                ORDER_NO,
                CUSTOMER_ID,
                CUSTOMER_NAME,
                items,
                TOTAL_AMOUNT,
                shippingAddress,
                contactInfo,
                null
        );
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CREATED);
        // 支付订单以改变状态为 PAID
        order.pay(PaymentMethod.ALIPAY, TOTAL_AMOUNT);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
        return order;
    }

    // ==================== 全额退款测试 ====================

    @Nested
    @DisplayName("全额退款测试")
    class FullRefundTests {

        @Test
        @DisplayName("全额退款成功 - 状态变为 REFUNDED")
        void testRefund_FullRefund_Success() {
            // Given
            OrderAggr order = createPaidOrder();
            Money refundAmount = TOTAL_AMOUNT;

            // When
            order.refund(refundAmount, RefundType.FULL, "用户申请退款");

            // Then
            assertThat(order.getStatus()).isEqualTo(OrderStatus.REFUNDED);
            assertThat(order.getTotalRefundedAmount()).isEqualTo(TOTAL_AMOUNT);
            assertThat(order.getRefundedAmount()).isEqualTo(refundAmount);
            assertThat(order.getRefundReason()).isEqualTo("用户申请退款");
            assertThat(order.getRefundType()).isEqualTo(RefundType.FULL);
            assertThat(order.getRefundedTime()).isNotNull();
            assertThat(order.isRefunded()).isTrue();
        }

    }

    // ==================== 部分退款测试 ====================

    @Nested
    @DisplayName("部分退款测试")
    class PartialRefundTests {

        @Test
        @DisplayName("部分退款成功 - 状态变为 PARTIALLY_REFUNDED")
        void testRefund_PartialRefund_Success() {
            // Given
            OrderAggr order = createPaidOrder();
            Money refundAmount = Money.of(new BigDecimal("30.00"));

            // When
            order.refund(refundAmount, RefundType.PARTIAL, "部分退款");

            // Then
            assertThat(order.getStatus()).isEqualTo(OrderStatus.PARTIALLY_REFUNDED);
            assertThat(order.getTotalRefundedAmount()).isEqualTo(refundAmount);
            assertThat(order.getRefundedAmount()).isEqualTo(refundAmount);
            assertThat(order.getRefundType()).isEqualTo(RefundType.PARTIAL);
            assertThat(order.isPartiallyRefunded()).isTrue();
            assertThat(order.isRefunded()).isFalse();
        }

    }

    // ==================== 多次累计退款测试 ====================

    @Nested
    @DisplayName("多次累计退款测试")
    class MultipleRefundsTests {

        @Test
        @DisplayName("多次累计退款 - 最终全额退款")
        void testRefund_MultipleRefunds_Cumulative_FullRefund() {
            // Given
            OrderAggr order = createPaidOrder();
            Money firstRefund = Money.of(new BigDecimal("30.00"));
            Money secondRefund = Money.of(new BigDecimal("40.00"));
            Money thirdRefund = Money.of(new BigDecimal("30.00"));

            // When - 第一次部分退款
            order.refund(firstRefund, RefundType.PARTIAL, "第一次退款");

            // Then
            assertThat(order.getStatus()).isEqualTo(OrderStatus.PARTIALLY_REFUNDED);
            assertThat(order.getTotalRefundedAmount()).isEqualTo(firstRefund);

            // When - 第二次部分退款
            order.refund(secondRefund, RefundType.PARTIAL, "第二次退款");

            // Then
            assertThat(order.getStatus()).isEqualTo(OrderStatus.PARTIALLY_REFUNDED);
            Money expectedTotal = firstRefund.add(secondRefund);
            assertThat(order.getTotalRefundedAmount()).isEqualTo(expectedTotal);

            // When - 第三次退款（达到全额）
            order.refund(thirdRefund, RefundType.PARTIAL, "第三次退款");

            // Then - 状态变为 REFUNDED
            assertThat(order.getStatus()).isEqualTo(OrderStatus.REFUNDED);
            assertThat(order.getTotalRefundedAmount()).isEqualTo(TOTAL_AMOUNT);
            assertThat(order.isRefunded()).isTrue();
        }

        @Test
        @DisplayName("多次累计退款 - 仍为部分退款")
        void testRefund_MultipleRefunds_StillPartial() {
            // Given
            OrderAggr order = createPaidOrder();
            Money firstRefund = Money.of(new BigDecimal("20.00"));
            Money secondRefund = Money.of(new BigDecimal("30.00"));

            // When - 第一次部分退款
            order.refund(firstRefund, RefundType.PARTIAL, "第一次退款");

            // When - 第二次部分退款
            order.refund(secondRefund, RefundType.PARTIAL, "第二次退款");

            // Then - 仍为部分退款
            assertThat(order.getStatus()).isEqualTo(OrderStatus.PARTIALLY_REFUNDED);
            Money expectedTotal = firstRefund.add(secondRefund);
            assertThat(order.getTotalRefundedAmount()).isEqualTo(expectedTotal);
            assertThat(order.isPartiallyRefunded()).isTrue();
        }

    }

    // ==================== 状态验证测试 ====================

    @Nested
    @DisplayName("状态验证测试")
    class StatusValidationTests {

        @Test
        @DisplayName("CREATED 状态不允许退款")
        void testRefund_CreatedStatus_ThrowsException() {
            // Given - 创建订单后不支付
            OrderAggr order = OrderAggr.create(
                    ORDER_NO,
                    CUSTOMER_ID,
                    CUSTOMER_NAME,
                    items,
                    TOTAL_AMOUNT,
                    shippingAddress,
                    contactInfo,
                    null
            );
            assertThat(order.getStatus()).isEqualTo(OrderStatus.CREATED);

            // When & Then
            assertThatThrownBy(() -> order.refund(TOTAL_AMOUNT, RefundType.FULL, "测试"))
                    .isInstanceOf(BizException.class)
                    .satisfies(ex -> {
                        BizException bizEx = (BizException) ex;
                        assertThat(bizEx.getErrorCode()).isEqualTo(OrderErrorCode.ORDER_STATUS_INVALID);
                    });
        }

        @Test
        @DisplayName("CANCELLED 状态不允许退款")
        void testRefund_CancelledStatus_ThrowsException() {
            // Given
            OrderAggr order = createPaidOrder();
            order.cancel("用户取消");

            // When & Then
            assertThatThrownBy(() -> order.refund(TOTAL_AMOUNT, RefundType.FULL, "测试"))
                    .isInstanceOf(BizException.class)
                    .satisfies(ex -> {
                        BizException bizEx = (BizException) ex;
                        assertThat(bizEx.getErrorCode()).isEqualTo(OrderErrorCode.ORDER_STATUS_INVALID);
                    });
        }

        @Test
        @DisplayName("REFUNDED 状态不允许再次退款")
        void testRefund_RefundedStatus_ThrowsException() {
            // Given
            OrderAggr order = createPaidOrder();
            order.refund(TOTAL_AMOUNT, RefundType.FULL, "第一次退款");
            assertThat(order.getStatus()).isEqualTo(OrderStatus.REFUNDED);

            // When & Then
            assertThatThrownBy(() -> order.refund(Money.of(new BigDecimal("10.00")), RefundType.PARTIAL, "再次退款"))
                    .isInstanceOf(BizException.class)
                    .satisfies(ex -> {
                        BizException bizEx = (BizException) ex;
                        assertThat(bizEx.getErrorCode()).isEqualTo(OrderErrorCode.ORDER_STATUS_INVALID);
                    });
        }

        @Test
        @DisplayName("SHIPPED 状态不允许退款")
        void testRefund_ShippedStatus_ThrowsException() {
            // Given
            OrderAggr order = createPaidOrder();
            order.ship();
            assertThat(order.getStatus()).isEqualTo(OrderStatus.SHIPPED);

            // When & Then
            assertThatThrownBy(() -> order.refund(TOTAL_AMOUNT, RefundType.FULL, "测试"))
                    .isInstanceOf(BizException.class)
                    .satisfies(ex -> {
                        BizException bizEx = (BizException) ex;
                        assertThat(bizEx.getErrorCode()).isEqualTo(OrderErrorCode.ORDER_STATUS_INVALID);
                    });
        }

    }

    // ==================== 金额验证测试 ====================

    @Nested
    @DisplayName("金额验证测试")
    class AmountValidationTests {

        @Test
        @DisplayName("退款金额为 null - 抛出异常")
        void testRefund_NullAmount_ThrowsException() {
            // Given
            OrderAggr order = createPaidOrder();

            // When & Then
            assertThatThrownBy(() -> order.refund(null, RefundType.FULL, "测试"))
                    .isInstanceOf(BizException.class)
                    .satisfies(ex -> {
                        BizException bizEx = (BizException) ex;
                        assertThat(bizEx.getErrorCode()).isEqualTo(OrderErrorCode.REFUND_AMOUNT_INVALID);
                    });
        }

        @Test
        @DisplayName("退款金额为零 - 抛出异常")
        void testRefund_ZeroAmount_ThrowsException() {
            // Given
            OrderAggr order = createPaidOrder();
            Money zeroAmount = Money.zero();

            // When & Then
            assertThatThrownBy(() -> order.refund(zeroAmount, RefundType.FULL, "测试"))
                    .isInstanceOf(BizException.class)
                    .satisfies(ex -> {
                        BizException bizEx = (BizException) ex;
                        assertThat(bizEx.getErrorCode()).isEqualTo(OrderErrorCode.REFUND_AMOUNT_INVALID);
                    });
        }

        @Test
        @DisplayName("退款金额为负数 - 抛出异常")
        void testRefund_NegativeAmount_ThrowsException() {
            // Given
            OrderAggr order = createPaidOrder();
            Money negativeAmount = Money.of(new BigDecimal("-10.00"));

            // When & Then
            assertThatThrownBy(() -> order.refund(negativeAmount, RefundType.FULL, "测试"))
                    .isInstanceOf(BizException.class)
                    .satisfies(ex -> {
                        BizException bizEx = (BizException) ex;
                        assertThat(bizEx.getErrorCode()).isEqualTo(OrderErrorCode.REFUND_AMOUNT_INVALID);
                    });
        }

        @Test
        @DisplayName("退款金额超过剩余可退金额 - 抛出异常")
        void testRefund_ExceededAmount_ThrowsException() {
            // Given
            OrderAggr order = createPaidOrder();
            Money excessiveAmount = Money.of(new BigDecimal("150.00")); // 超过订单金额

            // When & Then
            assertThatThrownBy(() -> order.refund(excessiveAmount, RefundType.FULL, "测试"))
                    .isInstanceOf(BizException.class)
                    .satisfies(ex -> {
                        BizException bizEx = (BizException) ex;
                        assertThat(bizEx.getErrorCode()).isEqualTo(OrderErrorCode.REFUND_AMOUNT_EXCEEDED);
                    });
        }

        @Test
        @DisplayName("部分退款后，再次退款超过剩余可退金额 - 抛出异常")
        void testRefund_ExceededAmount_AfterPartialRefund_ThrowsException() {
            // Given
            OrderAggr order = createPaidOrder();
            Money firstRefund = Money.of(new BigDecimal("70.00"));
            order.refund(firstRefund, RefundType.PARTIAL, "第一次退款");

            // 剩余可退金额 = 100 - 70 = 30
            Money excessiveAmount = Money.of(new BigDecimal("50.00")); // 超过剩余可退

            // When & Then
            assertThatThrownBy(() -> order.refund(excessiveAmount, RefundType.PARTIAL, "第二次退款"))
                    .isInstanceOf(BizException.class)
                    .satisfies(ex -> {
                        BizException bizEx = (BizException) ex;
                        assertThat(bizEx.getErrorCode()).isEqualTo(OrderErrorCode.REFUND_AMOUNT_EXCEEDED);
                    });
        }

        @Test
        @DisplayName("退款金额等于剩余可退金额 - 成功")
        void testRefund_ExactRemainingAmount_Success() {
            // Given
            OrderAggr order = createPaidOrder();
            Money firstRefund = Money.of(new BigDecimal("70.00"));
            order.refund(firstRefund, RefundType.PARTIAL, "第一次退款");

            // 剩余可退金额 = 100 - 70 = 30
            Money exactRemaining = Money.of(new BigDecimal("30.00"));

            // When
            order.refund(exactRemaining, RefundType.PARTIAL, "第二次退款");

            // Then
            assertThat(order.getStatus()).isEqualTo(OrderStatus.REFUNDED);
            assertThat(order.getTotalRefundedAmount()).isEqualTo(TOTAL_AMOUNT);
        }

    }

    // ==================== PARTIALLY_REFUNDED 状态退款测试 ====================

    @Nested
    @DisplayName("PARTIALLY_REFUNDED 状态退款测试")
    class PartiallyRefundedStatusTests {

        @Test
        @DisplayName("PARTIALLY_REFUNDED 状态可以继续退款")
        void testRefund_PartiallyRefundedStatus_CanRefund() {
            // Given
            OrderAggr order = createPaidOrder();
            Money firstRefund = Money.of(new BigDecimal("30.00"));
            order.refund(firstRefund, RefundType.PARTIAL, "第一次退款");
            assertThat(order.getStatus()).isEqualTo(OrderStatus.PARTIALLY_REFUNDED);

            Money secondRefund = Money.of(new BigDecimal("20.00"));

            // When
            order.refund(secondRefund, RefundType.PARTIAL, "第二次退款");

            // Then
            assertThat(order.getStatus()).isEqualTo(OrderStatus.PARTIALLY_REFUNDED);
            Money expectedTotal = firstRefund.add(secondRefund);
            assertThat(order.getTotalRefundedAmount()).isEqualTo(expectedTotal);
        }

    }

}

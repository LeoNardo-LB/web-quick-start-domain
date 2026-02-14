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
 * OrderAggr.sortOrder 字段的单元测试
 *
 * <p>测试排序字段业务逻辑，包括：
 * <ul>
 *   <li>默认排序值为 0</li>
 *   <li>创建时指定排序值</li>
 *   <li>CREATED 状态下修改排序值</li>
 *   <li>非 CREATED 状态下修改排序值抛出异常</li>
 *   <li>负值和正值支持</li>
 * </ul>
 */
@DisplayName("OrderAggr.sortOrder 单元测试")
public class OrderAggrSortOrderUTest extends UnitTestBase {

    private static final String ORDER_NO      = "ORD202602140001";
    private static final String CUSTOMER_ID   = "customer123";
    private static final String CUSTOMER_NAME = "测试客户";
    private static final Money  TOTAL_AMOUNT  = Money.of(new BigDecimal("100.00"));
    private static final String CURRENCY      = "CNY";

    private Address              shippingAddress;
    private ContactInfo          contactInfo;
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

    // ==================== 默认值测试 ====================

    @Nested
    @DisplayName("默认值测试")
    class DefaultValueTests {

        @Test
        @DisplayName("创建订单时未指定 sortOrder - 默认值为 0")
        void testCreate_WithoutSortOrder_DefaultIsZero() {
            // When - 使用 8 参数版本创建订单
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

            // Then
            assertThat(order.getSortOrder()).isEqualTo(0);
        }

        @Test
        @DisplayName("创建订单时 sortOrder 为 null - 默认值为 0")
        void testCreate_WithNullSortOrder_DefaultIsZero() {
            // When - 使用 9 参数版本，sortOrder 为 null
            OrderAggr order = OrderAggr.create(
                    ORDER_NO,
                    CUSTOMER_ID,
                    CUSTOMER_NAME,
                    items,
                    TOTAL_AMOUNT,
                    shippingAddress,
                    contactInfo,
                    null,
                    null
            );

            // Then
            assertThat(order.getSortOrder()).isEqualTo(0);
        }

    }

    // ==================== 创建时指定排序值测试 ====================

    @Nested
    @DisplayName("创建时指定排序值测试")
    class CreateWithSortOrderTests {

        @Test
        @DisplayName("创建订单时指定 sortOrder 为正数")
        void testCreate_WithPositiveSortOrder_Success() {
            // When
            OrderAggr order = OrderAggr.create(
                    ORDER_NO,
                    CUSTOMER_ID,
                    CUSTOMER_NAME,
                    items,
                    TOTAL_AMOUNT,
                    shippingAddress,
                    contactInfo,
                    null,
                    100
            );

            // Then
            assertThat(order.getSortOrder()).isEqualTo(100);
        }

        @Test
        @DisplayName("创建订单时指定 sortOrder 为负数")
        void testCreate_WithNegativeSortOrder_Success() {
            // When
            OrderAggr order = OrderAggr.create(
                    ORDER_NO,
                    CUSTOMER_ID,
                    CUSTOMER_NAME,
                    items,
                    TOTAL_AMOUNT,
                    shippingAddress,
                    contactInfo,
                    null,
                    -10
            );

            // Then
            assertThat(order.getSortOrder()).isEqualTo(-10);
        }

        @Test
        @DisplayName("创建订单时指定 sortOrder 为大正数")
        void testCreate_WithLargePositiveSortOrder_Success() {
            // When
            OrderAggr order = OrderAggr.create(
                    ORDER_NO,
                    CUSTOMER_ID,
                    CUSTOMER_NAME,
                    items,
                    TOTAL_AMOUNT,
                    shippingAddress,
                    contactInfo,
                    null,
                    999
            );

            // Then
            assertThat(order.getSortOrder()).isEqualTo(999);
        }

    }

    // ==================== 更新排序值测试 ====================

    @Nested
    @DisplayName("更新排序值测试")
    class UpdateSortOrderTests {

        @Test
        @DisplayName("CREATED 状态下可以更新排序值")
        void testUpdateSortOrder_CreatedStatus_Success() {
            // Given
            OrderAggr order = OrderAggr.create(
                    ORDER_NO,
                    CUSTOMER_ID,
                    CUSTOMER_NAME,
                    items,
                    TOTAL_AMOUNT,
                    shippingAddress,
                    contactInfo,
                    null,
                    0
            );
            assertThat(order.getStatus()).isEqualTo(OrderStatus.CREATED);

            // When
            order.updateSortOrder(50);

            // Then
            assertThat(order.getSortOrder()).isEqualTo(50);
        }

        @Test
        @DisplayName("更新排序值为 null - 设置为默认值 0")
        void testUpdateSortOrder_NullValue_DefaultToZero() {
            // Given
            OrderAggr order = OrderAggr.create(
                    ORDER_NO,
                    CUSTOMER_ID,
                    CUSTOMER_NAME,
                    items,
                    TOTAL_AMOUNT,
                    shippingAddress,
                    contactInfo,
                    null,
                    100
            );
            assertThat(order.getSortOrder()).isEqualTo(100);

            // When
            order.updateSortOrder(null);

            // Then
            assertThat(order.getSortOrder()).isEqualTo(0);
        }

    }

    // ==================== 状态验证测试 ====================

    @Nested
    @DisplayName("状态验证测试 - 非 CREATED 状态不允许修改")
    class StatusValidationTests {

        @Test
        @DisplayName("PAID 状态不允许修改排序值")
        void testUpdateSortOrder_PaidStatus_ThrowsException() {
            // Given
            OrderAggr order = OrderAggr.create(
                    ORDER_NO,
                    CUSTOMER_ID,
                    CUSTOMER_NAME,
                    items,
                    TOTAL_AMOUNT,
                    shippingAddress,
                    contactInfo,
                    null,
                    0
            );
            order.pay(PaymentMethod.ALIPAY, TOTAL_AMOUNT);
            assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);

            // When & Then
            assertThatThrownBy(() -> order.updateSortOrder(50))
                    .isInstanceOf(BizException.class)
                    .satisfies(ex -> {
                        BizException bizEx = (BizException) ex;
                        assertThat(bizEx.getErrorCode()).isEqualTo(OrderErrorCode.ORDER_STATUS_INVALID);
                    });
        }

        @Test
        @DisplayName("SHIPPED 状态不允许修改排序值")
        void testUpdateSortOrder_ShippedStatus_ThrowsException() {
            // Given
            OrderAggr order = OrderAggr.create(
                    ORDER_NO,
                    CUSTOMER_ID,
                    CUSTOMER_NAME,
                    items,
                    TOTAL_AMOUNT,
                    shippingAddress,
                    contactInfo,
                    null,
                    0
            );
            order.pay(PaymentMethod.ALIPAY, TOTAL_AMOUNT);
            order.ship();
            assertThat(order.getStatus()).isEqualTo(OrderStatus.SHIPPED);

            // When & Then
            assertThatThrownBy(() -> order.updateSortOrder(50))
                    .isInstanceOf(BizException.class)
                    .satisfies(ex -> {
                        BizException bizEx = (BizException) ex;
                        assertThat(bizEx.getErrorCode()).isEqualTo(OrderErrorCode.ORDER_STATUS_INVALID);
                    });
        }

        @Test
        @DisplayName("CANCELLED 状态不允许修改排序值")
        void testUpdateSortOrder_CancelledStatus_ThrowsException() {
            // Given
            OrderAggr order = OrderAggr.create(
                    ORDER_NO,
                    CUSTOMER_ID,
                    CUSTOMER_NAME,
                    items,
                    TOTAL_AMOUNT,
                    shippingAddress,
                    contactInfo,
                    null,
                    0
            );
            order.cancel("用户取消");
            assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);

            // When & Then
            assertThatThrownBy(() -> order.updateSortOrder(50))
                    .isInstanceOf(BizException.class)
                    .satisfies(ex -> {
                        BizException bizEx = (BizException) ex;
                        assertThat(bizEx.getErrorCode()).isEqualTo(OrderErrorCode.ORDER_STATUS_INVALID);
                    });
        }

    }

}

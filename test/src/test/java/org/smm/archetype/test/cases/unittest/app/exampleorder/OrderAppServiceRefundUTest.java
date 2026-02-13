package org.smm.archetype.test.cases.unittest.app.exampleorder;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.smm.archetype.app.exampleorder.OrderAppService;
import org.smm.archetype.app.exampleorder.command.RefundOrderCommand;
import org.smm.archetype.app.exampleorder.converter.OrderDtoConverter;
import org.smm.archetype.app.exampleorder.dto.MoneyDTO;
import org.smm.archetype.app.exampleorder.dto.OrderDTO;
import org.smm.archetype.domain.exampleorder.OrderErrorCode;
import org.smm.archetype.domain.exampleorder.model.OrderAggr;
import org.smm.archetype.domain.exampleorder.model.OrderItem;
import org.smm.archetype.domain.exampleorder.model.OrderStatus;
import org.smm.archetype.domain.exampleorder.model.PaymentMethod;
import org.smm.archetype.domain.exampleorder.model.RefundType;
import org.smm.archetype.domain.exampleorder.model.valueobject.Address;
import org.smm.archetype.domain.exampleorder.model.valueobject.ContactInfo;
import org.smm.archetype.domain.exampleorder.model.valueobject.Money;
import org.smm.archetype.domain.exampleorder.repository.OrderAggrRepository;
import org.smm.archetype.domain.exampleorder.service.OrderDomainService;
import org.smm.archetype.domain.shared.event.DomainEventPublisher;
import org.smm.archetype.domain.shared.exception.BizException;
import org.smm.archetype.test.support.UnitTestBase;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * OrderAppService.refundOrder() 方法的单元测试
 */
@DisplayName("OrderAppService.refundOrder() 单元测试")
public class OrderAppServiceRefundUTest extends UnitTestBase {

    private OrderAggrRepository orderRepository;
    private OrderDomainService orderDomainService;
    private DomainEventPublisher domainEventPublisher;
    private OrderDtoConverter dtoConverter;
    private OrderAppService orderAppService;

    private static final Long ORDER_ID = 1L;
    private static final Money TOTAL_AMOUNT = Money.of(new BigDecimal("100.00"));
    private static final String CURRENCY = "CNY";

    @BeforeEach
    void setUp() {
        orderRepository = mock(OrderAggrRepository.class);
        orderDomainService = mock(OrderDomainService.class);
        domainEventPublisher = mock(DomainEventPublisher.class);
        dtoConverter = mock(OrderDtoConverter.class);
        
        orderAppService = new OrderAppService(
                orderRepository,
                orderDomainService,
                domainEventPublisher,
                dtoConverter
        );
    }

    /**
     * 创建已支付状态的订单
     */
    private OrderAggr createPaidOrder() {
        Address shippingAddress = Address.ABuilder()
                .setProvince("北京市")
                .setCity("北京市")
                .setDistrict("朝阳区")
                .setDetailAddress("测试街道123号")
                .build();

        ContactInfo contactInfo = ContactInfo.builder()
                .setContactName("张三")
                .setContactPhone("13800138000")
                .build();

        ArrayList<OrderItem> items = new ArrayList<>();
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

        OrderAggr order = OrderAggr.create(
                "ORD202602130001",
                "customer123",
                "测试客户",
                items,
                TOTAL_AMOUNT,
                shippingAddress,
                contactInfo,
                null
        );
        
        order.pay(PaymentMethod.ALIPAY, TOTAL_AMOUNT);
        
        // 使用反射设置订单ID
        try {
            var idField = org.smm.archetype.domain.shared.base.Entity.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(order, ORDER_ID);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set order ID: " + e.getMessage(), e);
        }
        
        return order;
    }

    private OrderDTO createRefundedOrderDTO() {
        return OrderDTO.builder()
                .setId(ORDER_ID)
                .setOrderNo("ORD202602130001")
                .setCustomerId("customer123")
                .setStatus(OrderStatus.REFUNDED)
                .setTotalAmount(MoneyDTO.builder()
                        .setAmount(new BigDecimal("100.00"))
                        .setCurrency(CURRENCY)
                        .build())
                .setRefundType(RefundType.FULL.name())
                .build();
    }

    @Nested
    @DisplayName("退款成功测试")
    class RefundSuccessTests {

        @Test
        @DisplayName("退款成功 - 调用 Repository 并返回 DTO")
        void testRefundOrder_Success() {
            // Given
            OrderAggr paidOrder = createPaidOrder();
            RefundOrderCommand command = new RefundOrderCommand(
                    ORDER_ID,
                    "100.00",
                    CURRENCY,
                    "FULL",
                    "用户申请退款"
            );

            when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(paidOrder));
            when(orderRepository.save(any(OrderAggr.class))).thenAnswer(inv -> inv.getArgument(0));
            when(dtoConverter.toDTO(any(OrderAggr.class))).thenReturn(createRefundedOrderDTO());

            // When
            OrderDTO result = orderAppService.refundOrder(command);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getStatus()).isEqualTo(OrderStatus.REFUNDED);
            verify(orderRepository).findById(ORDER_ID);
            verify(orderRepository).save(any(OrderAggr.class));
        }

    }

    @Nested
    @DisplayName("订单不存在测试")
    class OrderNotFoundTests {

        @Test
        @DisplayName("订单不存在 - 抛出 RuntimeException")
        void testRefundOrder_OrderNotFound_ThrowsException() {
            // Given
            RefundOrderCommand command = new RefundOrderCommand(
                    999L,
                    "100.00",
                    CURRENCY,
                    "FULL",
                    "用户申请退款"
            );

            when(orderRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> orderAppService.refundOrder(command))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("订单不存在");

            verify(orderRepository, never()).save(any());
        }

    }

    @Nested
    @DisplayName("领域层异常传播测试")
    class DomainExceptionTests {

        @Test
        @DisplayName("状态不允许退款 - BizException 正确传播")
        void testRefundOrder_InvalidStatus_ThrowsBizException() {
            // Given - 创建 CREATED 状态的订单（不允许退款）
            Address shippingAddress = Address.ABuilder()
                    .setProvince("北京市")
                    .setCity("北京市")
                    .build();
            ContactInfo contactInfo = ContactInfo.builder()
                    .setContactName("张三")
                    .setContactPhone("13800138000")
                    .build();
            ArrayList<OrderItem> items = new ArrayList<>();
            items.add(OrderItem.builder()
                    .setProductId("PROD001")
                    .setProductName("测试商品")
                    .setSkuCode("SKU001")
                    .setUnitPrice(TOTAL_AMOUNT)
                    .setQuantity(1)
                    .setCurrency(CURRENCY)
                    .setSubtotal(TOTAL_AMOUNT)
                    .build());
            
            OrderAggr createdOrder = OrderAggr.create(
                    "ORD202602130002",
                    "customer123",
                    "测试客户",
                    items,
                    TOTAL_AMOUNT,
                    shippingAddress,
                    contactInfo,
                    null
            );
            // 设置ID但不支付（状态为 CREATED）
            try {
                var idField = org.smm.archetype.domain.shared.base.Entity.class.getDeclaredField("id");
                idField.setAccessible(true);
                idField.set(createdOrder, ORDER_ID);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            RefundOrderCommand command = new RefundOrderCommand(
                    ORDER_ID,
                    "100.00",
                    CURRENCY,
                    "FULL",
                    "用户申请退款"
            );

            when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(createdOrder));

            // When & Then
            assertThatThrownBy(() -> orderAppService.refundOrder(command))
                    .isInstanceOf(BizException.class)
                    .satisfies(ex -> {
                        BizException bizEx = (BizException) ex;
                        assertThat(bizEx.getErrorCode()).isEqualTo(OrderErrorCode.ORDER_STATUS_INVALID);
                    });

            verify(orderRepository, never()).save(any());
        }

        @Test
        @DisplayName("退款金额超限 - BizException 正确传播")
        void testRefundOrder_AmountExceeded_ThrowsBizException() {
            // Given
            OrderAggr paidOrder = createPaidOrder();
            RefundOrderCommand command = new RefundOrderCommand(
                    ORDER_ID,
                    "200.00", // 超过订单金额
                    CURRENCY,
                    "FULL",
                    "用户申请退款"
            );

            when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(paidOrder));

            // When & Then
            assertThatThrownBy(() -> orderAppService.refundOrder(command))
                    .isInstanceOf(BizException.class)
                    .satisfies(ex -> {
                        BizException bizEx = (BizException) ex;
                        assertThat(bizEx.getErrorCode()).isEqualTo(OrderErrorCode.REFUND_AMOUNT_EXCEEDED);
                    });

            verify(orderRepository, never()).save(any());
        }

    }

    @Nested
    @DisplayName("释放库存测试")
    class ReleaseInventoryTests {

        @Test
        @DisplayName("释放库存失败 - 不影响退款流程")
        void testRefundOrder_ReleaseInventoryFails_DoesNotAffectRefund() {
            // Given
            OrderAggr paidOrder = createPaidOrder();
            RefundOrderCommand command = new RefundOrderCommand(
                    ORDER_ID,
                    "100.00",
                    CURRENCY,
                    "FULL",
                    "用户申请退款"
            );

            when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(paidOrder));
            when(orderRepository.save(any(OrderAggr.class))).thenAnswer(inv -> inv.getArgument(0));
            when(dtoConverter.toDTO(any(OrderAggr.class))).thenReturn(createRefundedOrderDTO());
            doThrow(new RuntimeException("库存服务不可用")).when(orderDomainService).releaseInventory(anyLong(), anyString());

            // When
            OrderDTO result = orderAppService.refundOrder(command);

            // Then - 退款仍然成功
            assertThat(result).isNotNull();
            assertThat(result.getStatus()).isEqualTo(OrderStatus.REFUNDED);
            verify(orderDomainService).releaseInventory(anyLong(), anyString());
        }

    }

}

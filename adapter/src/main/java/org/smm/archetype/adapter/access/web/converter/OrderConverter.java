package org.smm.archetype.adapter.access.web.converter;

import org.smm.archetype.adapter.access.web.dto.CreateOrderRequest;
import org.smm.archetype.adapter.access.web.dto.OrderDTO;
import org.smm.archetype.adapter.access.web.dto.OrderItemDTO;
import org.smm.archetype.adapter.access.web.dto.OrderItemRequest;
import org.smm.archetype.adapter.access.web.dto.PayOrderRequest;
import org.smm.archetype.app._example.order.command.CreateOrderCommand;
import org.smm.archetype.app._example.order.command.PayOrderCommand;
import org.smm.archetype.domain._example.order.model.Money;
import org.smm.archetype.domain._example.order.model.Order;
import org.smm.archetype.domain._example.order.model.OrderItem;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 订单DTO转换器
 * @author Leonardo
 * @since 2025/12/30
 */
public class OrderConverter {

    /**
     * CreateOrderRequest转CreateOrderCommand
     */
    public CreateOrderCommand toCommand(CreateOrderRequest request) {
        List<OrderItem> items = request.getItems().stream()
                                        .map(this::toOrderItem)
                                        .collect(Collectors.toList());

        return CreateOrderCommand.builder()
                       .customerId(request.getCustomerId())
                       .items(items)
                       .shippingAddress(request.getShippingAddress())
                       .phoneNumber(request.getPhoneNumber())
                       .build();
    }

    /**
     * PayOrderRequest转PayOrderCommand
     */
    public PayOrderCommand toCommand(PayOrderRequest request) {
        return new PayOrderCommand(request.getOrderId(), request.getPaymentMethod());
    }

    /**
     * OrderItemRequest转OrderItem
     */
    private OrderItem toOrderItem(OrderItemRequest request) {
        Money unitPrice = Money.of(request.getUnitPrice());
        return OrderItem.of(
                request.getProductId(),
                request.getProductName(),
                unitPrice,
                request.getQuantity()
        );
    }

    /**
     * Order转OrderDTO
     */
    public OrderDTO toDTO(Order order) {
        List<OrderItemDTO> itemDTOs = order.getItems().stream()
                                              .map(this::toDTO)
                                              .collect(Collectors.toList());

        return OrderDTO.builder()
                       .id(order.getId())
                       .orderId(order.getOrderId())
                       .customerId(order.getCustomerId())
                       .items(itemDTOs)
                       .totalAmount(order.getTotalAmount() != null ? order.getTotalAmount().getAmount() : null)
                       .currency(order.getTotalAmount() != null ? order.getTotalAmount().getCurrency() : null)
                       .status(order.getStatus() != null ? order.getStatus().name() : null)
                       .shippingAddress(order.getShippingAddress())
                       .phoneNumber(order.getPhoneNumber())
                       .paymentTime(order.getPaymentTime())
                       .shippingTime(order.getShippingTime())
                       .completedTime(order.getCompletedTime())
                       .cancelledTime(order.getCancelledTime())
                       .cancelReason(order.getCancelReason())
                       .createTime(order.getCreateTime())
                       .updateTime(order.getUpdateTime())
                       .build();
    }

    /**
     * OrderItem转OrderItemDTO
     */
    private OrderItemDTO toDTO(OrderItem item) {
        return OrderItemDTO.builder()
                       .productId(item.getProductId())
                       .productName(item.getProductName())
                       .unitPrice(item.getUnitPrice().getAmount())
                       .currency(item.getUnitPrice().getCurrency())
                       .quantity(item.getQuantity())
                       .subtotal(item.getSubtotal().getAmount())
                       .build();
    }

    /**
     * 批量转换Order列表为OrderDTO列表
     */
    public List<OrderDTO> toDTOList(List<Order> orders) {
        return orders.stream()
                       .map(this::toDTO)
                       .collect(Collectors.toList());
    }

}

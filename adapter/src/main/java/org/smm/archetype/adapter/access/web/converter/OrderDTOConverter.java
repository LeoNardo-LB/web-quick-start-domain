package org.smm.archetype.adapter.access.web.converter;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
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

/**
 * 订单DTO转换器（Adapter层）
 *
 * <p>负责请求DTO与领域对象之间的转换。
 * @author Leonardo
 * @since 2025/12/30
 */
@Mapper(componentModel = "spring")
public interface OrderDTOConverter {

    /**
     * CreateOrderRequest转CreateOrderCommand
     */
    @Mapping(target = "items", source = "items", qualifiedByName = "toOrderItemList")
    CreateOrderCommand toCommand(CreateOrderRequest request);

    /**
     * PayOrderRequest转PayOrderCommand
     */
    default PayOrderCommand toCommand(PayOrderRequest request) {
        return new PayOrderCommand(request.getOrderId(), request.getPaymentMethod());
    }

    /**
     * Order转OrderDTO
     */
    @Mapping(target = "totalAmount", source = "totalAmount", qualifiedByName = "moneyAmount")
    @Mapping(target = "currency", source = "totalAmount", qualifiedByName = "moneyCurrency")
    @Mapping(target = "status", source = "status", qualifiedByName = "statusName")
    OrderDTO toDTO(Order order);

    /**
     * OrderItem转OrderItemDTO
     */
    @Mapping(target = "unitPrice", source = "unitPrice", qualifiedByName = "moneyAmount")
    @Mapping(target = "currency", source = "unitPrice", qualifiedByName = "moneyCurrency")
    @Mapping(target = "subtotal", source = "subtotal", qualifiedByName = "moneyAmount")
    OrderItemDTO toDTO(OrderItem item);

    /**
     * 批量转换Order列表为OrderDTO列表
     */
    List<OrderDTO> toDTOList(List<Order> orders);

    /**
     * OrderItemRequest转OrderItem
     */
    @Named("toOrderItem")
    default OrderItem toOrderItem(OrderItemRequest request) {
        Money unitPrice = Money.of(request.getUnitPrice());
        return OrderItem.of(
                request.getProductId(),
                request.getProductName(),
                unitPrice,
                request.getQuantity()
        );
    }

    /**
     * 批量转换OrderItemRequest列表为OrderItem列表
     */
    @Named("toOrderItemList")
    default List<OrderItem> toOrderItemList(List<OrderItemRequest> requests) {
        return requests.stream()
                       .map(this::toOrderItem)
                       .toList();
    }

    /**
     * 获取Money金额
     */
    @Named("moneyAmount")
    default java.math.BigDecimal moneyAmount(Money money) {
        return money != null ? money.getAmount() : null;
    }

    /**
     * 获取Money货币
     */
    @Named("moneyCurrency")
    default String moneyCurrency(Money money) {
        return money != null ? money.getCurrency() : null;
    }

    /**
     * 获取枚举名称
     */
    @Named("statusName")
    default String statusName(org.smm.archetype.domain._example.order.model.OrderStatus status) {
        return status != null ? status.name() : null;
    }

}

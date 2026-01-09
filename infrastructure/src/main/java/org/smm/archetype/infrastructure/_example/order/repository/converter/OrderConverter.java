package org.smm.archetype.infrastructure._example.order.repository.converter;

import org.mapstruct.Mapper;
import org.smm.archetype.domain._example.order.model.Money;
import org.smm.archetype.domain._example.order.model.Order;
import org.smm.archetype.domain._example.order.model.OrderItem;
import org.smm.archetype.domain._example.order.model.OrderStatus;
import org.smm.archetype.infrastructure._example.order.repository.entity.OrderDO;
import org.smm.archetype.infrastructure._example.order.repository.entity.OrderItemDO;
import org.smm.archetype.infrastructure._shared.converter.BaseDomainConverter;

import java.util.List;

/**
 * 订单转换器
 *
 * <p>职责：在Order和OrderDO之间进行转换</p>
 *
 * <h3>使用说明</h3>
 * <ul>
 *   <li>使用{@code toEntity()}将DO转换为Entity（不包含items）</li>
 *   <li>使用{@code toDataObject()}将Entity转换为DO</li>
 *   <li>使用{@code reconstructOrder()}重建完整的聚合根（包含items）</li>
 * </ul>
 * @author Leonardo
 * @since 2025-01-09
 */
@Mapper(componentModel = "spring", uses = OrderItemConverter.class)
public interface OrderConverter extends BaseDomainConverter<Order, OrderDO> {

    @Override
    default Order toEntity(OrderDO dataObject) {
        // 使用Order.reconstruct静态方法重建Order
        Money totalAmount = Money.of(
                dataObject.getTotalAmount(),
                dataObject.getCurrency()
        );

        return Order.reconstruct(
                dataObject.getOrderId(),
                dataObject.getCustomerId(),
                List.of(), // items单独添加
                totalAmount,
                OrderStatus.valueOf(dataObject.getStatus()),
                dataObject.getShippingAddress(),
                dataObject.getPhoneNumber(),
                dataObject.getPaymentTime(),
                dataObject.getShippingTime(),
                dataObject.getCompletedTime(),
                dataObject.getCancelledTime(),
                dataObject.getCancelReason(),
                dataObject.getId(),
                dataObject.getCreateTime(),
                dataObject.getUpdateTime(),
                dataObject.getCreateUser(),
                dataObject.getUpdateUser(),
                dataObject.getVersion()
        );
    }

    @Override
    default OrderDO toDataObject(Order entity) {
        OrderDO orderDO = new OrderDO();
        orderDO.setId(entity.getId());
        orderDO.setOrderId(entity.getOrderId());
        orderDO.setCustomerId(entity.getCustomerId());
        orderDO.setTotalAmount(entity.getTotalAmount().getAmount());
        orderDO.setCurrency(entity.getTotalAmount().getCurrency());
        orderDO.setStatus(entity.getStatus().name());
        orderDO.setShippingAddress(entity.getShippingAddress());
        orderDO.setPhoneNumber(entity.getPhoneNumber());
        orderDO.setPaymentTime(entity.getPaymentTime());
        orderDO.setShippingTime(entity.getShippingTime());
        orderDO.setCompletedTime(entity.getCompletedTime());
        orderDO.setCancelledTime(entity.getCancelledTime());
        orderDO.setCancelReason(entity.getCancelReason());
        orderDO.setVersion(entity.getVersion());
        orderDO.setCreateTime(entity.getCreateTime());
        orderDO.setUpdateTime(entity.getUpdateTime());
        orderDO.setCreateUser(entity.getCreateUser());
        orderDO.setUpdateUser(entity.getUpdateUser());
        return orderDO;
    }

    /**
     * 重建订单聚合根（包含订单项）
     *
     * <p>此方法用于从数据库重建完整的订单聚合根
     * @param orderDO       订单数据对象
     * @param itemDOs       订单项数据对象列表
     * @param itemConverter 订单项转换器
     * @return 订单聚合根
     */
    default Order reconstructOrder(OrderDO orderDO, List<OrderItemDO> itemDOs, OrderItemConverter itemConverter) {
        // 先转换所有订单项
        List<OrderItem> items = itemDOs.stream()
                                        .map(itemConverter::toEntity)
                                        .toList();

        // 使用Order.reconstruct静态方法，一次性传入所有items
        Money totalAmount = Money.of(
                orderDO.getTotalAmount(),
                orderDO.getCurrency()
        );

        return Order.reconstruct(
                orderDO.getOrderId(),
                orderDO.getCustomerId(),
                items, // 完整的items列表
                totalAmount,
                OrderStatus.valueOf(orderDO.getStatus()),
                orderDO.getShippingAddress(),
                orderDO.getPhoneNumber(),
                orderDO.getPaymentTime(),
                orderDO.getShippingTime(),
                orderDO.getCompletedTime(),
                orderDO.getCancelledTime(),
                orderDO.getCancelReason(),
                orderDO.getId(),
                orderDO.getCreateTime(),
                orderDO.getUpdateTime(),
                orderDO.getCreateUser(),
                orderDO.getUpdateUser(),
                orderDO.getVersion()
        );
    }

}

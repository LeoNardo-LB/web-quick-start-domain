package org.smm.archetype.infrastructure._example.order.repository.converter;

import org.mapstruct.Mapper;
import org.smm.archetype.domain._example.order.model.Money;
import org.smm.archetype.domain._example.order.model.OrderItem;
import org.smm.archetype.infrastructure._example.order.repository.entity.OrderItemDO;
import org.smm.archetype.infrastructure._shared.converter.BaseDomainConverter;

/**
 * 订单项转换器
 *
 * <p>职责：在OrderItem和OrderItemDO之间进行转换</p>
 * <p>注意：OrderItem使用静态工厂方法，需要自定义转换逻辑</p>
 * @author Leonardo
 * @since 2025-01-09
 */
@Mapper(componentModel = "spring")
public interface OrderItemConverter extends BaseDomainConverter<OrderItem, OrderItemDO> {

    @Override
    default OrderItem toEntity(OrderItemDO dataObject) {
        // 使用OrderItem的静态工厂方法
        Money unitPrice = Money.of(
                dataObject.getUnitPrice(),
                dataObject.getCurrency()
        );
        return OrderItem.of(
                dataObject.getProductId(),
                dataObject.getProductName(),
                unitPrice,
                dataObject.getQuantity()
        );
    }

    @Override
    default OrderItemDO toDataObject(OrderItem entity) {
        OrderItemDO itemDO = new OrderItemDO();
        itemDO.setProductId(entity.getProductId());
        itemDO.setProductName(entity.getProductName());
        itemDO.setUnitPrice(entity.getUnitPrice().getAmount());
        itemDO.setCurrency(entity.getUnitPrice().getCurrency());
        itemDO.setQuantity(entity.getQuantity());
        itemDO.setSubtotal(entity.getSubtotal().getAmount());
        return itemDO;
    }

}

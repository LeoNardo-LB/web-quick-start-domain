package org.smm.archetype.infrastructure._example.order.persistence.converter;

import org.smm.archetype.domain._example.order.model.OrderItem;
import org.smm.archetype.domain._example.order.model.valueobject.Money;
import org.smm.archetype.infrastructure._shared.generated.repository.entity.OrderItemDO;
import org.springframework.stereotype.Component;

/**
 * 订单项转换器
 *
 * <p>职责：
 * <ul>
 *   <li>OrderItem与OrderItemDO之间的转换</li>
 * </ul>
 * @author Leonardo
 * @since 2026/1/11
 */
@Component
public class OrderItemConverter {

    /**
     * DO转领域对象
     */
    public OrderItem toDomain(OrderItemDO itemDO) {
        if (itemDO == null) {
            return null;
        }

        return OrderItem.builder()
                       .setId(itemDO.getId())
                       .setProductId(itemDO.getProductId())
                       .setProductName(itemDO.getProductName())
                       .setSkuCode(itemDO.getSkuCode())
                       .setUnitPrice(itemDO.getUnitPrice() != null ? Money.of(itemDO.getUnitPrice()) : null)
                       .setQuantity(itemDO.getQuantity())
                       .setCurrency(itemDO.getCurrency())
                       .setSubtotal(itemDO.getSubtotal() != null ? Money.of(itemDO.getSubtotal()) : null)
                       .setCreateTime(itemDO.getCreateTime())
                       .setUpdateTime(itemDO.getUpdateTime())
                       .build();
    }

}

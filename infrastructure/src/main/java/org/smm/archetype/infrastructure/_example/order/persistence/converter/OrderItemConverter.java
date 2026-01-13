package org.smm.archetype.infrastructure._example.order.persistence.converter;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.smm.archetype.domain._example.order.model.OrderItem;
import org.smm.archetype.domain._example.order.model.valueobject.Money;
import org.smm.archetype.infrastructure._shared.generated.repository.entity.OrderItemDO;

/**
 * 订单项转换器（MapStruct实现）
 *
 * <p>职责：
 * <ul>
 *   <li>OrderItem（领域对象） → OrderItemDO（数据对象）</li>
 *   <li>OrderItemDO（数据对象） → OrderItem（领域对象）</li>
 *   <li>处理值对象转换（Money）</li>
 * </ul>
 *
 * <p>通过@Mapper(componentModel = "spring")自动生成@Component，支持Spring依赖注入
 * @author Leonardo
 * @since 2026/1/11
 */
@Mapper(componentModel = "spring", imports = {Money.class})
public interface OrderItemConverter {

    /**
     * DO转领域对象
     * @param itemDO 订单项DO
     * @return 订单项领域对象
     */
    @Mapping(target = "unitPrice", expression = "java(itemDO.getUnitPrice() != null ? Money.of(itemDO.getUnitPrice()) : null)")
    @Mapping(target = "subtotal", expression = "java(itemDO.getSubtotal() != null ? Money.of(itemDO.getSubtotal()) : null)")
    @Mapping(target = "version", ignore = true)
    OrderItem toDomain(OrderItemDO itemDO);

}

package org.smm.archetype.app.exampleorder.converter;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.smm.archetype.app.exampleorder.dto.AddressDTO;
import org.smm.archetype.app.exampleorder.dto.ContactInfoDTO;
import org.smm.archetype.app.exampleorder.dto.MoneyDTO;
import org.smm.archetype.app.exampleorder.dto.OrderDTO;
import org.smm.archetype.app.exampleorder.dto.OrderItemDTO;
import org.smm.archetype.domain.exampleorder.model.OrderAggr;
import org.smm.archetype.domain.exampleorder.model.OrderItem;
import org.smm.archetype.domain.exampleorder.model.RefundType;
import org.smm.archetype.domain.exampleorder.model.valueobject.Address;
import org.smm.archetype.domain.exampleorder.model.valueobject.ContactInfo;
import org.smm.archetype.domain.exampleorder.model.valueobject.Money;

import java.util.List;

/**
 * 订单 DTO 转换器，负责将 Domain 层的领域对象转换为 Application 层的 DTO 对象。
 *
 * <p>使用 MapStruct 自动生成实现类，通过 Spring 进行依赖注入。</p>
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface OrderDtoConverter {

    /**
     * 将订单聚合根转换为 DTO
     *
     * @param order 订单聚合根
     * @return 订单 DTO
     */
    @Mapping(target = "createTime", ignore = true)
    OrderDTO toDTO(OrderAggr order);

    /**
     * 批量转换订单聚合根列表
     *
     * @param orders 订单聚合根列表
     * @return 订单 DTO 列表
     */
    List<OrderDTO> toDTOList(List<OrderAggr> orders);

    // ========== 嵌套对象转换 ==========

    /**
     * 将金额值对象转换为 DTO
     */
    MoneyDTO toDTO(Money money);

    /**
     * 将订单项实体转换为 DTO
     */
    @Mapping(target = "unitPrice", source = "unitPrice.amount")
    @Mapping(target = "subtotal", source = "subtotal.amount")
    @Mapping(target = "currency", source = "currency")
    OrderItemDTO toDTO(OrderItem item);

    /**
     * 批量转换订单项列表
     */
    List<OrderItemDTO> toOrderItemDTOList(List<OrderItem> items);

    /**
     * 将地址值对象转换为 DTO
     */
    AddressDTO toDTO(Address address);

    /**
     * 将联系信息值对象转换为 DTO
     */
    ContactInfoDTO toDTO(ContactInfo contactInfo);

    // ========== 枚举转换 ==========

    /**
     * 将退款类型枚举转换为字符串
     */
    default String toString(RefundType refundType) {
        return refundType != null ? refundType.name() : null;
    }

}

package org.smm.archetype.adapter.exampleorder.converter;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.smm.archetype.adapter.exampleorder.web.dto.response.AddressResponse;
import org.smm.archetype.adapter.exampleorder.web.dto.response.ContactInfoResponse;
import org.smm.archetype.adapter.exampleorder.web.dto.response.MoneyResponse;
import org.smm.archetype.adapter.exampleorder.web.dto.response.OrderItemResponse;
import org.smm.archetype.adapter.exampleorder.web.dto.response.OrderResponse;
import org.smm.archetype.app.exampleorder.dto.AddressDTO;
import org.smm.archetype.app.exampleorder.dto.ContactInfoDTO;
import org.smm.archetype.app.exampleorder.dto.MoneyDTO;
import org.smm.archetype.app.exampleorder.dto.OrderDTO;
import org.smm.archetype.app.exampleorder.dto.OrderItemDTO;

import java.util.List;

/**
 * 订单响应转换器，负责将 Application 层的 DTO 对象转换为 Web 层的 Response 对象。
 *
 * <p>使用 MapStruct 自动生成实现类，通过 Spring 进行依赖注入。</p>
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface OrderResponseConverter {

    /**
     * 将订单 DTO 转换为响应对象
     *
     * @param dto 订单 DTO
     * @return 订单响应
     */
    OrderResponse toResponse(OrderDTO dto);

    /**
     * 批量转换订单 DTO 列表
     *
     * @param dtos 订单 DTO 列表
     * @return 订单响应列表
     */
    List<OrderResponse> toOrderResponseList(List<OrderDTO> dtos);

    // ========== 嵌套对象转换 ==========

    /**
     * 将金额 DTO 转换为响应对象
     */
    MoneyResponse toResponse(MoneyDTO dto);

    /**
     * 将订单项 DTO 转换为响应对象
     */
    OrderItemResponse toResponse(OrderItemDTO dto);

    /**
     * 批量转换订单项 DTO 列表
     */
    List<OrderItemResponse> toOrderItemResponseList(List<OrderItemDTO> dtos);

    /**
     * 将地址 DTO 转换为响应对象
     */
    AddressResponse toResponse(AddressDTO dto);

    /**
     * 将联系信息 DTO 转换为响应对象
     */
    ContactInfoResponse toResponse(ContactInfoDTO dto);

}

package org.smm.archetype.adapter.exampleorder.converter;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.smm.archetype.adapter.exampleorder.web.dto.request.CancelOrderRequest;
import org.smm.archetype.adapter.exampleorder.web.dto.request.CreateOrderRequest;
import org.smm.archetype.adapter.exampleorder.web.dto.request.PayOrderRequest;
import org.smm.archetype.adapter.exampleorder.web.dto.request.RefundOrderRequest;
import org.smm.archetype.app.exampleorder.command.CancelOrderCommand;
import org.smm.archetype.app.exampleorder.command.CreateOrderCommand;
import org.smm.archetype.app.exampleorder.command.PayOrderCommand;
import org.smm.archetype.app.exampleorder.command.RefundOrderCommand;
import org.smm.archetype.domain.exampleorder.model.valueobject.Address;
import org.smm.archetype.domain.exampleorder.model.valueobject.ContactInfo;
import org.smm.archetype.domain.exampleorder.model.valueobject.OrderItemInfo;

import java.util.List;

/**
 * 订单请求转换器，负责将 Web 层的 Request 对象转换为 Application 层的 Command 对象。
 *
 * <p>使用 MapStruct 自动生成实现类，通过 Spring 进行依赖注入。</p>
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface OrderRequestConverter {

    /**
     * 将创建订单请求转换为命令对象
     *
     * @param request 创建订单请求
     * @return 创建订单命令
     */
    CreateOrderCommand toCommand(CreateOrderRequest request);

    /**
     * 将支付订单请求转换为命令对象
     *
     * @param request 支付订单请求
     * @return 支付订单命令
     */
    PayOrderCommand toCommand(PayOrderRequest request);

    /**
     * 将取消订单请求转换为命令对象
     *
     * @param request 取消订单请求
     * @return 取消订单命令
     */
    CancelOrderCommand toCommand(CancelOrderRequest request);

    /**
     * 将退款订单请求转换为命令对象
     *
     * @param request 退款订单请求
     * @return 退款订单命令
     */
    RefundOrderCommand toCommand(RefundOrderRequest request);

    // ========== 嵌套对象转换 ==========

    /**
     * 将订单项请求转换为订单项信息值对象
     */
    OrderItemInfo toOrderItemInfo(CreateOrderRequest.OrderItemRequest request);

    /**
     * 批量转换订单项请求列表
     */
    List<OrderItemInfo> toOrderItemInfoList(List<CreateOrderRequest.OrderItemRequest> requests);

    /**
     * 将地址请求转换为地址值对象
     */
    Address toAddress(CreateOrderRequest.AddressRequest request);

    /**
     * 将联系信息请求转换为联系信息值对象
     */
    ContactInfo toContactInfo(CreateOrderRequest.ContactInfoRequest request);

}

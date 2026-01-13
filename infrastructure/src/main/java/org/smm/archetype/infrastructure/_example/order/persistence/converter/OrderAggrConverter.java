package org.smm.archetype.infrastructure._example.order.persistence.converter;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.smm.archetype.domain._example.order.model.OrderAggr;
import org.smm.archetype.domain._example.order.model.OrderStatus;
import org.smm.archetype.domain._example.order.model.PaymentMethod;
import org.smm.archetype.infrastructure._shared.generated.repository.entity.OrderAggrDO;

/**
 * 订单聚合根DO转换器（MapStruct实现）
 *
 * <p>职责：
 * <ul>
 *   <li>OrderAggr（领域对象） → OrderAggrDO（数据对象）</li>
 *   <li>处理值对象转换（Money）</li>
 *   <li>处理枚举转换（OrderStatus、PaymentMethod）</li>
 * </ul>
 *
 * <p>注意：
 * <ul>
 *   <li>DO → OrderAggr 转换由 OrderAggrRepositoryImpl 使用反射处理</li>
 *   <li>关联数据（items、address、contactInfo）需要额外查询</li>
 * </ul>
 *
 * <p>通过@Mapper(componentModel = "spring")自动生成@Component，支持Spring依赖注入
 * @author Leonardo
 * @since 2026/1/11
 */
@Mapper(
        componentModel = "spring",
        imports = {OrderStatus.class, PaymentMethod.class}
)
public interface OrderAggrConverter {

    /**
     * 领域对象转DO（用于新增）
     * @param order 订单聚合根
     * @return 订单DO
     */
    @Mapping(target = "totalAmount", source = "totalAmount.amount")
    @Mapping(target = "status", expression = "java(order.getStatus().name())")
    @Mapping(target = "paymentMethod", expression = "java(order.getPaymentMethod().name())")
    OrderAggrDO toDO(OrderAggr order);

    /**
     * 更新DO（用于修改）
     * @param order   订单聚合根
     * @param orderDO 订单DO（更新目标）
     */
    @Mapping(target = "totalAmount", source = "totalAmount.amount")
    @Mapping(target = "status", expression = "java(order.getStatus().name())")
    @Mapping(target = "paymentMethod", expression = "java(order.getPaymentMethod().name())")
    @Mapping(target = "id", source = "order.id")
    @Mapping(target = "createTime", source = "order.createTime")
    @Mapping(target = "updateTime", source = "order.updateTime")
    @Mapping(target = "createUser", source = "order.createUser")
    @Mapping(target = "updateUser", source = "order.updateUser")
    @Mapping(target = "orderNo", source = "order.orderNo")
    @Mapping(target = "customerId", source = "order.customerId")
    @Mapping(target = "customerName", source = "order.customerName")
    @Mapping(target = "currency", source = "order.currency")
    @Mapping(target = "remark", source = "order.remark")
    void updateDO(OrderAggr order, @MappingTarget OrderAggrDO orderDO);

}

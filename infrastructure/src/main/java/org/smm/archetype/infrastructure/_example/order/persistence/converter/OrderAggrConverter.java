package org.smm.archetype.infrastructure._example.order.persistence.converter;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.smm.archetype.domain._example.order.model.OrderAggr;
import org.smm.archetype.domain._example.order.model.OrderStatus;
import org.smm.archetype.domain._example.order.model.PaymentMethod;
import org.smm.archetype.infrastructure._shared.generated.repository.entity.OrderAggrDO;

/**
 * 订单聚合根DO转换器
 *
 * <p>职责：
 * <ul>
 *   <li>OrderAggr（领域对象） → OrderAggrDO（数据对象）</li>
 *   <li>OrderAggrDO（数据对象） → OrderAggr（领域对象）</li>
 *   <li>处理值对象转换（Money）</li>
 *   <li>处理枚举转换（OrderStatus、PaymentMethod）</li>
 * </ul>
 *
 * <p>注意：
 * <ul>
 *   <li>toDomain 方法需要手动处理，因为OrderAggr是聚合根，字段没有setter</li>
 *   <li>关联数据（items、address、contactInfo）需要额外查询</li>
 * </ul>
 * @author Leonardo
 * @since 2026/1/11
 */
@Mapper(componentModel = "spring")
public interface OrderAggrConverter {

    /**
     * 领域对象转DO（用于新增）
     * @param order 订单聚合根
     * @return 订单DO
     */
    @Mapping(target = "totalAmount", source = "totalAmount.amount")
    @Mapping(target = "status", source = "status", qualifiedByName = "statusToString")
    @Mapping(target = "paymentMethod", source = "paymentMethod", qualifiedByName = "paymentMethodToString")
    OrderAggrDO toDO(OrderAggr order);

    /**
     * DO转领域对象（用于查询）
     * <p>注意：此方法返回一个空的OrderAggr对象，需要手动设置字段
     * <p>由于OrderAggr是聚合根，字段没有setter，所以不能使用MapStruct自动转换
     * @param orderDO 订单DO
     * @return 订单聚合根（需要手动初始化）
     */
    default OrderAggr toDomain(OrderAggrDO orderDO) {
        // 由于OrderAggr的聚合根特性，字段没有setter
        // 这里返回null，由RepositoryImpl手动处理
        return null;
    }

    /**
     * 更新DO（用于修改）
     * @param order   订单聚合根
     * @param orderDO 订单DO
     */
    @Mapping(target = "totalAmount", source = "totalAmount.amount")
    @Mapping(target = "status", source = "status", qualifiedByName = "statusToString")
    @Mapping(target = "paymentMethod", source = "paymentMethod", qualifiedByName = "paymentMethodToString")
    void updateDO(OrderAggr order, @MappingTarget OrderAggrDO orderDO);

    // ==================== 自定义转换方法 ====================

    /**
     * OrderStatus → String
     */
    @Named("statusToString")
    default String statusToString(OrderStatus status) {
        if (status == null) {
            return null;
        }
        return status.name();
    }

    /**
     * PaymentMethod → String
     */
    @Named("paymentMethodToString")
    default String paymentMethodToString(PaymentMethod method) {
        if (method == null) {
            return null;
        }
        return method.name();
    }

}

package org.smm.archetype.infrastructure._example.order.persistence.converter;

import org.smm.archetype.domain._example.order.model.OrderAggr;
import org.smm.archetype.domain._example.order.model.OrderStatus;
import org.smm.archetype.domain._example.order.model.PaymentMethod;
import org.smm.archetype.infrastructure._shared.generated.repository.entity.OrderAggrDO;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 订单聚合根DO转换器
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
 * @author Leonardo
 * @since 2026/1/11
 */
@Component
public class OrderAggrConverter {

    /**
     * 领域对象转DO（用于新增）
     * @param order 订单聚合根
     * @return 订单DO
     */
    public OrderAggrDO toDO(OrderAggr order) {
        if (order == null) {
            return null;
        }

        return OrderAggrDO.builder()
                       .totalAmount(orderTotalAmountAmount(order))
                       .status(statusToString(order.getStatus()))
                       .paymentMethod(paymentMethodToString(order.getPaymentMethod()))
                       .orderNo(order.getOrderNo())
                       .customerId(order.getCustomerId())
                       .customerName(order.getCustomerName())
                       .currency(order.getCurrency())
                       .remark(order.getRemark())
                       .build();
    }

    /**
     * 更新DO（用于修改）
     * @param order   订单聚合根
     * @param orderDO 订单DO
     */
    public void updateDO(OrderAggr order, OrderAggrDO orderDO) {
        if (order == null || orderDO == null) {
            return;
        }

        orderDO.setTotalAmount(orderTotalAmountAmount(order));
        orderDO.setStatus(statusToString(order.getStatus()));
        orderDO.setPaymentMethod(paymentMethodToString(order.getPaymentMethod()));
        orderDO.setId(order.getId());
        orderDO.setCreateTime(order.getCreateTime());
        orderDO.setUpdateTime(order.getUpdateTime());
        orderDO.setCreateUser(order.getCreateUser());
        orderDO.setUpdateUser(order.getUpdateUser());
        orderDO.setOrderNo(order.getOrderNo());
        orderDO.setCustomerId(order.getCustomerId());
        orderDO.setCustomerName(order.getCustomerName());
        orderDO.setCurrency(order.getCurrency());
        orderDO.setRemark(order.getRemark());
    }

    // ==================== 私有转换方法 ====================

    /**
     * 获取订单总金额数值
     * @param orderAggr 订单聚合根
     * @return 金额数值
     */
    private BigDecimal orderTotalAmountAmount(OrderAggr orderAggr) {
        if (orderAggr == null || orderAggr.getTotalAmount() == null) {
            return null;
        }
        return orderAggr.getTotalAmount().getAmount();
    }

    /**
     * OrderStatus → String
     * @param status 订单状态
     * @return 字符串
     */
    private String statusToString(OrderStatus status) {
        if (status == null) {
            return null;
        }
        return status.name();
    }

    /**
     * PaymentMethod → String
     * @param method 支付方式
     * @return 字符串
     */
    private String paymentMethodToString(PaymentMethod method) {
        if (method == null) {
            return null;
        }
        return method.name();
    }

}

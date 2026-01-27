package org.smm.archetype.domain.example.model.event;

import lombok.Getter;
import org.smm.archetype.domain.example.model.valueobject.Address;
import org.smm.archetype.domain.example.model.valueobject.ContactInfo;
import org.smm.archetype.domain.example.model.valueobject.Money;
import org.smm.archetype.domain.bizshared.base.DomainEvent;
import org.smm.archetype.domain.bizshared.event.EventPriority;

import java.util.List;

/**
 * 订单创建事件
 *
 * <p>当订单成功创建时发布此事件
 * @author Leonardo
 * @since 2026/1/11
 */
@Getter
public class OrderCreatedEvent extends DomainEvent {

    /**
     * 订单ID
     */
    private final Long orderId;

    /**
     * 订单编号
     */
    private final String orderNo;

    /**
     * 客户ID
     */
    private final String customerId;

    /**
     * 客户姓名
     */
    private final String customerName;

    /**
     * 订单总金额
     */
    private final Money totalAmount;

    /**
     * 收货地址
     */
    private final Address shippingAddress;

    /**
     * 联系信息
     */
    private final ContactInfo contactInfo;

    /**
     * 订单项列表（商品ID、SKU、数量）
     */
    private final List<OrderItemInfo> orderItems;

    /**
     * 构造函数
     */
    public OrderCreatedEvent(
            Long orderId,
            String orderNo,
            String customerId,
            String customerName,
            Money totalAmount,
            Address shippingAddress,
            ContactInfo contactInfo,
            List<OrderItemInfo> orderItems) {
        super();
        this.orderId = orderId;
        this.orderNo = orderNo;
        this.customerId = customerId;
        this.customerName = customerName;
        this.totalAmount = totalAmount;
        this.shippingAddress = shippingAddress;
        this.contactInfo = contactInfo;
        this.orderItems = orderItems;
        // 订单创建事件为高优先级，需要立即处理库存锁定
        setPriority(EventPriority.HIGH);
    }

    @Override
    public String toString() {
        return String.format("OrderCreatedEvent{orderId=%d, orderNo='%s', customerId='%s', totalAmount=%s}",
                orderId, orderNo, customerId, totalAmount);
    }

    /**
         * 订单项信息（简化版）
         */
        public record OrderItemInfo(String productId, String skuCode, Integer quantity) {

    }

}

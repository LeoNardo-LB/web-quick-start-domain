package org.smm.archetype.domain.exampleorder.model.event;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.smm.archetype.domain.exampleorder.model.valueobject.Address;
import org.smm.archetype.domain.exampleorder.model.valueobject.ContactInfo;
import org.smm.archetype.domain.exampleorder.model.valueobject.Money;
import org.smm.archetype.domain.shared.event.dto.DomainEventDTO;

import java.util.List;

/**
 * 订单创建事件
 *
当订单成功创建时发布此事件


 */
@Getter
@SuperBuilder(setterPrefix = "set", builderMethodName = "OCEBuilder")
public class OrderCreatedEventDTO extends DomainEventDTO {

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

    @Override
    public String toString() {
        return String.format("OrderCreatedEventDTO{orderId=%d, orderNo='%s', customerId='%s', totalAmount=%s}",
                orderId, orderNo, customerId, totalAmount);
    }

    /**
     * 订单项信息（简化版）
     *
     * @param productId 商品ID
     * @param skuCode   SKU编码
     * @param quantity  数量
     */
    public record OrderItemInfo(String productId, String skuCode, Integer quantity) {

    }

}

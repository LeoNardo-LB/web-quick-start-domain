package org.smm.archetype.domain.example.model.valueobject;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.smm.archetype.domain.bizshared.base.ValueObject;

/**
 * 订单项信息值对象
 *
 * <p>特征：
 * <ul>
 *   <li>不可变性（Immutable）</li>
 *   <li>包含商品ID、SKU、单价、数量</li>
 *   <li>用于创建订单项</li>
 * </ul>
 * @author Leonardo
 * @since 2026/1/11
 */
@Getter
@SuperBuilder(setterPrefix = "set")
public class OrderItemInfo extends ValueObject {

    /**
     * 商品ID
     */
    private final String productId;

    /**
     * 商品名称
     */
    private final String productName;

    /**
     * SKU编码
     */
    private final String skuCode;

    /**
     * 单价
     */
    private final Money unitPrice;

    /**
     * 数量
     */
    private final int quantity;

    /**
     * 计算小计金额
     * @return 小计金额
     */
    public Money calculateSubtotal() {
        return unitPrice.multiply(quantity);
    }

}

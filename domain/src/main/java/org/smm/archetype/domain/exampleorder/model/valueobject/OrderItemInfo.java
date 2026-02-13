package org.smm.archetype.domain.exampleorder.model.valueobject;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.smm.archetype.domain.shared.base.ValueObject;

/**
 * 订单项信息值对象，包含商品和价格信息。
 */
@Getter
@AllArgsConstructor
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

    @Override
    protected Object[] equalityFields() {
        return new Object[] {productId, productName, skuCode, unitPrice, quantity};
    }

}

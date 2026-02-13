package org.smm.archetype.domain.exampleorder.model;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.smm.archetype.domain.exampleorder.OrderErrorCode;
import org.smm.archetype.domain.exampleorder.model.valueobject.Money;
import org.smm.archetype.domain.shared.base.Entity;
import org.smm.archetype.domain.shared.exception.BizException;

/**
 * 订单项实体，属于订单聚合根。
 */
@Getter
@SuperBuilder(setterPrefix = "set")
public class OrderItem extends Entity {

    /**
     * 商品ID
     */
    private String productId;

    /**
     * 商品名称
     */
    private String productName;

    /**
     * SKU编码
     */
    private String skuCode;

    /**
     * 单价
     */
    private Money unitPrice;

    /**
     * 数量
     */
    private Integer quantity;

    /**
     * 货币类型
     */
    private String currency;

    /**
     * 小计金额
     */
    private Money subtotal;

    /**
     * 计算小计金额
     * @return 小计金额
     */
    public Money calculateSubtotal() {
        return unitPrice.multiply(quantity);
    }

    /**
     * 更新数量
     * @param quantity 新数量
     */
    public void updateQuantity(Integer quantity) {
        if (quantity <= 0) {
            throw new BizException(OrderErrorCode.QUANTITY_INVALID);
        }
        this.quantity = quantity;
        this.subtotal = calculateSubtotal();
        markAsUpdated();
    }

    /**
     * 验证数量
     * @return 数量有效返回true
     */
    public boolean isQuantityValid() {
        return quantity != null && quantity > 0;
    }

    @Override
    public String toString() {
        return String.format("OrderItem{productId='%s', productName='%s', quantity=%d, subtotal=%s}",
                productId, productName, quantity, subtotal);
    }

}

package org.smm.archetype.domain._example.order.model.valueobject;

import lombok.Getter;
import org.smm.archetype.domain._shared.base.ValueObject;

import java.util.Objects;

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
     * 私有构造函数
     */
    private OrderItemInfo(Builder builder) {
        this.productId = Objects.requireNonNull(builder.productId, "商品ID不能为空");
        this.productName = Objects.requireNonNull(builder.productName, "商品名称不能为空");
        this.skuCode = Objects.requireNonNull(builder.skuCode, "SKU编码不能为空");
        this.unitPrice = Objects.requireNonNull(builder.unitPrice, "单价不能为空");
        this.quantity = builder.quantity;

        // 验证数量
        if (this.quantity <= 0) {
            throw new IllegalArgumentException("数量必须大于0");
        }
    }

    /**
     * 创建构建器
     * @return 构建器
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * 计算小计金额
     * @return 小计金额
     */
    public Money calculateSubtotal() {
        return unitPrice.multiply(quantity);
    }

    @Override
    protected Object[] equalityFields() {
        return new Object[] {productId, skuCode, unitPrice, quantity};
    }

    @Override
    public String toString() {
        return String.format("%s x %d", productName, quantity);
    }

    /**
     * 构建器
     */
    public static class Builder {

        private String productId;
        private String productName;
        private String skuCode;
        private Money  unitPrice;
        private int    quantity = 1;

        public Builder productId(String productId) {
            this.productId = productId;
            return this;
        }

        public Builder productName(String productName) {
            this.productName = productName;
            return this;
        }

        public Builder skuCode(String skuCode) {
            this.skuCode = skuCode;
            return this;
        }

        public Builder unitPrice(Money unitPrice) {
            this.unitPrice = unitPrice;
            return this;
        }

        public Builder quantity(int quantity) {
            this.quantity = quantity;
            return this;
        }

        /**
         * 构建订单项信息对象
         * @return 订单项信息值对象
         */
        public OrderItemInfo build() {
            return new OrderItemInfo(this);
        }

    }

}

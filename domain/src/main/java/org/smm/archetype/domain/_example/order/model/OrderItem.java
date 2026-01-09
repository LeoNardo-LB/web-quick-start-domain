package org.smm.archetype.domain._example.order.model;

import lombok.Getter;
import org.smm.archetype.domain._shared.base.ValueObject;

import java.util.Objects;

/**
 * 订单项值对象
 * @author Leonardo
 * @since 2025/12/30
 */
@Getter
public class OrderItem extends ValueObject {

    private final String productId;
    private final String productName;
    private final Money  unitPrice;
    private final int    quantity;
    private final Money  subtotal;

    private OrderItem(String productId, String productName, Money unitPrice, int quantity) {
        this.productId = Objects.requireNonNull(productId, "Product id cannot be null");
        this.productName = Objects.requireNonNull(productName, "Product name cannot be null");
        this.unitPrice = Objects.requireNonNull(unitPrice, "Unit price cannot be null");

        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        this.quantity = quantity;

        this.subtotal = unitPrice.multiply(new java.math.BigDecimal(quantity));
    }

    public static OrderItem of(String productId, String productName, Money unitPrice, int quantity) {
        return new OrderItem(productId, productName, unitPrice, quantity);
    }

    @Override
    protected Object[] equalityFields() {
        return new Object[] {productId, unitPrice, quantity};
    }

    @Override
    public String toString() {
        return String.format("OrderItem{product='%s', qty=%d, price=%s}", productId, quantity, subtotal);
    }

}

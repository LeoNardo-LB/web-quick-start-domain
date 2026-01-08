package org.smm.archetype.domain.example.order.model;

import lombok.Getter;
import org.smm.archetype.domain._shared.base.ValueObject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * 金额值对象
 *
 * <p>值对象示例：
 * <ul>
 *   <li>不可变性</li>
 *   <li>封装金额计算逻辑</li>
 *   <li>防止精度丢失</li>
 *   <li>基于值的相等性</li>
 * </ul>
 * @author Leonardo
 * @since 2025/12/30
 */
@Getter
public class Money extends ValueObject {

    private static final String DEFAULT_CURRENCY = "CNY";
    private final BigDecimal amount;
    private final String     currency;

    private Money(BigDecimal amount, String currency) {
        this.amount = Objects.requireNonNull(amount, "Amount cannot be null")
                              .setScale(2, RoundingMode.HALF_UP);
        this.currency = Objects.requireNonNull(currency, "Currency cannot be null");

        if (this.amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount cannot be negative");
        }
    }

    public static Money of(BigDecimal amount) {
        return new Money(amount, DEFAULT_CURRENCY);
    }

    public static Money of(BigDecimal amount, String currency) {
        return new Money(amount, currency);
    }

    public static Money zero() {
        return new Money(BigDecimal.ZERO, DEFAULT_CURRENCY);
    }

    /**
     * 金额相加
     * @param other 另一个金额
     * @return 新的金额对象
     */
    public Money add(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("Cannot add money with different currencies");
        }
        return new Money(this.amount.add(other.amount), this.currency);
    }

    /**
     * 金额相乘
     * @param multiplier 乘数
     * @return 新的金额对象
     */
    public Money multiply(BigDecimal multiplier) {
        return new Money(this.amount.multiply(multiplier), this.currency);
    }

    /**
     * 大于
     * @param other 另一个金额
     * @return 如果大于返回true
     */
    public boolean greaterThan(Money other) {
        return this.amount.compareTo(other.amount) > 0;
    }

    @Override
    protected Object[] equalityFields() {
        return new Object[] {amount, currency};
    }

    @Override
    public String toString() {
        return amount.setScale(2, RoundingMode.HALF_UP) + " " + currency;
    }

}

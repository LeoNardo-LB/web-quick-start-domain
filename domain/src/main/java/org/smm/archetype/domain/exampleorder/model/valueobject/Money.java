package org.smm.archetype.domain.exampleorder.model.valueobject;

import lombok.Getter;
import org.smm.archetype.domain.exampleorder.OrderErrorCode;
import org.smm.archetype.domain.shared.base.ValueObject;
import org.smm.archetype.domain.shared.exception.BizException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * 金额值对象，提供精确的金额运算和比较。
 */
@Getter
public class Money extends ValueObject {

    private static final int SCALE = 2;

    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    /**
     * 金额
     */
    private final BigDecimal amount;

    /**
     * 货币类型（默认人民币）
     */
    private final String currency;

    /**
     * 私有构造函数
     */
    private Money(BigDecimal amount, String currency) {
        this.amount = Objects.requireNonNull(amount, "金额不能为空")
                              .setScale(SCALE, ROUNDING_MODE);
        this.currency = Objects.requireNonNull(currency, "货币类型不能为空");
    }

    /**
     * 创建金额对象（默认人民币）
     * @param amount 金额
     * @return 金额值对象
     */
    public static Money of(BigDecimal amount) {
        return new Money(amount, "CNY");
    }

    /**
     * 创建金额对象（指定货币）
     * @param amount   金额
     * @param currency 货币类型
     * @return 金额值对象
     */
    public static Money of(BigDecimal amount, String currency) {
        return new Money(amount, currency);
    }

    /**
     * 创建金额对象（从long）
     * @param amount 金额
     * @return 金额值对象
     */
    public static Money of(long amount) {
        return new Money(BigDecimal.valueOf(amount), "CNY");
    }

    /**
     * 创建零金额
     * @return 零金额值对象
     */
    public static Money zero() {
        return new Money(BigDecimal.ZERO, "CNY");
    }

    /**
     * 加法运算
     * @param other 另一个金额
     * @return 新的金额对象
     */
    public Money add(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new BizException(OrderErrorCode.CURRENCY_MISMATCH);
        }
        return new Money(this.amount.add(other.amount), this.currency);
    }

    /**
     * 减法运算
     * @param other 另一个金额
     * @return 新的金额对象
     */
    public Money subtract(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new BizException(OrderErrorCode.CURRENCY_MISMATCH);
        }
        return new Money(this.amount.subtract(other.amount), this.currency);
    }

    /**
     * 乘法运算
     * @param multiplier 乘数
     * @return 新的金额对象
     */
    public Money multiply(BigDecimal multiplier) {
        return new Money(this.amount.multiply(multiplier), this.currency);
    }

    /**
     * 乘法运算
     * @param multiplier 乘数
     * @return 新的金额对象
     */
    public Money multiply(int multiplier) {
        return multiply(BigDecimal.valueOf(multiplier));
    }

    /**
     * 除法运算
     * @param divisor 除数
     * @return 新的金额对象
     */
    public Money divide(BigDecimal divisor) {
        return new Money(this.amount.divide(divisor, SCALE, ROUNDING_MODE), this.currency);
    }

    /**
     * 比较大小
     * @param other 另一个金额
     * @return 大于返回正数，等于返回0，小于返回负数
     */
    public int compareTo(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new BizException(OrderErrorCode.CURRENCY_MISMATCH);
        }
        return this.amount.compareTo(other.amount);
    }

    /**
     * 是否大于
     * @param other 另一个金额
     * @return 大于返回true
     */
    public boolean greaterThan(Money other) {
        return compareTo(other) > 0;
    }

    /**
     * 是否小于
     * @param other 另一个金额
     * @return 小于返回true
     */
    public boolean lessThan(Money other) {
        return compareTo(other) < 0;
    }

    /**
     * 是否为零
     * @return 为零返回true
     */
    public boolean isZero() {
        return this.amount.compareTo(BigDecimal.ZERO) == 0;
    }

    /**
     * 是否为正数
     * @return 为正数返回true
     */
    public boolean isPositive() {
        return this.amount.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * 是否为负数
     * @return 为负数返回true
     */
    public boolean isNegative() {
        return this.amount.compareTo(BigDecimal.ZERO) < 0;
    }

    @Override
    protected Object[] equalityFields() {
        return new Object[] {amount, currency};
    }

    @Override
    public String toString() {
        return String.format("%s %s", amount, currency);
    }

}

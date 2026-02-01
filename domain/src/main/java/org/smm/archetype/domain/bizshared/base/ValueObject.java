package org.smm.archetype.domain.bizshared.base;

import lombok.RequiredArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Arrays;

/**
 * 值对象基类
 *
 * <p>值对象特征：
 * <ul>
 *   <li>不可变性（Immutable）</li>
 *   <li>基于属性值的相等性</li>
 *   <li>没有唯一标识</li>
 *   <li>可以自由共享</li>
 * </ul>
 *
 * <p>使用示例：
 * <pre>{@code
 * public class Money extends ValueObject {
 *     private final BigDecimal amount;
 *     private final String currency;
 *
 *     public Money(BigDecimal amount, String currency) {
 *         this.amount = Objects.requireNonNull(amount);
 *         this.currency = Objects.requireNonNull(currency);
 *     }
 * }
 * }</pre>
 * @author Leonardo
 * @since 2025/12/30
 */
@RequiredArgsConstructor
@SuperBuilder(setterPrefix = "set")
public abstract class ValueObject {

    /**
     * 获取用于相等性比较的属性值
     * <p>
     * 子类可以重写此方法来指定哪些属性参与相等性比较。
     * 默认实现返回null，表示使用所有字段（通过@EqualsAndHashCode）。
     * @return 参与相等性比较的属性值数组
     */
    protected Object[] equalityFields() {
        return null;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        Object[] fields = equalityFields();
        if (fields != null && fields.length > 0) {
            // 使用指定字段进行比较
            ValueObject that = (ValueObject) o;
            return Arrays.equals(fields, that.equalityFields());
        }

        // 使用Lombok生成的equals方法
        return super.equals(o);
    }

    @Override
    public final int hashCode() {
        Object[] fields = equalityFields();
        if (fields != null && fields.length > 0) {
            return Arrays.hashCode(fields);
        }
        return super.hashCode();
    }

}

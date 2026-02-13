package org.smm.archetype.domain.platform.search.query;

import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.smm.archetype.domain.shared.base.ValueObject;
import org.smm.archetype.domain.platform.search.enums.FilterOperator;

import java.util.List;

/**
 * 搜索过滤条件值对象
 *
 */
@Getter
@SuperBuilder(setterPrefix = "set")
public class SearchFilter extends ValueObject {

    /**
     * 字段名
     */
    private final String field;

    /**
     * 操作符
     */
    private final FilterOperator operator;

    /**
     * 单值（用于equals、range、gt、lt等）
     */
    private final Object value;

    /**
     * 范围值（用于range操作）
     */
    private final RangeValue rangeValue;

    /**
     * 多值（用于in操作）
     */
    private final List<Object> values;

    /**
     * 范围值（值对象）
     */
    @Getter
    @SuperBuilder(setterPrefix = "set")
    private static class RangeValue extends ValueObject {
        private final Object gte; // 大于等于
        private final Object lte; // 小于等于
        private final Object gt;  // 大于
        private final Object lt;  // 小于

        @Override
        protected Object[] equalityFields() {
            return new Object[] {gte, lte, gt, lt};
        }
    }

    @Override
    protected Object[] equalityFields() {
        return new Object[] {field, operator, value, rangeValue, values};
    }
}

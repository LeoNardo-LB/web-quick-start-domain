package org.smm.archetype.domain.common.search.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.smm.archetype.domain.common.search.enums.FilterOperator;

import java.util.List;

/**
 * 搜索过滤条件
 *


 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchFilter {

    /**
     * 字段名
     */
    private String field;

    /**
     * 操作符
     */
    private FilterOperator operator;

    /**
     * 单值（用于equals、range、gt、lt等）
     */
    private Object value;

    /**
     * 范围值（用于range操作）
     */
    private RangeValue rangeValue;

    /**
     * 多值（用于in操作）
     */
    private List<Object> values;

    /**
     * 范围值
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RangeValue {
        private Object gte; // 大于等于
        private Object lte; // 小于等于
        private Object gt;  // 大于
        private Object lt;  // 小于
    }
}

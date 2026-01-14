package org.smm.archetype.app._example.search.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.smm.archetype.domain.common.search.enums.SearchStrategy;

import java.util.List;

/**
 * 搜索命令对象
 *
 * @author Leonardo
 * @since 2026-01-14
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchCommand {

    /**
     * 搜索关键词
     */
    private String keyword;

    /**
     * 搜索策略
     */
    @Builder.Default
    private SearchStrategy strategy = SearchStrategy.BM25;

    /**
     * 过滤条件（JSON格式）
     *
     * <p>示例: [{"field":"category","operator":"EQ","value":"electronics"}]
     */
    private List<FilterCondition> filters;

    /**
     * 排序条件
     *
     * <p>示例: [{"field":"price","order":"DESC"}]
     */
    private List<SortCondition> sorts;

    /**
     * 分页页码（从1开始）
     */
    @Builder.Default
    private int pageNo = 1;

    /**
     * 每页大小
     */
    @Builder.Default
    private int pageSize = 10;

    /**
     * 聚合条件（JSON格式）
     */
    private List<AggregationCondition> aggregations;

    /**
     * 过滤条件
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FilterCondition {
        private String field;
        private String operator;
        private Object value;
    }

    /**
     * 排序条件
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SortCondition {
        private String field;
        private String order;
    }

    /**
     * 聚合条件
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AggregationCondition {
        private String name;
        private String type;
        private String field;
        private Integer size;
    }
}

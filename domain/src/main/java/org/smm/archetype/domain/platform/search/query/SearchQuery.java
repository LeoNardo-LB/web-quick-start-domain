package org.smm.archetype.domain.platform.search.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.smm.archetype.domain.platform.search.enums.SearchStrategy;

import java.util.List;

/**
 * 搜索查询对象，封装搜索条件和过滤参数。
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchQuery {

    /**
     * 搜索关键字
     */
    private String keyword;

    /**
     * 搜索策略
     */
    @Builder.Default
    private SearchStrategy strategy = SearchStrategy.BM25;

    /**
     * 过滤条件列表
     */
    @Builder.Default
    private List<SearchFilter> filters = List.of();

    /**
     * 排序条件列表
     */
    @Builder.Default
    private List<SearchSort> sorts = List.of();

    /**
     * 聚合条件列表
     */
    @Builder.Default
    private List<SearchAggregation> aggregations = List.of();

    /**
     * 分页：从第几条开始（0-based）
     */
    @Builder.Default
    private int from = 0;

    /**
     * 分页：返回多少条
     */
    @Builder.Default
    private int size = 10;

    /**
     * 是否需要高亮
     */
    @Builder.Default
    private boolean highlight = false;

    /**
     * 高亮字段列表
     */
    @Builder.Default
    private List<String> highlightFields = List.of();

    /**
     * 返回字段列表（空表示返回所有字段）
     */
    @Builder.Default
    private List<String> sourceFields = List.of();

    /**
     * 最小得分（用于过滤低相关性结果）
     */
    @Builder.Default
    private float minScore = 0.0f;
}

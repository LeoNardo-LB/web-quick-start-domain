package org.smm.archetype.domain.platform.search.result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 搜索结果对象，包含命中文档和聚合信息。
 *
 * @param <T> 文档类型
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchResult<T> {

    /**
     * 总命中数
     */
    private long totalHits;

    /**
     * 总得分（最大相关性得分）
     */
    private float maxScore;

    /**
     * 命中文档列表
     */
    @Builder.Default
    private List<SearchHit<T>> hits = List.of();

    /**
     * 聚合结果
     */
    @Builder.Default
    private List<SearchAggregationResult> aggregations = List.of();

    /**
     * 耗时（毫秒）
     */
    private long took;
}

package org.smm.archetype.app._example.search.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 搜索结果DTO
 *
 * @param <T> 文档类型
 * @author Leonardo
 * @since 2026-01-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchResultDTO<T> {

    /**
     * 总命中数
     */
    private Long total;

    /**
     * 最高得分
     */
    private Float maxScore;

    /**
     * 命中文档列表
     */
    @Builder.Default
    private List<SearchHitDTO<T>> hits = List.of();

    /**
     * 聚合结果列表
     */
    @Builder.Default
    private List<SearchAggregationResultDTO> aggregations = List.of();

    /**
     * 耗时（毫秒）
     */
    private Long took;
}

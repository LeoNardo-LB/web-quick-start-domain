package org.smm.archetype.domain.platform.search.result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 聚合桶
 *


 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AggregationBucket {

    /**
     * 桶的key（如类别名称）
     */
    private String key;

    /**
     * 文档数量
     */
    private long docCount;

    /**
     * 子聚合结果
     */
    @Builder.Default
    private List<SearchAggregationResult> subAggregations = List.of();
}

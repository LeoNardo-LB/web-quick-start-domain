package org.smm.archetype.domain.platform.search.result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 聚合结果
 *


 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchAggregationResult {

    /**
     * 聚合名称
     */
    private String name;

    /**
     * 桶列表（用于terms、range等聚合）
     */
    @Builder.Default
    private List<AggregationBucket> buckets = List.of();

    /**
     * 单值结果（用于sum、avg、min、max等聚合）
     */
    private double value;

    /**
     * 是否有值（用于判断是单值还是多值聚合）
     */
    @Builder.Default
    private boolean hasValue = false;

    /**
     * 创建单值聚合结果
     */
    public static SearchAggregationResult ofSingleValue(String name, double value) {
        return SearchAggregationResult.builder()
            .name(name)
            .value(value)
            .hasValue(true)
            .build();
    }

    /**
     * 创建多值聚合结果
     */
    public static SearchAggregationResult ofBuckets(String name, List<AggregationBucket> buckets) {
        return SearchAggregationResult.builder()
            .name(name)
            .buckets(buckets)
            .hasValue(false)
            .build();
    }
}

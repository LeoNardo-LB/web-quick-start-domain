package org.smm.archetype.app._example.search.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 搜索聚合结果DTO
 *
 * @author Leonardo
 * @since 2026-01-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchAggregationResultDTO {

    /**
     * 聚合名称
     */
    private String name;

    /**
     * 单值（用于SUM, AVG, MAX, MIN等）
     */
    private Double value;

    /**
     * 分桶结果（用于TERMS等）
     */
    private List<AggregationBucketDTO> buckets;

    /**
     * 聚合分桶DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AggregationBucketDTO {
        /**
         * 桶key
         */
        private String key;

        /**
         * 文档数量
         */
        private Long docCount;

        /**
         * 子聚合结果
         */
        private Double value;
    }
}

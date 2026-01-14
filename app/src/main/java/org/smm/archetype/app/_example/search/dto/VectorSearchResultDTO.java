package org.smm.archetype.app._example.search.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 向量搜索结果DTO
 *
 * @param <T> 文档类型
 * @author Leonardo
 * @since 2026-01-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VectorSearchResultDTO<T> {

    /**
     * 命中的文档列表
     */
    @Builder.Default
    private List<VectorSearchHitDTO<T>> hits = List.of();

    /**
     * 搜索耗时（毫秒）
     */
    private Long took;

    /**
     * 向量搜索命中项
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VectorSearchHitDTO<T> {
        /**
         * 文档ID
         */
        private String id;

        /**
         * 相似度分数
         */
        private Float score;

        /**
         * 文档内容
         */
        private T document;
    }
}

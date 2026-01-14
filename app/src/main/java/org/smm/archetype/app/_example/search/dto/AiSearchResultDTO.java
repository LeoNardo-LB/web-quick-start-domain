package org.smm.archetype.app._example.search.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * AI搜索结果DTO
 *
 * @param <T> 文档类型
 * @author Leonardo
 * @since 2026-01-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiSearchResultDTO<T> {

    /**
     * 命中的文档列表
     */
    @Builder.Default
    private List<AiSearchHitDTO<T>> hits = List.of();

    /**
     * 总命中数
     */
    private Long totalHits;

    /**
     * 搜索耗时（毫秒）
     */
    private Long took;

    /**
     * 重排序策略
     */
    private String rerankStrategy;

    /**
     * 查询扩展词
     */
    private List<String> expandedTerms;

    /**
     * AI搜索命中项
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AiSearchHitDTO<T> {
        /**
         * 文档ID
         */
        private String id;

        /**
         * 综合得分
         */
        private Float score;

        /**
         * BM25分数
         */
        private Float bm25Score;

        /**
         * AI分数
         */
        private Float aiScore;

        /**
         * 排名变化
         */
        private Integer rankChange;

        /**
         * 文档内容
         */
        private T document;

        /**
         * 额外信息
         */
        private Object extraInfo;
    }
}

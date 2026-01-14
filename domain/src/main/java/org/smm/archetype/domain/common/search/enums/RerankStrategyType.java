package org.smm.archetype.domain.common.search.enums;

/**
 * 重排序策略类型
 *
 * @author Leonardo
 * @since 2026-01-14
 */
public enum RerankStrategyType {

    /**
     * 不重排序
     *
     * <p>使用原始搜索结果排序
     */
    NONE,

    /**
     * 基于分数的重新加权
     *
     * <p>结合BM25和向量分数进行加权
     * <p>公式：final_score = α * bm25_score + (1-α) * vector_score
     */
    SCORE_WEIGHTED,

    /**
     * 基于排序位置的融合
     *
     * <p>结合BM25和向量的排序位置
     * <p>算法：Reciprocal Rank Fusion (RRF)
     */
    RRF,

    /**
     * AI模型重排序
     *
     * <p>使用AI模型（如Cohere Rerank）对结果重新排序
     * <p>特点：精度高、性能较低
     */
    AI_MODEL
}

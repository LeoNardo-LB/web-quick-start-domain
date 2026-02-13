package org.smm.archetype.domain.platform.search.enums;

/**
 * 重排序策略类型
 *


 */
public enum RerankStrategyType {

    /**
     * 不重排序
     *
    使用原始搜索结果排序
     */
    NONE,

    /**
     * 基于分数的重新加权
     *
    结合BM25和向量分数进行加权
    公式：final_score = α * bm25_score + (1-α) * vector_score
     */
    SCORE_WEIGHTED,

    /**
     * 基于排序位置的融合
     *
    结合BM25和向量的排序位置
    算法：Reciprocal Rank Fusion (RRF)
     */
    RRF,

    /**
     * AI模型重排序
     *
    使用AI模型（如Cohere Rerank）对结果重新排序
    特点：精度高、性能较低
     */
    AI_MODEL
}

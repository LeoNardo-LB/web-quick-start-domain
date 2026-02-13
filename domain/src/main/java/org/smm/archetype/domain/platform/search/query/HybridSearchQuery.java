package org.smm.archetype.domain.platform.search.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.smm.archetype.domain.platform.search.enums.VectorDistanceType;

import java.util.List;

/**
 * 混合搜索查询对象（BM25 + 向量）
 *


 */
@Getter
@Builder
@AllArgsConstructor
public class HybridSearchQuery {

    /**
     * 查询文本（用于BM25）
     */
    private final String queryText;

    /**
     * 查询向量（用于向量搜索）
     */
    private final List<Float> queryVector;

    /**
     * 向量字段名
     */
    @Builder.Default
    private final String vectorField = "vector";

    /**
     * 向量搜索的k值
     */
    @Builder.Default
    private final Integer k = 10;

    /**
     * 向量距离类型
     */
    @Builder.Default
    private final VectorDistanceType distanceType = VectorDistanceType.COSINE;

    /**
     * BM25权重
     *
    最终分数 = bm25Weight * bm25_score + (1 - bm25Weight) * vector_score
     */
    @Builder.Default
    private final Float bm25Weight = 0.5f;

    /**
     * 返回结果数量
     */
    @Builder.Default
    private final Integer size = 10;

    /**
     * 起始位置
     */
    @Builder.Default
    private final Integer from = 0;
}

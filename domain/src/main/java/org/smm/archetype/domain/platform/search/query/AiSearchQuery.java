package org.smm.archetype.domain.platform.search.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.smm.archetype.domain.platform.search.enums.AiSearchModelType;
import org.smm.archetype.domain.platform.search.enums.RerankStrategyType;

import java.util.List;

/**
 * AI增强搜索查询对象
 *


 */
@Getter
@Builder
@AllArgsConstructor
public class AiSearchQuery {

    /**
     * 查询文本（必填）
     */
    private final String queryText;

    /**
     * AI模型类型（必填）
     *
    默认: ELSER
     */
    @Builder.Default
    private final AiSearchModelType modelType = AiSearchModelType.ELSER;

    /**
     * 返回结果数量（必填）
     *
    默认: 10
     */
    @Builder.Default
    private final Integer size = 10;

    /**
     * 起始位置（可选）
     *
    默认: 0
     */
    @Builder.Default
    private final Integer from = 0;

    /**
     * 重排序策略（可选）
     *
    默认: NONE
     */
    @Builder.Default
    private final RerankStrategyType rerankStrategy = RerankStrategyType.NONE;

    /**
     * BM25权重（用于SCORE_WEIGHTED策略）
     *
    范围：[0, 1]
    默认: 0.5（表示BM25和向量分数各占50%）
     */
    @Builder.Default
    private final Float bm25Weight = 0.5f;

    /**
     * 过滤条件（可选）
     *
    支持在AI搜索前应用过滤条件
     */
    private final List<SearchFilter> filters;

    /**
     * 是否启用查询扩展（可选）
     *
    true表示使用AI模型扩展查询
    默认: false
     */
    @Builder.Default
    private final boolean enableQueryExpansion = false;

    /**
     * 查询扩展的最大词数（可选）
     *
    仅当enableQueryExpansion=true时有效
    默认: 5
     */
    @Builder.Default
    private final Integer maxExpansionTerms = 5;
}

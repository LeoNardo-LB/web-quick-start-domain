package org.smm.archetype.domain.platform.search.result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.util.Map;

/**
 * AI搜索命中文档
 *


 */
@Getter
@Builder
@AllArgsConstructor
@FieldDefaults(makeFinal = true, level = lombok.AccessLevel.PRIVATE)
public class AiSearchHit<T> {

    /**
     * 文档ID
     */
    private final String id;

    /**
     * 综合得分
     *
    经过重排序后的最终分数
     */
    private final Float score;

    /**
     * 文档源数据
     */
    private final T document;

    /**
     * BM25分数（可选）
     *
    原始全文搜索的分数
     */
    private final Float bm25Score;

    /**
     * 向量/AI分数（可选）
     *
    AI模型计算的分数
     */
    private final Float aiScore;

    /**
     * 重排名次变化（可选）
     *
    正值表示排名上升，负值表示排名下降
    0表示排名未变化
     */
    private final Integer rankChange;

    /**
     * 额外信息（可选）
     *
    包含高亮信息、查询扩展词等
     */
    private final Map<String, Object> extraInfo;
}

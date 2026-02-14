package org.smm.archetype.domain.platform.search.result;

import lombok.Builder;

import java.util.Map;

/**
 * AI搜索命中文档。
 *
 * @param <T>        文档类型
 * @param id         文档ID
 * @param score      综合得分
 *
 *                   经过重排序后的最终分数
 * @param document   文档源数据
 * @param bm25Score  BM25分数（可选）
 *
 *                   原始全文搜索的分数
 * @param aiScore    向量/AI分数（可选）
 *
 *                   AI模型计算的分数
 * @param rankChange 重排名次变化（可选）
 *
 *                   正值表示排名上升，负值表示排名下降
 *                   0表示排名未变化
 * @param extraInfo  额外信息（可选）
 *
 *                   包含高亮信息、查询扩展词等
 */
@Builder
public record AiSearchHit<T>(String id, Float score, T document, Float bm25Score, Float aiScore, Integer rankChange,
                             Map<String, Object> extraInfo) {

}

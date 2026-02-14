package org.smm.archetype.domain.platform.search.result;

import lombok.Builder;

import java.util.Map;

/**
 * 向量搜索命中文档。
 *
 * @param <T>       文档类型
 * @param id        文档ID
 * @param score     相似度分数
 *
 *                  根据距离类型不同，含义不同：
 *                  - COSINE: [-1, 1]，越大越相似
 *                  - L2: [0, +∞)，越小越相似
 *                  - DOT_PRODUCT: (-∞, +∞)，越大越相似
 * @param document  文档源数据
 * @param distance  距离值（可选）
 *
 *                  某些实现可能返回原始距离值
 * @param extraInfo 额外信息（可选）
 *
 *                  包含shard、node等元信息
 */
@Builder
public record VectorSearchHit<T>(String id, Float score, T document, Double distance,
                                 Map<String, Object> extraInfo) {

}

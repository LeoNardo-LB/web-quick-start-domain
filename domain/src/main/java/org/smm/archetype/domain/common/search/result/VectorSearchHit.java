package org.smm.archetype.domain.common.search.result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.util.Map;

/**
 * 向量搜索命中文档
 *


 */
@Getter
@Builder
@AllArgsConstructor
@FieldDefaults(makeFinal = true, level = lombok.AccessLevel.PRIVATE)
public class VectorSearchHit<T> {

    /**
     * 文档ID
     */
    private final String id;

    /**
     * 相似度分数
     *
     * <p>根据距离类型不同，含义不同：
     * <p>- COSINE: [-1, 1]，越大越相似
     * <p>- L2: [0, +∞)，越小越相似
     * <p>- DOT_PRODUCT: (-∞, +∞)，越大越相似
     */
    private final Float score;

    /**
     * 文档源数据
     */
    private final T document;

    /**
     * 距离值（可选）
     *
     * <p>某些实现可能返回原始距离值
     */
    private final Double distance;

    /**
     * 额外信息（可选）
     *
     * <p>包含shard、node等元信息
     */
    private final Map<String, Object> extraInfo;
}

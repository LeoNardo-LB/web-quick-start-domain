package org.smm.archetype.domain.platform.search.result;

import lombok.Builder;

import java.util.List;

/**
 * 向量搜索结果。
 *
 * @param <T>  文档类型
 * @param hits 命中的文档列表
 *
 *             按相似度排序
 * @param took 搜索耗时（毫秒）
 */
@Builder
public record VectorSearchResult<T>(List<VectorSearchHit<T>> hits, Long took) {

}

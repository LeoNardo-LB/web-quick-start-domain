package org.smm.archetype.domain.platform.search.result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.util.List;

/**
 * 向量搜索结果
 *


 */
@Getter
@Builder
@AllArgsConstructor
@FieldDefaults(makeFinal = true, level = lombok.AccessLevel.PRIVATE)
public class VectorSearchResult<T> {

    /**
     * 命中的文档列表
     *
    按相似度排序
     */
    private final List<VectorSearchHit<T>> hits;

    /**
     * 搜索耗时（毫秒）
     */
    private final Long took;
}

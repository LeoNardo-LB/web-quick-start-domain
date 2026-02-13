package org.smm.archetype.domain.platform.search.result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.util.List;

/**
 * AI增强搜索结果
 *


 */
@Getter
@Builder
@AllArgsConstructor
@FieldDefaults(makeFinal = true, level = lombok.AccessLevel.PRIVATE)
public class AiSearchResult<T> {

    /**
     * 命中的文档列表
     *
    按综合得分排序
     */
    private final List<AiSearchHit<T>> hits;

    /**
     * 总命中数
     */
    private final Long totalHits;

    /**
     * 搜索耗时（毫秒）
     */
    private final Long took;

    /**
     * 查询扩展词（可选）
     *
    当启用查询扩展时，包含AI生成的扩展词
     */
    private final List<String> expandedTerms;
}

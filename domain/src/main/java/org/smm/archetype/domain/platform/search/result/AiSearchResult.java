package org.smm.archetype.domain.platform.search.result;

import lombok.Builder;

import java.util.List;

/**
 * AI增强搜索结果。
 *
 * @param <T>           文档类型
 * @param hits          命中的文档列表
 *
 *                      按综合得分排序
 * @param totalHits     总命中数
 * @param took          搜索耗时（毫秒）
 * @param expandedTerms 查询扩展词（可选）
 *
 *                      当启用查询扩展时，包含AI生成的扩展词
 */
@Builder
public record AiSearchResult<T>(List<AiSearchHit<T>> hits, Long totalHits, Long took, List<String> expandedTerms) {

}

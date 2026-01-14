package org.smm.archetype.domain.common.search.result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 命中文档
 *
 * @author Leonardo
 * @since 2026-01-14
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchHit<T> {

    /**
     * 文档ID
     */
    private String id;

    /**
     * 得分
     */
    private float score;

    /**
     * 文档内容
     */
    private T document;

    /**
     * 高亮结果
     */
    @Builder.Default
    private Map<String, java.util.List<String>> highlights = Map.of();
}

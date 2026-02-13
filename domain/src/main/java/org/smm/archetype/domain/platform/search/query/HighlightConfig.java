package org.smm.archetype.domain.platform.search.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * 高亮配置对象
 *


 */
@Getter
@Builder
@AllArgsConstructor
public class HighlightConfig {

    /**
     * 高亮字段列表
     */
    private final List<String> fields;

    /**
     * 前置标签
     */
    @Builder.Default
    private final String preTag = "<em>";

    /**
     * 后置标签
     */
    @Builder.Default
    private final String postTag = "</em>";

    /**
     * 片段大小
     */
    @Builder.Default
    private final Integer fragmentSize = 150;

    /**
     * 返回片段数量
     */
    @Builder.Default
    private final Integer numberOfFragments = 3;
}

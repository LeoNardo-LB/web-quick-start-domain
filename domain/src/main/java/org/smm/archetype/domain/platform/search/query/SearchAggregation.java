package org.smm.archetype.domain.platform.search.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.smm.archetype.domain.platform.search.enums.AggregationType;

import java.util.List;

/**
 * 搜索聚合条件
 *


 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchAggregation {

    /**
     * 聚合名称（用于识别结果）
     */
    private String name;

    /**
     * 聚合类型
     */
    private AggregationType type;

    /**
     * 聚合字段
     */
    private String field;

    /**
     * 聚合大小（用于terms聚合）
     */
    @Builder.Default
    private int size = 10;

    /**
     * 子聚合列表
     */
    @Builder.Default
    private List<SearchAggregation> subAggregations = List.of();

    /**
     * 脚本（用于script聚合）
     */
    private String script;

    /**
     * 缺失值处理
     */
    private Object missing;
}

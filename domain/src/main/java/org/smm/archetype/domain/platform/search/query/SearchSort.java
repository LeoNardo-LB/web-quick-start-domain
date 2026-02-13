package org.smm.archetype.domain.platform.search.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.smm.archetype.domain.platform.search.enums.SortOrder;

/**
 * 搜索排序条件
 *


 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchSort {

    /**
     * 字段名
     */
    private String field;

    /**
     * 排序方向
     */
    @Builder.Default
    private SortOrder order = SortOrder.ASC;

    /**
     * 是否按得分排序（特殊模式）
     */
    @Builder.Default
    private boolean byScore = false;
}

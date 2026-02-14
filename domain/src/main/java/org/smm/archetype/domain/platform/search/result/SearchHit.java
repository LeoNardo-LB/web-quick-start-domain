package org.smm.archetype.domain.platform.search.result;

import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.smm.archetype.domain.shared.base.ValueObject;

import java.util.Map;

/**
 * 命中文档值对象
 *
 * @param <T> 文档类型
 */
@Getter
@SuperBuilder(setterPrefix = "set")
public class SearchHit<T> extends ValueObject {

    /**
     * 文档ID
     */
    private final String id;

    /**
     * 得分
     */
    private final float score;

    /**
     * 文档内容
     */
    private final T document;

    /**
     * 高亮结果
     */
    @Builder.Default
    private final Map<String, java.util.List<String>> highlights = Map.of();

    @Override
    protected Object[] equalityFields() {
        return new Object[] {id, score, document, highlights};
    }
}

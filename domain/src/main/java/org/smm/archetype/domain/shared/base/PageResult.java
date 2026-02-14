package org.smm.archetype.domain.shared.base;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * 基础分页结果。
 *
 * @param <T> 记录类型
 */
@Getter
@Setter
@SuperBuilder(setterPrefix = "set")
public abstract class PageResult<T> {

    private Long pageNumber;

    private Long pageSize;

    private List<T> records;

    private Long totalRaw;

}

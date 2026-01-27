package org.smm.archetype.domain.bizshared.base;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * 基础分页结果
 * @author Leonardo
 * @since 2026/1/6
 */

@Getter
@Setter
@SuperBuilder(setterPrefix = "set")
public abstract class BasePageResult<T> {

    private Long pageNumber;

    private Long pageSize;

    private List<T> records;

    private Long totalRaw;

}

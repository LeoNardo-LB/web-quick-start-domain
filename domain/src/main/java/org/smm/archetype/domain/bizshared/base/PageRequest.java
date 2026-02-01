package org.smm.archetype.domain.bizshared.base;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

/**
 * 基础分页请求
 * @author Leonardo
 * @since 2026/1/6
 */
@Getter
@SuperBuilder(setterPrefix = "set")
public abstract class PageRequest {

    private String preLastId;

    private Long pageNumber;

    private Long pageSize;

    private Long total;

}

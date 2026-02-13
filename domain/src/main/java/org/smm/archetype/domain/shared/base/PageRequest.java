package org.smm.archetype.domain.shared.base;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

/**
 * 基础分页请求


 */
@Getter
@SuperBuilder(setterPrefix = "set")
public abstract class PageRequest {

    private String preLastId;

    private Long pageNumber;

    private Long pageSize;

    private Long total;

}

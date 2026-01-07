package org.smm.archetype.adapter._shared.result;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import org.smm.archetype.domain._shared.base.BasePage;

import java.io.Serializable;
import java.util.List;

/**
 * 分页结果
 * @author Leonardo
 * @since 2026/1/6
 */
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(setterPrefix = "set", builderMethodName = "pageResultBuilder")
public class PageResult<T extends List<?>> extends BaseResult<T> implements Serializable, BasePage {

    /**
     * 页码
     */
    protected Long pageNumber;

    /**
     * 页大小
     */
    protected Long pageSize;

    /**
     * 总数
     */
    protected Long totalRaw;

}

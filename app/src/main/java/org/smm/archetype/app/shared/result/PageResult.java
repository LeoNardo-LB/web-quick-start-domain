package org.smm.archetype.app.shared.result;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.List;

/**
 * 分页结果对象，封装分页数据和总记录数。
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(setterPrefix = "set", builderMethodName = "PRBuilder")
public class PageResult<T extends List<?>> extends BaseResult<T> implements Serializable {

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

    /**
     * 总页数
     */
    protected Long totalPage() {
        return totalRaw == null ? 0 : (totalRaw + pageSize - 1) / pageSize;
    }

}

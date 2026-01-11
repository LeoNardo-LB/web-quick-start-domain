package org.smm.archetype.app._shared.result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 应用层分页结果
 *
 * <p>职责：封装应用层的分页查询结果
 * @param <T> 记录类型
 * @author Leonardo
 * @since 2026/01/12
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {

    /**
     * 页码（从1开始）
     */
    private Long pageNumber;

    /**
     * 每页大小
     */
    private Long pageSize;

    /**
     * 总记录数
     */
    private Long totalRaw;

    /**
     * 当前页数据
     */
    private List<T> records;

    /**
     * 总页数
     */
    public Long getTotalPages() {
        if (pageSize == null || pageSize == 0) {
            return 0L;
        }
        return (totalRaw + pageSize - 1) / pageSize;
    }

    /**
     * 是否有下一页
     */
    public boolean hasNext() {
        return pageNumber < getTotalPages();
    }

    /**
     * 是否有上一页
     */
    public boolean hasPrevious() {
        return pageNumber > 1;
    }

}

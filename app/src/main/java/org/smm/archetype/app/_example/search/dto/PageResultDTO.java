package org.smm.archetype.app._example.search.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 分页结果DTO
 *
 * @param <T> 数据类型
 * @author Leonardo
 * @since 2026-01-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResultDTO<T> {

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 当前页码（从1开始）
     */
    private Integer pageNo;

    /**
     * 每页大小
     */
    private Integer pageSize;

    /**
     * 总页数
     */
    private Integer totalPages;

    /**
     * 数据列表
     */
    @Builder.Default
    private List<T> items = List.of();

    /**
     * 是否有下一页
     */
    public boolean hasNext() {
        return pageNo < totalPages;
    }

    /**
     * 是否有上一页
     */
    public boolean hasPrevious() {
        return pageNo > 1;
    }
}

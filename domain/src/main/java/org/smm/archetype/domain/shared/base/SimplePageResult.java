package org.smm.archetype.domain.shared.base;

import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * 简单分页结果实现。
 * @param <T> 记录类型
 */
@SuperBuilder(setterPrefix = "set")
public class SimplePageResult<T> extends PageResult<T> {

    /**
     * 创建分页结果。
     * @param pageNumber 页码（从1开始）
     * @param pageSize   每页大小
     * @param records    记录列表
     * @param totalRaw   总记录数
     * @param <T>        记录类型
     * @return 分页结果
     */
    public static <T> SimplePageResult<T> of(Long pageNumber, Long pageSize, List<T> records, Long totalRaw) {
        return SimplePageResult.<T>builder()
                       .setPageNumber(pageNumber)
                       .setPageSize(pageSize)
                       .setRecords(records)
                       .setTotalRaw(totalRaw)
                       .build();
    }

}

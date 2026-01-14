package org.smm.archetype.app._example.search.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 搜索命中文档DTO
 *
 * @param <T> 文档类型
 * @author Leonardo
 * @since 2026-01-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchHitDTO<T> {

    /**
     * 文档ID
     */
    private String id;

    /**
     * 相关性得分
     */
    private Float score;

    /**
     * 文档内容
     */
    private T document;
}

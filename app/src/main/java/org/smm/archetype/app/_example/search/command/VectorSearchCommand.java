package org.smm.archetype.app._example.search.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 向量搜索命令对象
 *
 * @author Leonardo
 * @since 2026-01-14
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VectorSearchCommand {

    /**
     * 查询向量
     */
    private List<Float> vector;

    /**
     * 向量字段名
     */
    @Builder.Default
    private String vectorField = "vector";

    /**
     * 返回top-K结果
     */
    @Builder.Default
    private Integer k = 10;

    /**
     * 索引类型
     */
    private String indexType;

    /**
     * 距离类型
     */
    @Builder.Default
    private String distanceType = "cosine";

    /**
     * 过滤条件
     */
    private List<SearchCommand.FilterCondition> filters;

    /**
     * HNSW参数：efSearch
     */
    private Integer efSearch;

    /**
     * IVF参数：nprobes
     */
    private Integer nprobes;
}

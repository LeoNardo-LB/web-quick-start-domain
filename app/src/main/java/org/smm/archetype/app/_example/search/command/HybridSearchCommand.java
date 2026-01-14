package org.smm.archetype.app._example.search.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 混合搜索命令对象（BM25 + 向量搜索）
 *
 * @author Leonardo
 * @since 2026-01-14
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HybridSearchCommand {

    /**
     * 查询文本（用于BM25搜索）
     */
    private String queryText;

    /**
     * 查询向量（用于向量搜索）
     */
    private List<Float> queryVector;

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
     * 距离类型
     */
    @Builder.Default
    private String distanceType = "cosine";

    /**
     * BM25权重（0.0-1.0）
     *
     * <p>0.0 = 仅向量搜索
     * <p>0.5 = BM25和向量各占一半
     * <p>1.0 = 仅BM25搜索
     */
    @Builder.Default
    private Float bm25Weight = 0.5f;

    /**
     * 返回结果数量
     */
    @Builder.Default
    private Integer size = 10;

    /**
     * 起始位置
     */
    @Builder.Default
    private Integer from = 0;

    /**
     * 分页页码（从1开始）
     */
    @Builder.Default
    private Integer pageNo = 1;

    /**
     * 每页大小
     */
    @Builder.Default
    private Integer pageSize = 10;
}

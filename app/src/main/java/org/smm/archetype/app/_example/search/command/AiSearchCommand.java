package org.smm.archetype.app._example.search.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * AI搜索命令对象
 *
 * @author Leonardo
 * @since 2026-01-14
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiSearchCommand {

    /**
     * 查询文本
     */
    private String queryText;

    /**
     * AI模型类型
     */
    @Builder.Default
    private String modelType = "elser";

    /**
     * 重排序策略
     *
     * <p>NONE - 无重排序
     * <p>SCORE_WEIGHTED - 加权融合
     * <p>RRF - 倒数排名融合
     * <p>AI_MODEL - AI模型重排序
     */
    @Builder.Default
    private String rerankStrategy = "none";

    /**
     * BM25权重（用于SCORE_WEIGHTED策略）
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
     * 过滤条件
     */
    private List<SearchCommand.FilterCondition> filters;

    /**
     * 是否启用查询扩展
     */
    @Builder.Default
    private Boolean enableQueryExpansion = false;
}

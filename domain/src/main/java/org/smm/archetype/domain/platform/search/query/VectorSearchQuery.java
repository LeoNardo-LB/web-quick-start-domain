package org.smm.archetype.domain.platform.search.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.smm.archetype.domain.platform.search.enums.VectorDistanceType;
import org.smm.archetype.domain.platform.search.enums.VectorIndexType;

import java.util.List;

/**
 * 向量搜索查询对象，支持向量相似度搜索。
 */
@Getter
@Builder
@AllArgsConstructor
public class VectorSearchQuery {

    /**
     * 向量数据（必填）
     */
    private List<Float> vector;

    /**
     * 向量字段名称（必填）
     *
    默认: vector
     */
    @Builder.Default
    private String vectorField = "vector";

    /**
     * 返回结果数量（k值）（必填）
     *
    默认: 10
     */
    @Builder.Default
    private Integer k = 10;

    /**
     * 索引类型（可选）
     *
    用于指定查询时使用的索引类型
    null表示使用索引配置的默认类型
     */
    private VectorIndexType indexType;

    /**
     * 距离类型（必填）
     *
    默认: COSINE
     */
    @Builder.Default
    private VectorDistanceType distanceType = VectorDistanceType.COSINE;

    /**
     * 过滤条件（可选）
     *
    支持在向量搜索前应用过滤条件
     */
    private List<SearchFilter> filters;

    /**
     * IVF参数：nprobes（可选）
     *
    仅在IVF索引时有效
    指定搜索时探测的倒排文件桶数量
    null表示使用索引默认值
     */
    private Integer nprobes;

    /**
     * HNSW参数：ef_search（可选）
     *
    仅在HNSW索引时有效
    指定搜索时的候选队列大小
    值越大，准确率越高，但性能越低
    null表示使用索引默认值
     */
    private Integer efSearch;
}

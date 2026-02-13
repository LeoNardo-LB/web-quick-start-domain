package org.smm.archetype.domain.platform.search.enums;

/**
 * 向量索引类型
 *


 */
public enum VectorIndexType {

    /**
     * HNSW索引（Hierarchical Navigable Small World）
     *
    特点：高性能、高召回率、内存占用大
    适用场景：实时搜索、对准确率要求高的场景
     */
    HNSW,

    /**
     * IVF索引（Inverted File Index）
     *
    特点：平衡性能和资源、可控精度
    适用场景：大规模数据、可接受一定精度损失
     */
    IVF,

    /**
     * FLAT索引（暴力搜索）
     *
    特点：100%准确率、性能差、无需额外存储
    适用场景：小规模数据、基准测试
     */
    FLAT
}

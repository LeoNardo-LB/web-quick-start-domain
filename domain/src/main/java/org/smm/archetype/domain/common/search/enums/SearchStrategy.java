package org.smm.archetype.domain.common.search.enums;

/**
 * 搜索策略枚举
 *
 * <p>定义支持的搜索策略类型
 *
 * @author Leonardo
 * @since 2026-01-14
 */
public enum SearchStrategy {

    /**
     * BM25算法（传统全文搜索）
     */
    BM25,

    /**
     * 向量搜索（kNN）
     */
    VECTOR,

    /**
     * 混合搜索（BM25 + kNN）
     */
    HYBRID,

    /**
     * 语义搜索（ELSER）
     */
    SEMANTIC
}

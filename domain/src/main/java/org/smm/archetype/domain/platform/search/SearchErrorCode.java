package org.smm.archetype.domain.platform.search;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.smm.archetype.domain.shared.exception.ErrorCode;

/**
 * 搜索模块错误码枚举
 *
 * <p>定义搜索相关的所有系统错误码。</p>
 *
 * <p>错误码命名规范：SEARCH-xxx</p>
 */
@Getter
@RequiredArgsConstructor
public enum SearchErrorCode implements ErrorCode {

    // ========== 搜索操作错误 ==========

    /**
     * 搜索失败
     */
    SEARCH_FAILED("SEARCH-001", "搜索操作失败"),

    /**
     * 索引文档失败
     */
    INDEX_FAILED("SEARCH-002", "索引文档失败"),

    /**
     * 批量索引失败
     */
    BATCH_INDEX_FAILED("SEARCH-003", "批量索引失败"),

    /**
     * 获取文档失败
     */
    GET_DOCUMENT_FAILED("SEARCH-004", "获取文档失败"),

    /**
     * 批量获取失败
     */
    BATCH_GET_FAILED("SEARCH-005", "批量获取失败"),

    /**
     * 构建查询DSL失败
     */
    DSL_BUILD_FAILED("SEARCH-006", "构建查询DSL失败"),

    // ========== 向量搜索错误 ==========

    /**
     * 向量搜索失败
     */
    VECTOR_SEARCH_FAILED("SEARCH-010", "执行向量搜索失败"),

    /**
     * 创建向量索引失败
     */
    VECTOR_INDEX_FAILED("SEARCH-011", "创建向量索引失败"),

    // ========== AI搜索错误 ==========

    /**
     * AI搜索失败
     */
    AI_SEARCH_FAILED("SEARCH-020", "执行AI搜索失败"),

    /**
     * 混合搜索失败
     */
    HYBRID_SEARCH_FAILED("SEARCH-021", "执行混合搜索失败"),

    ;

    private final String code;
    private final String message;

}

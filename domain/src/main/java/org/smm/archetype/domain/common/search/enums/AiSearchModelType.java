package org.smm.archetype.domain.common.search.enums;

/**
 * AI搜索模型类型
 *
 * @author Leonardo
 * @since 2026-01-14
 */
public enum AiSearchModelType {

    /**
     * ELSER（Elastic Learned Sparse Encoder）
     *
     * <p>Elastic官方的语义搜索模型
     * <p>特点：稀疏向量、BM25兼容、无需外部依赖
     * <p>适用场景：文本语义搜索
     */
    ELSER,

    /**
     * Cohere Embed模型
     *
     * <p>第三方嵌入模型
     * <p>特点：高精度、多语言支持
     * <p>适用场景：需要高精度的语义搜索
     */
    COHERE_EMBED,

    /**
     * OpenAI Embedding模型
     *
     * <p>OpenAI的嵌入模型
     * <p>特点：性能优秀、成本较高
     * <p>适用场景：对精度要求高的场景
     */
    OPENAI_EMBEDDING
}

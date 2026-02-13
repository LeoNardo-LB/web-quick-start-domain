package org.smm.archetype.domain.platform.search.enums;

/**
 * AI搜索模型类型
 *


 */
public enum AiSearchModelType {

    /**
     * ELSER（Elastic Learned Sparse Encoder）
     *
    Elastic官方的语义搜索模型
    特点：稀疏向量、BM25兼容、无需外部依赖
    适用场景：文本语义搜索
     */
    ELSER,

    /**
     * Cohere Embed模型
     *
    第三方嵌入模型
    特点：高精度、多语言支持
    适用场景：需要高精度的语义搜索
     */
    COHERE_EMBED,

    /**
     * OpenAI Embedding模型
     *
    OpenAI的嵌入模型
    特点：性能优秀、成本较高
    适用场景：对精度要求高的场景
     */
    OPENAI_EMBEDDING
}

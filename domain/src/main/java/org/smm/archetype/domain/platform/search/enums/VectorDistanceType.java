package org.smm.archetype.domain.platform.search.enums;

/**
 * 向量距离类型
 *


 */
public enum VectorDistanceType {

    /**
     * 余弦相似度
     *
    范围：[-1, 1]，越大越相似
    特点：不受向量长度影响
    适用场景：文本语义搜索、推荐系统
     */
    COSINE,

    /**
     * 欧氏距离（L2距离）
     *
    范围：[0, +∞)，越小越相似
    特点：考虑向量长度
    适用场景：图像搜索、特征匹配
     */
    L2,

    /**
     * 点积（内积）
     *
    范围：(-∞, +∞)，越大越相似
    特点：计算速度快
    适用场景：归一化向量搜索
     */
    DOT_PRODUCT
}

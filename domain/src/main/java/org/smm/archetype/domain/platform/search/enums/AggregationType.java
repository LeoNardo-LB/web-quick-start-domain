package org.smm.archetype.domain.platform.search.enums;

/**
 * 聚合类型枚举
 *
定义支持的聚合类型
 *


 */
public enum AggregationType {

    /**
     * 词项聚合（分组统计）
     */
    TERMS,

    /**
     * 范围聚合
     */
    RANGE,

    /**
     * 日期直方图聚合
     */
    DATE_HISTOGRAM,

    /**
     * 直方图聚合
     */
    HISTOGRAM,

    /**
     * 平均值
     */
    AVG,

    /**
     * 求和
     */
    SUM,

    /**
     * 最小值
     */
    MIN,

    /**
     * 最大值
     */
    MAX,

    /**
     * 统计（包含avg、sum、min、max等）
     */
    STATS,

    /**
     * 顶部命中
     */
    TOP_HITS
}

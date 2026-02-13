package org.smm.archetype.domain.platform.search.enums;

/**
 * 过滤操作符枚举
 *
定义支持的过滤操作符类型
 *


 */
public enum FilterOperator {

    /**
     * 等于
     */
    EQ,

    /**
     * 不等于
     */
    NE,

    /**
     * 大于
     */
    GT,

    /**
     * 大于等于
     */
    GTE,

    /**
     * 小于
     */
    LT,

    /**
     * 小于等于
     */
    LTE,

    /**
     * 范围查询
     */
    RANGE,

    /**
     * 在列表中
     */
    IN,

    /**
     * 不在列表中
     */
    NIN,

    /**
     * 存在
     */
    EXISTS,

    /**
     * 不存在
     */
    NOT_EXISTS,

    /**
     * 前缀匹配
     */
    PREFIX,

    /**
     * 通配符匹配
     */
    WILDCARD,

    /**
     * 模糊匹配
     */
    FUZZY
}

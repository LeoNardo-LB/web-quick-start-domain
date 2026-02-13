package org.smm.archetype.domain.exampleorder.model;

import lombok.RequiredArgsConstructor;

/**
 * 退款类型枚举
 */
@RequiredArgsConstructor
public enum RefundType {
    /**
     * 全额退款
     */
    FULL("全额退款"),

    /**
     * 部分退款
     */
    PARTIAL("部分退款");

    private final String description;

    /**
     * 获取描述
     */
    public String getDescription() {
        return description;
    }
}

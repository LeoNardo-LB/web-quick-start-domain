package org.smm.archetype.domain._example.order.model;

import lombok.RequiredArgsConstructor;

/**
 * 订单状态枚举
 *
 * <p>订单状态流转：
 * <pre>
 * CREATED → PAID → SHIPPED → COMPLETED
 *    ↓         ↓
 * CANCELLED CANCELLED
 * </pre>
 * @author Leonardo
 * @since 2026/1/11
 */
@RequiredArgsConstructor
public enum OrderStatus {
    /**
     * 已创建 - 订单初始状态
     */
    CREATED("已创建"),

    /**
     * 已支付 - 订单支付完成
     */
    PAID("已支付"),

    /**
     * 已取消 - 订单已取消
     */
    CANCELLED("已取消"),

    /**
     * 已发货 - 订单已发货
     */
    SHIPPED("已发货"),

    /**
     * 已完成 - 订单已完成
     */
    COMPLETED("已完成");

    private final String description;

    /**
     * 获取状态描述
     */
    public String getDescription() {
        return description;
    }

    /**
     * 检查是否可以支付
     */
    public boolean canPay() {
        return this == CREATED;
    }

    /**
     * 检查是否可以取消
     */
    public boolean canCancel() {
        return this == CREATED || this == PAID;
    }

    /**
     * 检查是否可以发货
     */
    public boolean canShip() {
        return this == PAID;
    }

    /**
     * 检查是否可以完成
     */
    public boolean canComplete() {
        return this == SHIPPED;
    }

    /**
     * 检查是否为终态
     */
    public boolean isTerminalState() {
        return this == CANCELLED || this == COMPLETED;
    }
}

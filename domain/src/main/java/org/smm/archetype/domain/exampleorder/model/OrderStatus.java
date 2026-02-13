package org.smm.archetype.domain.exampleorder.model;

import lombok.RequiredArgsConstructor;

/**
 * 订单状态枚举，支持状态流转验证。
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
    COMPLETED("已完成"),

    /**
     * 已退款 - 订单全额退款
     */
    REFUNDED("已退款"),

    /**
     * 部分退款 - 订单部分退款
     */
    PARTIALLY_REFUNDED("部分退款");

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
     * 检查是否可以退款
     * <p>只有已支付或部分退款的订单可以退款</p>
     */
    public boolean canRefund() {
        return this == PAID || this == PARTIALLY_REFUNDED;
    }

    /**
     * 检查是否可以部分退款
     * <p>只有已支付的订单可以首次部分退款</p>
     */
    public boolean canPartialRefund() {
        return this == PAID || this == PARTIALLY_REFUNDED;
    }

    /**
     * 检查是否为终态
     */
    public boolean isTerminalState() {
        return this == CANCELLED || this == COMPLETED || this == REFUNDED;
    }
}

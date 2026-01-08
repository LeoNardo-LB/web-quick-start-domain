package org.smm.archetype.domain.example.order.model;

import lombok.Getter;

/**
 * 订单状态枚举
 *
 * <p>订单状态流转：
 * <pre>
 * CREATED -> PAID -> SHIPPED -> COMPLETED
 *    |         |
 *    v         v
 * CANCELLED CANCELLED
 * </pre>
 * @author Leonardo
 * @since 2025/12/30
 */
@Getter
public enum OrderStatus {

    /**
     * 已创建
     */
    CREATED("已创建"),

    /**
     * 已支付
     */
    PAID("已支付"),

    /**
     * 已发货
     */
    SHIPPED("已发货"),

    /**
     * 已完成
     */
    COMPLETED("已完成"),

    /**
     * 已取消
     */
    CANCELLED("已取消");

    private final String description;

    OrderStatus(String description) {
        this.description = description;
    }

    /**
     * 检查是否可以取消
     * @return 如果可以取消返回true
     */
    public boolean canCancel() {
        return this != CREATED && this != PAID;
    }

    /**
     * 检查是否可以支付
     * @return 如果可以支付返回true
     */
    public boolean canPay() {
        return this == CREATED;
    }

    /**
     * 检查是否可以发货
     * @return 如果可以发货返回true
     */
    public boolean canShip() {
        return this == PAID;
    }

}

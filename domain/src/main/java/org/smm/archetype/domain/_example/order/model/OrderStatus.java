package org.smm.archetype.domain._example.order.model;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static final Logger log = LoggerFactory.getLogger(OrderStatus.class);

    /**
     * 从字符串转换为订单状态
     *
     * <p>如果转换失败，返回默认状态 CREATED
     * @param value 状态字符串
     * @return 订单状态枚举
     */
    public static OrderStatus fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            log.warn("Empty OrderStatus value, using default: CREATED");
            return CREATED;
        }

        try {
            return OrderStatus.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid OrderStatus: {}, using default: CREATED", value);
            return CREATED;
        }
    }

}

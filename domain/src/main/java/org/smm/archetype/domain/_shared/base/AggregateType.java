package org.smm.archetype.domain._shared.base;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 聚合根类型枚举
 *
 * <p>定义系统中所有聚合根的类型，用于事件溯源和领域事件处理。
 * @author Leonardo
 * @since 2026/01/09
 */
@Getter
public enum AggregateType {

    /**
     * 订单聚合根
     */
    ORDER("Order", "订单"),

    /**
     * 用户聚合根
     */
    USER("User", "用户"),

    /**
     * 产品聚合根
     */
    PRODUCT("Product", "产品"),

    /**
     * 支付聚合根
     */
    PAYMENT("Payment", "支付"),

    /**
     * 未知类型
     */
    UNKNOWN("Unknown", "未知");

    private static final Logger log = LoggerFactory.getLogger(AggregateType.class);
    private final String code;
    private final String description;

    AggregateType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 从类名转换为聚合根类型
     *
     * <p>如果转换失败，返回 UNKNOWN
     * @param className 类名
     * @return 聚合根类型枚举
     */
    public static AggregateType fromClassName(String className) {
        if (className == null || className.trim().isEmpty()) {
            log.warn("Empty className, using default: UNKNOWN");
            return UNKNOWN;
        }

        // 简单类名（不含包名）
        String simpleName = className.contains(".")
                                    ? className.substring(className.lastIndexOf('.') + 1)
                                    : className;

        try {
            return AggregateType.valueOf(simpleName.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Unknown AggregateType: {}, using default: UNKNOWN", className);
            return UNKNOWN;
        }
    }

    /**
     * 从字符串转换为聚合根类型
     *
     * <p>如果转换失败，返回 UNKNOWN
     * @param value 类型字符串
     * @return 聚合根类型枚举
     */
    public static AggregateType fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            log.warn("Empty AggregateType value, using default: UNKNOWN");
            return UNKNOWN;
        }

        try {
            return AggregateType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid AggregateType: {}, using default: UNKNOWN", value);
            return UNKNOWN;
        }
    }

    /**
     * 获取类名
     * @return 类名
     */
    public String getClassName() {
        return this.code;
    }
}

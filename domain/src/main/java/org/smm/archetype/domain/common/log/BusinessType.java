package org.smm.archetype.domain.common.log;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 业务类型枚举
 *
 * <p>定义系统中需要记录日志的业务类型。
 * @author Leonardo
 * @since 2026/01/09
 */
@Getter
@AllArgsConstructor
public enum BusinessType {

    /**
     * 订单创建
     */
    ORDER_CREATE("ORDER_CREATE", "订单创建"),

    /**
     * 订单更新
     */
    ORDER_UPDATE("ORDER_UPDATE", "订单更新"),

    /**
     * 订单取消
     */
    ORDER_CANCEL("ORDER_CANCEL", "订单取消"),

    /**
     * 支付处理
     */
    PAYMENT_PROCESS("PAYMENT_PROCESS", "支付处理"),

    /**
     * 文件上传
     */
    FILE_UPLOAD("FILE_UPLOAD", "文件上传"),

    /**
     * 文件下载
     */
    FILE_DOWNLOAD("FILE_DOWNLOAD", "文件下载"),

    /**
     * 用户登录
     */
    USER_LOGIN("USER_LOGIN", "用户登录"),

    /**
     * 用户注册
     */
    USER_REGISTER("USER_REGISTER", "用户注册"),

    /**
     * 事件发布
     */
    EVENT_PUBLISH("EVENT_PUBLISH", "事件发布"),

    /**
     * 事件消费
     */
    EVENT_CONSUME("EVENT_CONSUME", "事件消费"),

    /**
     * 系统操作
     */
    SYSTEM_OPERATION("SYSTEM_OPERATION", "系统操作"),

    /**
     * 未知类型
     */
    UNKNOWN("UNKNOWN", "未知类型");

    /**
     * 类型代码
     */
    private final String code;

    /**
     * 类型描述
     */
    private final String description;

    /**
     * 根据代码获取业务类型
     * @param code 类型代码
     * @return 业务类型枚举
     */
    public static BusinessType fromCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return UNKNOWN;
        }

        for (BusinessType type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }

        return UNKNOWN;
    }

    /**
     * 验证类型代码是否有效
     * @param code 类型代码
     * @return 是否有效
     */
    public static boolean isValid(String code) {
        return fromCode(code) != UNKNOWN;
    }

}

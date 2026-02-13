package org.smm.archetype.adapter.shared.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 错误码枚举，定义系统标准错误码。
 */
@Getter
@AllArgsConstructor
public enum ResultEnum {

    SUCCESS(1000, "请求成功"),
    UNKNOWN_ERROR(5000, "未知异常"),
    SYSTEM_ERROR(5001, "系统异常"),
    BUSINESS_ERROR(5002, "业务异常"),
    ;

    /**
     * HTTP状态码
     */
    private final Integer code;

    /**
     * 错误描述
     */
    private final String desc;

}

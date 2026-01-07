package org.smm.archetype.adapter._shared.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 错误码枚举类
 * 定义系统中使用的标准错误码，用于Web层统一响应处理
 */
@Getter
@AllArgsConstructor
public enum ResultEnum {

    SUCCESS(200, "请求成功"),
    UNKNOWN_ERROR(500, "未知异常"),
    SYSTEM_ERROR(500, "系统异常"),
    BUSINESS_ERROR(400, "业务异常"),
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

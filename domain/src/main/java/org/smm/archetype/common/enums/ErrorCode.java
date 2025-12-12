package org.smm.archetype.common.enums;

import com.alibaba.cola.exception.BizException;
import com.alibaba.cola.exception.SysException;
import lombok.Getter;

/**
 * 错误码枚举类
 *
 * 定义系统中使用的标准错误码，包括未知错误、业务错误和系统错误。
 * 每个错误码关联特定的异常类型，便于统一异常处理。
 */
@Getter
public enum ErrorCode {

    UNKNOWN_ERROR(Exception.class),
    BIZ_ERROR(BizException.class),
    SYS_ERROR(SysException.class),
    ;

    /**
     * 异常类型
     *
     * 关联具体的异常类，用于在异常处理时创建对应类型的异常实例。
     */
    private final Class<? extends Exception> exceptionClass;

    /**
     * 构造函数
     *
     * 创建错误码枚举实例，关联指定的异常类型。
     * @param exceptionClass 异常类型Class对象
     */
    ErrorCode(Class<? extends Exception> exceptionClass) {
        this.exceptionClass = exceptionClass;
    }

}

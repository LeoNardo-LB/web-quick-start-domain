package org.smm.archetype.common.enums;

import com.alibaba.cola.exception.BizException;
import com.alibaba.cola.exception.SysException;
import lombok.Getter;

/**
 *
 *
 * @author Leonardo
 * @since 2025/12/13
 */
@Getter
public enum ErrorCode {

    BIZ_ERROR(BizException.class),
    SYS_ERROR(SysException.class),
    UNKNOWN_ERROR(Exception.class),
    ;

    /**
     * 异常类型
     */
    private final Class<? extends Exception> exceptionClass;

    ErrorCode(Class<? extends Exception> exceptionClass) {
        this.exceptionClass = exceptionClass;
    }

}

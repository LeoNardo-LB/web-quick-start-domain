package org.smm.archetype.adapter.access.web.config;

import lombok.extern.slf4j.Slf4j;
import org.smm.archetype.adapter._shared.enums.ResultEnum;
import org.smm.archetype.adapter._shared.result.BaseResult;
import org.smm.archetype.domain._shared.exception.BizException;
import org.smm.archetype.domain._shared.exception.SysException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Web全局异常处理器
 *
 * 用于统一处理Web层抛出的各类异常，包括系统异常、业务异常和未知异常，
 * 并将异常信息转换为统一的响应格式返回给客户端。
 */
@Slf4j
@RestControllerAdvice
public class WebExceptionAdvise {

    /**
     * 处理系统异常
     *
     * 拦截并处理SysException类型的异常，记录错误日志并将异常信息转换为失败响应返回。
     * @param e 系统异常对象
     * @return 包含异常信息的失败响应
     */
    @ExceptionHandler(SysException.class)
    public BaseResult<Void> handleSysException(SysException e) {
        log.error("系统异常", e);
        return BaseResult.<Void>builder()
                       .setCode(ResultEnum.SYSTEM_ERROR.getCode())
                       .setMessage(e.getMessage())
                       .build();
    }

    /**
     * 处理业务异常
     *
     * 拦截并处理BizException类型的异常，记录错误日志并将异常信息转换为失败响应返回。
     * @param e 业务异常对象
     * @return 包含异常信息的失败响应
     */
    @ExceptionHandler(BizException.class)
    public BaseResult<Void> handleBizException(BizException e) {
        log.error("业务异常", e);
        return BaseResult.<Void>builder()
                       .setCode(ResultEnum.BUSINESS_ERROR.getCode())
                       .setMessage(e.getMessage())
                       .build();
    }

    /**
     * 处理未知异常
     *
     * 拦截并处理所有未被其他异常处理器处理的异常，记录错误日志并将异常信息转换为失败响应返回。
     * @param e 异常对象
     * @return 包含异常信息的失败响应
     */
    @ExceptionHandler(Exception.class)
    public BaseResult<Void> handleException(Exception e) {
        log.error("未知异常", e);
        return BaseResult.<Void>builder()
                       .setCode(ResultEnum.UNKNOWN_ERROR.getCode())
                       .setMessage(e.getMessage())
                       .build();
    }

}

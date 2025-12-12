package org.smm.archetype.web.config;

import com.alibaba.cola.dto.Response;
import com.alibaba.cola.exception.BizException;
import com.alibaba.cola.exception.SysException;
import lombok.extern.slf4j.Slf4j;
import org.smm.archetype.common.enums.ErrorCode;
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
    public Response handleSysException(SysException e) {
        log.error("系统异常", e);
        return Response.buildFailure(e.getErrCode(), e.getMessage());
    }

    /**
     * 处理业务异常
     *
     * 拦截并处理BizException类型的异常，记录错误日志并将异常信息转换为失败响应返回。
     * @param e 业务异常对象
     * @return 包含异常信息的失败响应
     */
    @ExceptionHandler(BizException.class)
    public Response handleBizException(BizException e) {
        log.error("业务异常", e);
        return Response.buildFailure(e.getErrCode(), e.getMessage());
    }

    /**
     * 处理未知异常
     *
     * 拦截并处理所有未被其他异常处理器处理的异常，记录错误日志并将异常信息转换为失败响应返回。
     * @param e 异常对象
     * @return 包含异常信息的失败响应
     */
    @ExceptionHandler(Exception.class)
    public Response handleException(Exception e) {
        log.error("未知异常", e);
        return Response.buildFailure(ErrorCode.UNKNOWN_ERROR.name(), e.getMessage());
    }

}

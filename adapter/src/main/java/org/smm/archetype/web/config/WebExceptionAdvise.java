package org.smm.archetype.web.config;

import com.alibaba.cola.dto.Response;
import com.alibaba.cola.exception.BizException;
import com.alibaba.cola.exception.SysException;
import org.smm.archetype.common.enums.ErrorCode;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @author Leonardo
 * @since 2025/7/14
 * 全局异常处理器
 */
@RestControllerAdvice
public class WebExceptionAdvise {

    @ExceptionHandler(SysException.class)
    public Response handleSysException(SysException e) {
        return Response.buildFailure(e.getErrCode(), e.getMessage());
    }

    @ExceptionHandler(BizException.class)
    public Response handleBizException(BizException e) {
        return Response.buildFailure(e.getErrCode(), e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public Response handleException(Exception e) {
        return Response.buildFailure(ErrorCode.UNKNOWN_ERROR.name(), e.getMessage());
    }

}

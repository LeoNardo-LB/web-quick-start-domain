package org.smm.archetype.adapter.web.config;

import lombok.extern.slf4j.Slf4j;
import org.smm.archetype.adapter.bizshared.enums.ResultEnum;
import org.smm.archetype.app.bizshared.result.BaseResult;
import org.smm.archetype.domain.bizshared.exception.BizException;
import org.smm.archetype.domain.bizshared.exception.SysException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Web全局异常处理器，统一处理Web层异常并转换为标准响应。
 */
@Slf4j
@RestControllerAdvice
public class WebExceptionAdvise {

    /**
     * 处理系统异常。
     * @param e 系统异常对象
     * @return 失败响应
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
     * 处理业务异常。
     * @param e 业务异常对象
     * @return 失败响应
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
     * 处理未知异常。
     * @param e 异常对象
     * @return 失败响应
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

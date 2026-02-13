package org.smm.archetype.adapter.web.config;

import lombok.extern.slf4j.Slf4j;
import org.smm.archetype.adapter.shared.enums.ResultEnum;
import org.smm.archetype.app.shared.result.BaseResult;
import org.smm.archetype.domain.shared.exception.BaseException;
import org.smm.archetype.domain.shared.exception.BizException;
import org.smm.archetype.domain.shared.exception.ClientException;
import org.smm.archetype.domain.shared.exception.SysException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Web全局异常处理器，统一处理Web层异常并转换为标准响应。
 */
@Slf4j
@RestControllerAdvice
public class WebExceptionAdvise {

    /**
     * 处理客户端异常。
     * @param e 客户端异常对象
     * @return 失败响应
     */
    @ExceptionHandler(ClientException.class)
    public BaseResult<Void> handleClientException(ClientException e) {
        log.error("客户端异常: {}", e.getMessage(), e);
        String errorCode = e.getErrorCode() != null 
                ? e.getErrorCode().getCode() 
                : String.valueOf(ResultEnum.SYSTEM_ERROR.getCode());
        return BaseResult.<Void>builder()
                       .setCode(errorCode)
                       .setMessage(e.getMessage())
                       .build();
    }

    /**
     * 处理系统异常。
     * @param e 系统异常对象
     * @return 失败响应
     */
    @ExceptionHandler(SysException.class)
    public BaseResult<Void> handleSysException(SysException e) {
        log.error("系统异常: {}", e.getMessage(), e);
        String errorCode = e.getErrorCode() != null 
                ? e.getErrorCode().getCode() 
                : String.valueOf(ResultEnum.SYSTEM_ERROR.getCode());
        return BaseResult.<Void>builder()
                       .setCode(errorCode)
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
        log.warn("业务异常: {}", e.getMessage());
        String errorCode = e.getErrorCode() != null 
                ? e.getErrorCode().getCode() 
                : String.valueOf(ResultEnum.BUSINESS_ERROR.getCode());
        return BaseResult.<Void>builder()
                       .setCode(errorCode)
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
                       .setCode(String.valueOf(ResultEnum.UNKNOWN_ERROR.getCode()))
                       .setMessage(e.getMessage())
                       .build();
    }

}

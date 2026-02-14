package org.smm.archetype.adapter.web.config;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.smm.archetype.adapter.shared.enums.ResultEnum;
import org.smm.archetype.app.shared.result.BaseResult;
import org.smm.archetype.domain.shared.exception.BizException;
import org.smm.archetype.domain.shared.exception.ClientException;
import org.smm.archetype.domain.shared.exception.SysException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * Web全局异常处理器，统一处理Web层异常并转换为标准响应。
 */
@Slf4j
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class WebExceptionAdvise {

    /**
     * 处理参数校验异常（@RequestBody 校验失败）。
     * @param e 校验异常对象
     * @return 失败响应（HTTP 400）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseResult<Void>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                                 .map(error -> error.getField() + ": " + error.getDefaultMessage())
                                 .collect(Collectors.joining(", "));
        log.warn("参数校验失败: {}", message);
        BaseResult<Void> result = BaseResult.<Void>builder()
                                          .setCode("400")
                                          .setMessage("参数校验失败: " + message)
                                          .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
    }

    /**
     * 处理约束违反异常（@RequestParam 校验失败）。
     * @param e 约束违反异常对象
     * @return 失败响应（HTTP 400）
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<BaseResult<Void>> handleConstraintViolationException(ConstraintViolationException e) {
        String message = e.getConstraintViolations().stream()
                                 .map(ConstraintViolation::getMessage)
                                 .collect(Collectors.joining(", "));
        log.warn("约束违反: {}", message);
        BaseResult<Void> result = BaseResult.<Void>builder()
                                          .setCode("400")
                                          .setMessage("参数校验失败: " + message)
                                          .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
    }

    /**
     * 处理绑定异常（表单数据校验失败）。
     * @param e 绑定异常对象
     * @return 失败响应（HTTP 400）
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<BaseResult<Void>> handleBindException(BindException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                                 .map(error -> error.getField() + ": " + error.getDefaultMessage())
                                 .collect(Collectors.joining(", "));
        log.warn("绑定异常: {}", message);
        BaseResult<Void> result = BaseResult.<Void>builder()
                                          .setCode("400")
                                          .setMessage("参数校验失败: " + message)
                                          .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
    }

    /**
     * 处理客户端异常。
     * @param e 客户端异常对象
     * @return 失败响应
     */
    @ExceptionHandler(ClientException.class)
    public BaseResult<Void> handleClientException(ClientException e) {
        log.error("客户端异常", e);
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
        log.error("系统异常", e);
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
    public ResponseEntity<BaseResult<Void>> handleException(Exception e) {
        // 首先打印异常类型
        log.error("捕获异常, 类型={}", e.getClass().getName());

        // 检查是否是验证异常
        if (e instanceof MethodArgumentNotValidException manve) {
            String message = manve.getBindingResult().getFieldErrors().stream()
                                     .map(error -> error.getField() + ": " + error.getDefaultMessage())
                                     .collect(Collectors.joining(", "));
            log.warn("参数校验失败: {}", message);
            BaseResult<Void> result = BaseResult.<Void>builder()
                                              .setCode("400")
                                              .setMessage("参数校验失败: " + message)
                                              .build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
        }

        if (e instanceof BindException be) {
            String message = be.getBindingResult().getFieldErrors().stream()
                                     .map(error -> error.getField() + ": " + error.getDefaultMessage())
                                     .collect(Collectors.joining(", "));
            log.warn("绑定异常: {}", message);
            BaseResult<Void> result = BaseResult.<Void>builder()
                                              .setCode("400")
                                              .setMessage("参数校验失败: " + message)
                                              .build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
        }

        log.error("未知异常, 类型={}, 消息={}", e.getClass().getName(), e.getMessage());
        BaseResult<Void> result = BaseResult.<Void>builder()
                       .setCode(String.valueOf(ResultEnum.UNKNOWN_ERROR.getCode()))
                       .setMessage(e.getMessage())
                       .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
    }

}

package org.smm.archetype.domain.bizshared.exception;

/**
 * 基础异常类，提供通用异常构造方法。
 *
 * 支持基于异常Code的错误处理机制，用于统一异常处理和错误追踪。
 */
public abstract class BaseException extends RuntimeException {

    /**
     * 异常错误码，用于统一异常处理和错误追踪
     */
    private final String errorCode;

    public BaseException() {
        super();
        this.errorCode = null;
    }

    public BaseException(String message) {
        super(message);
        this.errorCode = null;
    }

    public BaseException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = null;
    }

    public BaseException(Throwable cause) {
        super(cause);
        this.errorCode = null;
    }

    protected BaseException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.errorCode = null;
    }

    /**
     * 带错误码的构造方法
     *
     * @param message 异常消息
     * @param errorCode 错误码
     */
    public BaseException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * 带错误码和原因的构造方法
     *
     * @param message 异常消息
     * @param cause 异常原因
     * @param errorCode 错误码
     */
    public BaseException(String message, Throwable cause, String errorCode) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    /**
     * 获取异常错误码
     *
     * @return 错误码，如果未设置则为null
     */
    public String getErrorCode() {
        return errorCode;
    }

}

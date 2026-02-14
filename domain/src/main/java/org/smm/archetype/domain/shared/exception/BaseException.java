package org.smm.archetype.domain.shared.exception;

/**
 * 基础异常类，提供通用异常构造方法。
 *
 * <p>支持基于ErrorCode的错误处理机制，用于统一异常处理和错误追踪。
 * 使用ErrorCode接口而非String，提供类型安全的错误码管理。</p>
 */
public abstract class BaseException extends RuntimeException {

    /**
     * 异常错误码，用于统一异常处理和错误追踪
     */
    private final ErrorCode errorCode;

    /**
     * 默认构造方法
     */
    public BaseException() {
        super();
        this.errorCode = null;
    }

    /**
     * 带消息的构造方法
     * @param message 异常消息
     */
    public BaseException(String message) {
        super(message);
        this.errorCode = null;
    }

    /**
     * 带消息和原因的构造方法
     * @param message 异常消息
     * @param cause   异常原因
     */
    public BaseException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = null;
    }

    /**
     * 带原因的构造方法
     * @param cause 异常原因
     */
    public BaseException(Throwable cause) {
        super(cause);
        this.errorCode = null;
    }

    /**
     * 完整参数的构造方法
     * @param message            异常消息
     * @param cause              异常原因
     * @param enableSuppression  是否启用抑制
     * @param writableStackTrace 是否可写堆栈跟踪
     */
    protected BaseException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.errorCode = null;
    }

    /**
     * 带错误码的构造方法
     *
     * @param errorCode 错误码枚举
     */
    public BaseException(ErrorCode errorCode) {
        super(errorCode != null ? errorCode.getMessage() : null);
        this.errorCode = errorCode;
    }

    /**
     * 带错误码和原因的构造方法
     *
     * @param errorCode 错误码枚举
     * @param cause     异常原因
     */
    public BaseException(ErrorCode errorCode, Throwable cause) {
        super(errorCode != null ? errorCode.getMessage() : null, cause);
        this.errorCode = errorCode;
    }

    /**
     * 带自定义消息和错误码的构造方法
     *
     * @param message   自定义异常消息
     * @param errorCode 错误码枚举
     */
    public BaseException(String message, ErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * 带自定义消息、错误码和原因的构造方法
     *
     * @param message   自定义异常消息
     * @param cause     异常原因
     * @param errorCode 错误码枚举
     */
    public BaseException(String message, Throwable cause, ErrorCode errorCode) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    /**
     * 获取异常错误码
     *
     * @return 错误码枚举，如果未设置则为null
     */
    public ErrorCode getErrorCode() {
        return errorCode;
    }

    /**
     * 获取错误码字符串（兼容旧API）
     *
     * @return 错误码字符串，如果未设置则为null
     * @deprecated 使用 {@link #getErrorCode()} 替代
     */
    @Deprecated
    public String getErrorCodeString() {
        return errorCode != null ? errorCode.getCode() : null;
    }

}

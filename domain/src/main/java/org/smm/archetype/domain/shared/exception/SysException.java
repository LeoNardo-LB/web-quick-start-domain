package org.smm.archetype.domain.shared.exception;

/**
 * 系统异常类，表示非预期的系统错误。
 *
 * <p>使用 ErrorCode 接口提供类型安全的错误码管理。</p>
 */
public class SysException extends BaseException {

    public SysException() {
        super();
    }

    public SysException(String message) {
        super(message);
    }

    public SysException(String message, Throwable cause) {
        super(message, cause);
    }

    public SysException(Throwable cause) {
        super(cause);
    }

    protected SysException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    /**
     * 带错误码的构造方法
     *
     * @param errorCode 错误码枚举
     */
    public SysException(ErrorCode errorCode) {
        super(errorCode);
    }

    /**
     * 带错误码和原因的构造方法
     *
     * @param errorCode 错误码枚举
     * @param cause     异常原因
     */
    public SysException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }

    /**
     * 带自定义消息和错误码的构造方法
     *
     * @param message   自定义异常消息
     * @param errorCode 错误码枚举
     */
    public SysException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }

    /**
     * 带自定义消息、错误码和原因的构造方法
     *
     * @param message   自定义异常消息
     * @param cause     异常原因
     * @param errorCode 错误码枚举
     */
    public SysException(String message, Throwable cause, ErrorCode errorCode) {
        super(message, cause, errorCode);
    }

}

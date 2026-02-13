package org.smm.archetype.domain.shared.exception;

/**
 * 业务异常类，表示可预期的业务错误。
 *
 * <p>使用 ErrorCode 接口提供类型安全的错误码管理。</p>
 */
public class BizException extends BaseException {

    public BizException() {
        super();
    }

    public BizException(String message) {
        super(message);
    }

    public BizException(String message, Throwable cause) {
        super(message, cause);
    }

    public BizException(Throwable cause) {
        super(cause);
    }

    protected BizException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    /**
     * 带错误码的构造方法
     *
     * @param errorCode 错误码枚举
     */
    public BizException(ErrorCode errorCode) {
        super(errorCode);
    }

    /**
     * 带错误码和原因的构造方法
     *
     * @param errorCode 错误码枚举
     * @param cause     异常原因
     */
    public BizException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }

    /**
     * 带自定义消息和错误码的构造方法
     *
     * @param message   自定义异常消息
     * @param errorCode 错误码枚举
     */
    public BizException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }

}

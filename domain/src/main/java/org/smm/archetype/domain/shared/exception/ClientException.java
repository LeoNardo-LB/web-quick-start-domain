package org.smm.archetype.domain.shared.exception;

/**
 * 客户端异常类，用于表示客户端调用失败或错误。
 *
 * <p>支持基于ErrorCode的错误处理机制，用于统一异常处理和错误追踪。</p>
 */
public class ClientException extends BaseException {

    /**
     * 构造方法
     *
     * @param errorCode 错误码枚举
     */
    public ClientException(ErrorCode errorCode) {
        super(errorCode);
    }

    /**
     * 构造方法
     *
     * @param errorCode 错误码枚举
     * @param cause     异常原因
     */
    public ClientException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }

    /**
     * 构造方法（自定义消息）
     *
     * @param message   自定义异常消息
     * @param errorCode 错误码枚举
     */
    public ClientException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }

    /**
     * 构造方法（自定义消息和原因）
     *
     * @param message   自定义异常消息
     * @param cause     异常原因
     * @param errorCode 错误码枚举
     */
    public ClientException(String message, Throwable cause, ErrorCode errorCode) {
        super(message, cause, errorCode);
    }

}

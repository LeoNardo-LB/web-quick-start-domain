package org.smm.archetype.domain.bizshared.exception;

/**
 * 客户端异常类，用于表示客户端调用失败或错误。
 *
 * 支持基于异常Code的错误处理机制，用于统一异常处理和错误追踪。
 */
public class ClientException extends BaseException {

    /**
     * 构造方法
     *
     * @param message 异常消息
     * @param errorCode 错误码
     */
    public ClientException(String message, String errorCode) {
        super(message, errorCode);
    }

    /**
     * 构造方法
     *
     * @param message 异常消息
     * @param cause 异常原因
     * @param errorCode 错误码
     */
    public ClientException(String message, Throwable cause, String errorCode) {
        super(message, cause, errorCode);
    }
}

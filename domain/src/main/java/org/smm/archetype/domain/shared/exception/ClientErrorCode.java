package org.smm.archetype.domain.shared.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 客户端错误码枚举，定义所有客户端相关的错误码。
 *
 * <p>包括通用错误、SMS错误、Email错误、OSS错误、Cache错误和Search错误。</p>
 *
 * <p>错误码命名规范：CLIENT-xxx</p>
 */
@Getter
@RequiredArgsConstructor
public enum ClientErrorCode implements ErrorCode {

    // ========== 通用错误 ==========

    /**
     * 客户端超时
     */
    TIMEOUT("CLIENT-TIMEOUT", "客户端请求超时"),

    /**
     * 网络错误
     */
    NETWORK_ERROR("CLIENT-NETWORK", "网络连接错误"),

    /**
     * 无效参数
     */
    INVALID_PARAMS("CLIENT-PARAMS", "客户端参数无效"),

    /**
     * 操作失败
     */
    OPERATION_FAILED("CLIENT-OPERATION", "客户端操作失败"),

    // ========== SMS特定错误 ==========

    /**
     * SMS超时
     */
    SMS_TIMEOUT("SMS-TIMEOUT", "短信发送超时"),

    /**
     * 无效的手机号
     */
    SMS_INVALID_PHONE("SMS-PHONE", "无效的手机号码"),

    // ========== Email特定错误 ==========

    /**
     * 邮件发送失败
     */
    EMAIL_SEND_FAILED("EMAIL-SEND", "邮件发送失败"),

    // ========== OSS特定错误 ==========

    /**
     * OSS文件未找到
     */
    OSS_FILE_NOT_FOUND("OSS-NOTFOUND", "文件不存在"),

    // ========== Cache特定错误 ==========

    /**
     * 缓存操作失败
     */
    CACHE_OPERATION_FAILED("CACHE-OPERATION", "缓存操作失败"),

    // ========== Search特定错误 ==========

    /**
     * 搜索索引失败
     */
    SEARCH_INDEX_FAILED("SEARCH-INDEX", "搜索索引操作失败"),

    ;

    private final String code;
    private final String message;

}

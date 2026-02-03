package org.smm.archetype.domain.bizshared.exception;

/**
 * 客户端错误码接口，定义所有客户端相关的错误码常量。
 *
 * 包括通用错误、SMS错误、Email错误、OSS错误、Cache错误和Search错误。
 */
public interface ClientErrorCode {

    // ========== 通用错误 ==========

    /**
     * 客户端超时
     */
    String TIMEOUT = "CLIENT_TIMEOUT";

    /**
     * 网络错误
     */
    String NETWORK_ERROR = "CLIENT_NETWORK_ERROR";

    /**
     * 无效参数
     */
    String INVALID_PARAMS = "CLIENT_INVALID_PARAMS";

    /**
     * 操作失败
     */
    String OPERATION_FAILED = "CLIENT_OPERATION_FAILED";


    // ========== SMS特定错误 ==========

    /**
     * SMS超时
     */
    String SMS_TIMEOUT = "SMS_TIMEOUT";

    /**
     * 无效的手机号
     */
    String SMS_INVALID_PHONE = "SMS_INVALID_PHONE";


    // ========== Email特定错误 ==========

    /**
     * 邮件发送失败
     */
    String EMAIL_SEND_FAILED = "EMAIL_SEND_FAILED";


    // ========== OSS特定错误 ==========

    /**
     * OSS文件未找到
     */
    String OSS_FILE_NOT_FOUND = "OSS_FILE_NOT_FOUND";


    // ========== Cache特定错误 ==========

    /**
     * 缓存操作失败
     */
    String CACHE_OPERATION_FAILED = "CACHE_OPERATION_FAILED";


    // ========== Search特定错误 ==========

    /**
     * 搜索索引失败
     */
    String SEARCH_INDEX_FAILED = "SEARCH_INDEX_FAILED";

}

package org.smm.archetype.domain.shared.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 通用错误码枚举
 *
 * <p>定义系统中通用的错误码，用于基础异常处理。
 * 业务模块应定义自己的错误码枚举（如 OrderErrorCode、UserErrorCode）。</p>
 *
 * <p>错误码命名规范：</p>
 * <ul>
 *   <li>格式：{模块}-{类型}-{序号}</li>
 *   <li>示例：SYS-001, BIZ-001, CLIENT-001</li>
 * </ul>
 */
@Getter
@RequiredArgsConstructor
public enum CommonErrorCode implements ErrorCode {

    // ========== 系统级错误 (SYS-xxx) ==========

    /**
     * 系统内部错误
     */
    SYSTEM_ERROR("SYS-001", "系统内部错误"),

    /**
     * 服务不可用
     */
    SERVICE_UNAVAILABLE("SYS-002", "服务暂时不可用"),

    /**
     * 参数校验失败
     */
    INVALID_PARAMETER("SYS-003", "参数校验失败"),

    /**
     * 资源未找到
     */
    RESOURCE_NOT_FOUND("SYS-004", "请求的资源不存在"),

    // ========== 业务级错误 (BIZ-xxx) ==========

    /**
     * 业务规则校验失败
     */
    BUSINESS_RULE_VIOLATION("BIZ-001", "业务规则校验失败"),

    /**
     * 数据已存在
     */
    DATA_ALREADY_EXISTS("BIZ-002", "数据已存在"),

    /**
     * 数据不存在
     */
    DATA_NOT_FOUND("BIZ-003", "数据不存在"),

    /**
     * 操作不允许
     */
    OPERATION_NOT_ALLOWED("BIZ-004", "当前状态不允许此操作"),

    // ========== 客户端错误 (CLIENT-xxx) ==========

    /**
     * 客户端超时
     */
    CLIENT_TIMEOUT("CLIENT-001", "客户端请求超时"),

    /**
     * 网络错误
     */
    CLIENT_NETWORK_ERROR("CLIENT-002", "网络连接错误"),

    /**
     * 客户端参数无效
     */
    CLIENT_INVALID_PARAMS("CLIENT-003", "客户端参数无效"),

    /**
     * 客户端操作失败
     */
    CLIENT_OPERATION_FAILED("CLIENT-004", "客户端操作失败"),

    ;

    private final String code;
    private final String message;

}

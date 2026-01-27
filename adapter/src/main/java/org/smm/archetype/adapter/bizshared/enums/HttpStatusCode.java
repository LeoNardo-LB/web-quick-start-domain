package org.smm.archetype.adapter.bizshared.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * HTTP状态码枚举
 *
 * <p>统一管理HTTP状态码，避免硬编码。
 *
 * <p>使用示例：
 * <pre>{@code
 * return ResponseEntity
 *     .status(HttpStatusCode.BAD_REQUEST.getHttpStatus())
 *     .body(BaseResult.error(HttpStatusCode.BAD_REQUEST.getCode(), e.getMessage()));
 * }</pre>
 * @author Leonardo
 * @since 2026/01/10
 */
@Getter
@AllArgsConstructor
public enum HttpStatusCode {

    /**
     * 成功
     */
    SUCCESS(200, "成功", HttpStatus.OK),

    /**
     * 已创建
     */
    CREATED(201, "已创建", HttpStatus.CREATED),

    /**
     * 请求错误
     */
    BAD_REQUEST(400, "请求错误", HttpStatus.BAD_REQUEST),

    /**
     * 未授权
     */
    UNAUTHORIZED(401, "未授权", HttpStatus.UNAUTHORIZED),

    /**
     * 禁止访问
     */
    FORBIDDEN(403, "禁止访问", HttpStatus.FORBIDDEN),

    /**
     * 未找到
     */
    NOT_FOUND(404, "未找到", HttpStatus.NOT_FOUND),

    /**
     * 冲突
     */
    CONFLICT(409, "冲突", HttpStatus.CONFLICT),

    /**
     * 服务器错误
     */
    INTERNAL_SERVER_ERROR(500, "服务器错误", HttpStatus.INTERNAL_SERVER_ERROR);

    /**
     * 状态码
     */
    private final int code;

    /**
     * 描述
     */
    private final String message;

    /**
     * Spring HttpStatus
     */
    private final HttpStatus httpStatus;

    /**
     * 根据code获取枚举
     * @param code 状态码
     * @return 枚举，未找到返回INTERNAL_SERVER_ERROR
     */
    public static HttpStatusCode fromCode(int code) {
        for (HttpStatusCode status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return INTERNAL_SERVER_ERROR;
    }

    /**
     * 判断是否成功
     * @return true-成功，false-失败
     */
    public boolean isSuccess() {
        return this == SUCCESS || this == CREATED;
    }

    /**
     * 判断是否客户端错误
     * @return true-客户端错误，false-非客户端错误
     */
    public boolean isClientError() {
        return this == BAD_REQUEST || this == UNAUTHORIZED || this == FORBIDDEN || this == NOT_FOUND || this == CONFLICT;
    }

    /**
     * 判断是否服务器错误
     * @return true-服务器错误，false-非服务器错误
     */
    public boolean isServerError() {
        return this == INTERNAL_SERVER_ERROR;
    }
}

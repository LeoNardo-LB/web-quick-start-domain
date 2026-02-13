package org.smm.archetype.app.shared.result;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.smm.archetype.infrastructure.shared.util.context.ScopedThreadContext;

import java.io.Serializable;
import java.time.Instant;

/**
 * 统一结果返回对象，封装错误码和数据。
 * 自动获取 traceId 用于请求追踪。
 */
@Getter
@Setter
@SuperBuilder(setterPrefix = "set")
public class BaseResult<T> implements Serializable {

    /**
     * 错误码（支持数字字符串和业务错误码格式）
     */
    protected String code;

    /**
     * 接口返回数据
     */
    protected T data;

    /**
     * 接口提示信息
     */
    protected String message;

    /**
     * 接口返回时间
     */
    protected Instant time;

    /**
     * traceId 用于请求追踪
     */
    protected String traceId;

    /**
     * 构造函数
     */
    public BaseResult() {
        this.time = Instant.now();
        // 从上下文获取 traceId
        String currentTraceId = ScopedThreadContext.getTraceId();
        this.traceId = currentTraceId != null ? currentTraceId : "N/A";
    }

    /**
     * 成功返回（无数据）
     */
    public static <T> BaseResult<T> success() {
        return BaseResult.<T>builder()
                       .setCode("200")
                       .setMessage("success")
                       .setTime(Instant.now())
                       .setTraceId(resolveTraceId())
                       .build();
    }

    /**
     * 成功返回（带数据）
     */
    public static <T> BaseResult<T> success(T data) {
        return BaseResult.<T>builder()
                       .setCode("200")
                       .setData(data)
                       .setMessage("success")
                       .setTime(Instant.now())
                       .setTraceId(resolveTraceId())
                       .build();
    }

    /**
     * 错误返回
     */
    public static <T> BaseResult<T> error(String code, String message) {
        return BaseResult.<T>builder()
                       .setCode(code)
                       .setMessage(message)
                       .setTime(Instant.now())
                       .setTraceId(resolveTraceId())
                       .build();
    }

    /**
     * 解析当前 traceId
     */
    private static String resolveTraceId() {
        String traceId = ScopedThreadContext.getTraceId();
        return traceId != null ? traceId : "N/A";
    }

}

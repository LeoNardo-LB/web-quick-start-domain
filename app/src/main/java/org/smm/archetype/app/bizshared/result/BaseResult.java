package org.smm.archetype.app.bizshared.result;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.time.Instant;

/**
 * 统一结果返回对象，封装错误码和数据。
 */
@Getter
@Setter
@SuperBuilder(setterPrefix = "set")
public class BaseResult<T> implements Serializable {

    /**
     * 错误码
     */
    protected int code;

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
     * traceId
     */
    protected String traceId;

    /**
     * 构造函数
     */
    public BaseResult() {
        this.time = Instant.now();
        // TODO 获取 traceId
        this.traceId = "TODO";
    }

    /**
     * 成功返回（无数据）
     */
    public static <T> BaseResult<T> success() {
        return BaseResult.<T>builder()
                       .setCode(200)
                       .setMessage("success")
                       .setTime(Instant.now())
                       .build();
    }

    /**
     * 成功返回（带数据）
     */
    public static <T> BaseResult<T> success(T data) {
        return BaseResult.<T>builder()
                       .setCode(200)
                       .setData(data)
                       .setMessage("success")
                       .setTime(Instant.now())
                       .build();
    }

    /**
     * 错误返回
     */
    public static <T> BaseResult<T> error(int code, String message) {
        return BaseResult.<T>builder()
                       .setCode(code)
                       .setMessage(message)
                       .setTime(Instant.now())
                       .build();
    }

}

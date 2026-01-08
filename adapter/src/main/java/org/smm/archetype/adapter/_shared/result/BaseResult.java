package org.smm.archetype.adapter._shared.result;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 统一结果返回
 * @author Leonardo
 * @since 2026/1/6
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
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
    protected LocalDateTime time;

    /**
     * traceId
     */
    protected String traceId;

    /**
     * 成功返回（无数据）
     */
    public static <T> BaseResult<T> success() {
        return BaseResult.<T>builder()
                       .setCode(200)
                       .setMessage("success")
                       .setTime(LocalDateTime.now())
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
                       .setTime(LocalDateTime.now())
                       .build();
    }

    /**
     * 错误返回
     */
    public static <T> BaseResult<T> error(int code, String message) {
        return BaseResult.<T>builder()
                       .setCode(code)
                       .setMessage(message)
                       .setTime(LocalDateTime.now())
                       .build();
    }

}

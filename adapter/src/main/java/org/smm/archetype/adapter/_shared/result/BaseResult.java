package org.smm.archetype.adapter._shared.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 统一结果返回
 * @author Leonardo
 * @since 2026/1/6
 */
@Data
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

}

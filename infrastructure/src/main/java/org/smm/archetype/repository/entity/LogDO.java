package org.smm.archetype.repository.entity;

import com.mybatisflex.annotation.Table;
import java.io.Serializable;
import java.time.Instant;

import org.smm.archetype.repository.listener.BaseDOFillListener;

import java.io.Serial;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;

/**
 * 日志表 实体类。
 *
 * @author Administrator
 * @since 2025-12-30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Table(value = "log", onInsert = BaseDOFillListener.class, onUpdate = BaseDOFillListener.class)
public class LogDO extends BaseDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 业务类型
     */
    private String biz;

    /**
     * 方法名
     */
    private String method;

    /**
     * 参数
     */
    private String argString;

    /**
     * 结果
     */
    private String resultString;

    /**
     * 线程名
     */
    private String threadName;

    /**
     * 异常信息
     */
    private String exception;

    /**
     * 开始时间
     */
    private Instant startTime;

    /**
     * 结束时间
     */
    private Instant endTime;

}

package org.smm.archetype.infrastructure.shared.log;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.aspectj.lang.reflect.MethodSignature;
import org.smm.archetype.domain.shared.base.Entity;

import java.time.Instant;

/**
 * 方法执行日志数据传输对象，封装方法执行信息。
 */
@Getter
@SuperBuilder(setterPrefix = "set")
public class MethodExecutionLog extends Entity {

    /**
     * 业务日志注解配置信息
     */
    private BusinessLog businessLog;

    /**
     * 方法签名信息
     */
    private MethodSignature signature;

    /**
     * 方法参数数组
     */
    private Object[] args;

    /**
     * 方法返回值
     */
    private Object result;

    /**
     * 线程名称
     */
    private String threadName;

    /**
     * 异常信息
     */
    private Throwable error;

    /**
     * 方法开始执行时间
     */
    private Instant startTime;

    /**
     * 方法执行结束时间
     * 记录方法执行结束的时间戳。
     */
    private Instant endTime;

}

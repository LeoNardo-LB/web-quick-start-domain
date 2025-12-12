package org.smm.archetype.domain.log;

import lombok.Getter;
import lombok.Setter;
import org.aspectj.lang.reflect.MethodSignature;

import java.time.Instant;

/**
 * 业务日志数据传输对象
 *
 * 封装业务方法执行过程中的相关信息，包括方法签名、参数、返回值、异常信息等。
 * 用于在日志处理过程中传递数据。
 */
@Getter
@Setter
public class LogDto {

    /**
     * 日志注解信息
     *
     * 包含方法上标记的@Log注解的配置信息。
     */
    private Log log;

    /**
     * 方法签名信息
     *
     * 包含被拦截方法的签名信息，如方法名、参数类型等。
     */
    private MethodSignature signature;

    /**
     * 方法参数数组
     *
     * 包含被拦截方法的参数值数组。
     */
    private Object[] args;

    /**
     * 方法返回值
     *
     * 包含被拦截方法的返回值。
     */
    private Object result;

    /**
     * 线程名称
     *
     * 记录方法执行时所在的线程名称。
     */
    private String threadName;

    /**
     * 异常信息
     *
     * 记录方法执行过程中抛出的异常信息。
     */
    private Throwable error;

    /**
     * 方法开始执行时间
     *
     * 记录方法开始执行的时间戳。
     */
    private Instant startTime;

    /**
     * 方法执行结束时间
     *
     * 记录方法执行结束的时间戳。
     */
    private Instant endTime;

}

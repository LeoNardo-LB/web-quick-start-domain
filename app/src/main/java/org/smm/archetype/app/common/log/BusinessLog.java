package org.smm.archetype.app.common.log;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.aspectj.lang.reflect.MethodSignature;

import java.time.Instant;

/**
 * 业务日志数据传输对象
 *
 * <p>封装业务方法执行过程中的相关信息，用于日志记录和审计。
 *
 * <p>注意：这是应用层的DTO，不是领域模型。
 * 因为它包含了技术框架的详细信息（AOP、反射等）。
 * @author Leonardo
 * @since 2025/12/30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusinessLog {

    /**
     * 日志注解信息
     * 包含方法上标记的@Log注解的配置信息。
     */
    private LogAnnotation logAnnotation;

    /**
     * 方法签名信息
     * 包含被拦截方法的签名信息，如方法名、参数类型等。
     */
    private MethodSignature signature;

    /**
     * 方法参数数组
     * 包含被拦截方法的参数值数组。
     */
    private Object[] args;

    /**
     * 方法返回值
     * 包含被拦截方法的返回值。
     */
    private Object result;

    /**
     * 线程名称
     * 记录方法执行时所在的线程名称。
     */
    private String threadName;

    /**
     * 异常信息
     * 记录方法执行过程中抛出的异常信息。
     */
    private Throwable error;

    /**
     * 方法开始执行时间
     * 记录方法开始执行的时间戳。
     */
    private Instant startTime;

    /**
     * 方法执行结束时间
     * 记录方法执行结束的时间戳。
     */
    private Instant endTime;

    /**
     * 计算方法执行时长（毫秒）
     * @return 执行时长
     */
    public long getDuration() {
        if (startTime != null && endTime != null) {
            return endTime.toEpochMilli() - startTime.toEpochMilli();
        }
        return 0;
    }

}

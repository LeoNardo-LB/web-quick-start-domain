package org.smm.archetype.infrastructure.common.log;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 业务日志切面类
 *
 * 通过AOP技术拦截标记了@Log注解的方法，在方法执行前后记录相关信息。
 * 支持异步持久化日志，避免影响业务方法的执行性能。
 */
@Aspect
@Order
@RequiredArgsConstructor
public class LogAspect {

    private static final Map<Class<?>, Logger> LOGGER_MAP = new ConcurrentHashMap<>();

    private final MeterRegistry meterRegistry;

    private Timer executionTimer;

    private Counter executionCounter;

    private Counter errorCounter;

    @PostConstruct
    public void init() {
        this.executionTimer = Timer.builder("log_aspect_timer_seconds")
            .description("LogAspect method execution time in seconds")
            .register(meterRegistry);
        this.executionCounter = Counter.builder("log_aspect_counter_total")
            .description("Total number of method executions intercepted by LogAspect")
            .register(meterRegistry);
        this.errorCounter = Counter.builder("log_aspect_errors_total")
            .description("Total number of errors in methods intercepted by LogAspect")
            .register(meterRegistry);
    }

    /**
     * 业务日志切点
     * <p>
     * 定义切入点，拦截标记了@Log注解的方法。
     */
    @Pointcut("@annotation(org.smm.archetype.infrastructure.common.log.MyLog)")
    public void logCut() {
    }

    /**
     * 环绕通知处理方法
     * <p>
     * 拦截标记了@Log注解的方法，在方法执行前后收集相关信息并记录日志和指标。
     * 记录方法的入参、出参、执行时间、线程信息等，并在出现异常时记录异常信息。
     * 同时采集执行时间和调用次数等指标。
     * @param joinPoint 连接点对象
     * @return 方法执行结果
     * @throws Throwable 方法执行过程中抛出的异常
     */
    @Around(value = "logCut()")
    public Object doAround(ProceedingJoinPoint joinPoint) throws Throwable {
        Timer.Sample timerSample = Timer.start();
        executionCounter.increment();

        // 获取注解信息
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        MyLog myLog = signature.getMethod().getAnnotation(MyLog.class);
        Class<?> declaringType = signature.getDeclaringType();

        // 构建日志信息
        var builder = Log.builder();
        builder.setArgs(joinPoint.getArgs());
        builder.setSignature(signature);
        builder.setMyLog(myLog);
        builder.setThreadName(Thread.currentThread().getName());

        String className = declaringType.getSimpleName();
        String methodName = signature.getMethod().getName();

        try {
            // 执行目标方法
            builder.setStartTime(Instant.now());
            Object result = joinPoint.proceed();
            builder.setResult(result);
            return result;
        } catch (Throwable e) {
            // 捕获异常, 记录异常信息
            builder.setError(e);
            errorCounter.increment();
            throw e;
        } finally {
            timerSample.stop(executionTimer);
            builder.setEndTime(Instant.now());
            Log log = builder.build();
            Logger logger = LOGGER_MAP.computeIfAbsent(declaringType, k -> LoggerFactory.getLogger(declaringType));

            // 标准文本格式日志输出（traceId由logback pattern自动获取）
            String logMessage = String.format(
                "[方法执行] 类: %s, 方法: %s, 业务: %s, 入参: %s, 出参: %s, 耗时: %dms, 线程: %s",
                className,
                methodName,
                myLog != null ? myLog.value() : "",
                log.getArgs(),
                log.getError() == null ? log.getResult() : "ERROR",
                log.getEndTime().toEpochMilli() - log.getStartTime().toEpochMilli(),
                log.getThreadName()
            );

            if (log.getError() == null) {
                logger.info(logMessage);
            } else {
                logger.error(logMessage, log.getError());
            }
        }
    }

}

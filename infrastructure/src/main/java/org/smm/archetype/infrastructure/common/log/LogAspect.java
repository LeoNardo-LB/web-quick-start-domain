package org.smm.archetype.infrastructure.common.log;

import com.alibaba.fastjson2.JSON;
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
 * 业务日志切面，拦截@Log注解方法，记录执行信息。
 */
@Aspect
@Order
@RequiredArgsConstructor
public class LogAspect {

    /**
     * 日志序列化最大长度
     */
    private static final int MAX_LENGTH = 2048;

    /**
     * 截断后缀
     */
    private static final String TRUNCATED_SUFFIX = "...(truncated)";

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
    
     * 定义切入点，拦截标记了@Log注解的方法。
     */
    @Pointcut("@annotation(org.smm.archetype.infrastructure.common.log.MyLog)")
    public void logCut() {
    }

    /**
     * 环绕通知处理方法
    
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

            // 日志格式约定：类 | 方法 | 业务 | 耗时ms | 线程 | 入参 | 出参
            String logMessage = String.format(
                    "[方法执行] %s | %s | %s | %dms | %s | %s | %s",
                    className,
                    methodName,
                    myLog != null ? myLog.value() : "-",
                    log.getEndTime().toEpochMilli() - log.getStartTime().toEpochMilli(),
                    log.getThreadName(),
                    toSafeJson(log.getArgs()),
                    log.getError() == null ? toSafeJson(log.getResult()) : "ERROR"
            );

            if (log.getError() == null) {
                logger.info(logMessage);
            } else {
                logger.error(logMessage, log.getError());
            }
        }
    }

    /**
     * 安全序列化对象为JSON，限制长度
     * @param obj 待序列化对象
     * @return JSON字符串
     */
    private String toSafeJson(Object obj) {
        if (obj == null) {
            return "null";
        }
        try {
            String json = JSON.toJSONString(obj);
            if (json.length() > MAX_LENGTH) {
                return json.substring(0, MAX_LENGTH - TRUNCATED_SUFFIX.length()) + TRUNCATED_SUFFIX;
            }
            return json;
        } catch (Exception e) {
            return obj.getClass().getSimpleName() + "@" + Integer.toHexString(obj.hashCode());
        }
    }

}

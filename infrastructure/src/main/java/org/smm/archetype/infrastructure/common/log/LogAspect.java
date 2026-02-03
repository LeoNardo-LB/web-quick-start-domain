package org.smm.archetype.infrastructure.common.log;

import com.alibaba.fastjson2.JSON;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 业务日志切面，拦截@Log注解方法，记录执行信息。
 */
@Aspect
@Order
@RequiredArgsConstructor
@Slf4j
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
     * Client类切点 - 自动拦截所有Client实现类的方法
     * 拦截org.smm.archetype.infrastructure.bizshared.client包及其子包下的所有方法
     */
    @Pointcut("execution(* org.smm.archetype.infrastructure.bizshared.client..*.*(..))")
    public void clientCut() {
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

            // 使用统一的日志记录方法
            logExecution(
                    declaringType,
                    methodName,
                    "方法执行",
                    myLog != null ? myLog.value() : null,
                    log.getEndTime().toEpochMilli() - log.getStartTime().toEpochMilli(),
                    log.getThreadName(),
                    log.getArgs(),
                    log.getResult(),
                    log.getError()
            );
        }
    }

    /**
     * 环绕通知 - 处理Client类的异常
     *
     * 与doAround不同，Client类异常被捕获并返回默认值，不抛出异常。
     * 这样可以保证基础设施层的故障不影响业务流程。
     * @param joinPoint 连接点对象
     * @return 方法执行结果或默认值
     */
    @Around(value = "clientCut()")
    public Object handleClientException(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Class<?> declaringType = signature.getDeclaringType();
        String methodName = signature.getMethod().getName();
        String threadName = Thread.currentThread().getName();
        long startTime = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;

            // 使用统一的日志记录方法记录成功执行
            logExecution(
                    declaringType,
                    methodName,
                    "Client调用",
                    null,
                    duration,
                    threadName,
                    joinPoint.getArgs(),
                    result,
                    null
            );
            return result;
        } catch (Throwable e) {
            long duration = System.currentTimeMillis() - startTime;

            // Client类：吞掉异常，返回默认值
            // 记录异常日志，但不抛出异常，保证业务流程继续
            logExecution(
                    declaringType,
                    methodName,
                    "Client调用",
                    null,
                    duration,
                    threadName,
                    joinPoint.getArgs(),
                    null,
                    e
            );
            return getDefaultValue(signature.getReturnType());
        }
    }

    /**
     * 根据返回类型获取默认值
     * @param returnType 方法返回类型
     * @return 对应的默认值
     */
    private Object getDefaultValue(Class<?> returnType) {
        if (returnType == void.class || returnType == Void.class) {
            return null;
        } else if (returnType == boolean.class || returnType == Boolean.class) {
            return false;
        } else if (returnType == long.class || returnType == Long.class) {
            return -1L;
        } else if (returnType == int.class || returnType == Integer.class) {
            return 0;
        } else if (returnType.isPrimitive()) {
            return 0;
        } else if (List.class.isAssignableFrom(returnType)) {
            return List.of();
        } else if (Map.class.isAssignableFrom(returnType)) {
            return Map.of();
        }
        return null;
    }

    /**
     * 统一日志记录方法
     *
     * 日志格式约定：[类型] 类#方法 | 业务描述 | 耗时ms | 线程 | 入参 | 出参/错误
     * @param declaringType 声明类
     * @param methodName    方法名
     * @param logType       日志类型（如：方法执行、Client调用）
     * @param businessDesc  业务描述
     * @param durationMs    执行耗时（毫秒）
     * @param threadName    线程名称
     * @param args          方法参数
     * @param result        方法结果
     * @param error         异常信息
     */
    private void logExecution(Class<?> declaringType, String methodName, String logType,
                              String businessDesc, long durationMs, String threadName,
                              Object[] args, Object result, Throwable error) {
        Logger logger = LOGGER_MAP.computeIfAbsent(declaringType, k -> LoggerFactory.getLogger(declaringType));

        String logMessage = String.format(
                "[%s] %s#%s | %s | %dms | %s | %s | %s",
                logType,
                declaringType.getSimpleName(),
                methodName,
                businessDesc != null && !businessDesc.isEmpty() ? businessDesc : "-",
                durationMs,
                threadName,
                toSafeJson(args),
                error != null ? "ERROR" : toSafeJson(result)
        );

        if (error == null) {
            logger.info(logMessage);
        } else {
            logger.error(logMessage, error);
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

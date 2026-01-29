package org.smm.archetype.infrastructure.common.log;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smm.archetype.infrastructure.common.log.Log.LogBuilder;
import org.springframework.core.annotation.Order;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

    /**
     * 异步执行服务
     * <p>
     * 用于异步执行日志持久化操作，避免阻塞业务方法的执行。
     */
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

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
     * 拦截标记了@Log注解的方法，在方法执行前后收集相关信息并异步持久化。
     * 记录方法的入参、出参、执行时间、线程信息等，并在出现异常时记录异常信息。
     * @param joinPoint 连接点对象
     * @return 方法执行结果
     * @throws Throwable 方法执行过程中抛出的异常
     */
    @Around(value = "logCut()")
    public Object doAround(ProceedingJoinPoint joinPoint) throws Throwable {
        // 获取注解信息
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        MyLog myLog = signature.getMethod().getAnnotation(MyLog.class);
        Class<?> declaringType = signature.getDeclaringType();

        // 构建日志信息
        LogBuilder<?, ?> builder = Log.builder();
        builder.setArgs(joinPoint.getArgs());
        builder.setSignature(signature);
        builder.setMyLog(myLog);
        builder.setThreadName(Thread.currentThread().getName());
        try {
            // 执行目标方法
            builder.setStartTime(Instant.now());
            Object result = joinPoint.proceed();
            builder.setResult(result);
            return result;
        } catch (Throwable e) {
            // 捕获异常, 记录异常信息
            builder.setError(e);
            throw e;
        } finally {
            // 持久化
            builder.setEndTime(Instant.now());
            Log log = builder.build();
            Logger logger = LOGGER_MAP.computeIfAbsent(declaringType, k -> LoggerFactory.getLogger(declaringType));
            // TODO 日志的信息
            if (log.getError() == null) {
                // logger.info();
            } else {
                // logger.error();
            }
        }
    }

}

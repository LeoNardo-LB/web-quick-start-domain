package org.smm.archetype.infrastructure.shared.log;

import com.alibaba.fastjson2.JSON;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 业务日志切面，拦截@Log注解方法，记录执行信息。
 *
 * <p>⚠️ 重要提示：由于 Spring Boot 4.0.2 和 spring-boot-starter-aop 4.0.0-M2 存在兼容性问题，
 * AOP 功能当前不可用。@Aspect 注解和切点定义虽然存在，但不会被 Spring AOP 框架处理。
 *
 * <p>当 AOP 兼容性问题解决后，此类可以恢复使用。当前仅保留静态工具方法供其他场景使用。
 *
 * <p>兼容性问题详情：
 * - @EnableAspectJAutoProxy(proxyTargetClass = true) 已启用
 * - AnnotationAwareAspectJAutoProxyCreator Bean 已注册
 * - 但 @Aspect 注解的 Bean 不会被转换为 Advisor
 * - 导致拦截器无法生效
 */
@Aspect
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

    /**
     * MeterRegistry 用于采集指标（通过构造函数注入）
     *
     * <p>⚠️ 注意：虽然 Spring Boot 4.0.2 和 spring-boot-starter-aop 4.0.0-M2 存在兼容性问题，
     * 但 AOP 切面在某些场景下仍然可能被触发（如通过 CGLIB 代理）。
     * 因此保留此字段以避免 NPE。
     */
    private final MeterRegistry meterRegistry;

    private volatile Timer executionTimer;

    private volatile Counter executionCounter;

    private volatile Counter errorCounter;

    /**
     * 构造函数
     * @param meterRegistry MeterRegistry（可选，通过 Spring 注入）
     */
    public LogAspect(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
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
    private static void logExecution(Class<?> declaringType, String methodName, String logType,
                                     String businessDesc, long durationMs, String threadName,
                                     Object[] args, Object result, Throwable error) {
        Logger logger = LOGGER_MAP.computeIfAbsent(declaringType, k -> LoggerFactory.getLogger(declaringType));

        businessDesc = businessDesc != null && !businessDesc.isEmpty() ? businessDesc : "-";
        if (error == null) {
            logger.info("[{}] {}#{} | {} | {}ms | {} | {} | {}",
                    logType,
                    declaringType.getSimpleName(),
                    methodName,
                    businessDesc,
                    durationMs,
                    threadName,
                    toSafeJson(args),
                    toSafeJson(result));
        } else {
            logger.error("[{}] {}#{} | {} | {}ms | {} | {} | ERROR",
                    logType,
                    declaringType.getSimpleName(),
                    methodName,
                    businessDesc,
                    durationMs,
                    threadName,
                    toSafeJson(args),
                    error);
        }
    }

    /**
     * 安全序列化对象为JSON，限制长度
     * @param obj 待序列化对象
     * @return JSON字符串
     */
    private static String toSafeJson(Object obj) {
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

    /**
     * 初始化指标（懒加载，避免@PostConstruct不调用的问题）
     */
    private void initIfNecessary() {
        if (executionTimer == null) {
            Logger logger = LoggerFactory.getLogger(LogAspect.class);
            logger.info("LogAspect初始化开始...");

            this.executionTimer = Timer.builder("log_aspect_timer_seconds")
                                          .description("LogAspect method execution time in seconds")
                                          .register(meterRegistry);
            this.executionCounter = Counter.builder("log_aspect_counter_total")
                                            .description("Total number of method executions intercepted by LogAspect")
                                            .register(meterRegistry);
            this.errorCounter = Counter.builder("log_aspect_errors_total")
                                        .description("Total number of errors in methods intercepted by LogAspect")
                                        .register(meterRegistry);

            logger.info("LogAspect初始化完成，Timer={}, Counter={}, ErrorCounter={}",
                    executionTimer.getId(), executionCounter.getId(), errorCounter.getId());
        }
    }

    /**
     * 业务日志切点
     *
     * 定义切入点，拦截标记了@BusinessLog注解的方法。
     */
    @Pointcut("@annotation(org.smm.archetype.infrastructure.shared.log.BusinessLog)")
    public void logCut() {
    }

    /**
     * Client实现切点
     *
     * 定义切入点，拦截Client实现类的所有方法。
     */
    @Pointcut("execution(* org.smm.archetype.infrastructure..*.*(..))")
    public void clientCut() {
    }

    /**
     * 合并切入点
     *
     * 合并@Log注解和Client实现的切入点。
     */
    @Pointcut("clientCut()")
    public void combinedCut() {
    }

    /**
     * 环绕通知处理方法
     *
     * 拦截标记了@Log注解的方法，在方法执行前后收集相关信息并记录日志和指标。
     * 记录方法的入参、出参、执行时间、线程信息等，并在出现异常时记录异常信息。
     * 同时采集执行时间和调用次数等指标。
     * @param joinPoint 连接点对象
     * @return 方法执行结果
     * @throws Throwable 方法执行过程中抛出的异常
     */
    @Around(value = "combinedCut()")
    public Object doAround(ProceedingJoinPoint joinPoint) throws Throwable {
        // 调试日志
        Logger logger = LoggerFactory.getLogger(LogAspect.class);
        logger.info("=== LogAspect.doAround() 被调用 ===");
        logger.info("连接点: " + joinPoint.getSignature().toShortString());
        // 懒加载指标（避免@PostConstruct不调用的问题）
        initIfNecessary();

        Timer.Sample timerSample = Timer.start();
        executionCounter.increment();

        // 获取注解信息
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        BusinessLog businessLog = signature.getMethod().getAnnotation(BusinessLog.class);
        Class<?> declaringType = signature.getDeclaringType();

        String className = declaringType.getSimpleName();
        String methodName = signature.getMethod().getName();
        long startTime = System.currentTimeMillis();

        // 获取日志上下文（统一处理类型和业务描述）
        LogContext logContext = resolveLogContext(businessLog, declaringType);

        try {
            // 执行目标方法
            Object result = joinPoint.proceed();
            long durationMs = System.currentTimeMillis() - startTime;

            // 统一使用 logExecution 输出成功日志
            logExecution(
                    declaringType,
                    methodName,
                    logContext.type(),
                    logContext.description(),
                    durationMs,
                    Thread.currentThread().getName(),
                    joinPoint.getArgs(),
                    result,
                    null
            );

            return result;
        } catch (Throwable e) {
            // 捕获异常，记录异常信息
            long durationMs = System.currentTimeMillis() - startTime;
            errorCounter.increment();

            // 统一使用 logExecution 输出错误日志
            logExecution(
                    declaringType,
                    methodName,
                    logContext.type(),
                    logContext.description(),
                    durationMs,
                    Thread.currentThread().getName(),
                    joinPoint.getArgs(),
                    null,
                    e
            );

            throw e;
        } finally {
            timerSample.stop(executionTimer);
        }
    }

    /**
     * 解析日志上下文，根据注解和类型确定日志类型和业务描述
     * 使用策略模式思想，提高可维护性和可读性
     * @param businessLog   方法上的@BusinessLog注解
     * @param declaringType 方法声明类型
     * @return 日志上下文
     */
    private LogContext resolveLogContext(BusinessLog businessLog, Class<?> declaringType) {
        // 优先级1: 有@BusinessLog注解 -> 方法执行日志
        if (businessLog != null) {
            return new LogContext("方法执行", businessLog.value());
        }

        // 优先级2: Client接口方法 -> Client调用日志
        if (isClientType(declaringType)) {
            return new LogContext("Client调用", "-");
        }

        // 默认: 普通方法调用
        return new LogContext("方法调用", "-");
    }

    /**
     * 判断是否为Client接口类型
     */
    private boolean isClientType(Class<?> type) {
        return type != null && type.getPackage() != null &&
                       type.getPackage().getName().contains("client");
    }

    /**
         * 日志上下文内部类，封装日志类型和业务描述
         */
        private record LogContext(String type, String description) {

    }

}

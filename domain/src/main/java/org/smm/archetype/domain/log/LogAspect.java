package org.smm.archetype.domain.log;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.smm.archetype.domain.log.handler.persistence.PersistenceHandler;
import org.smm.archetype.domain.log.handler.persistence.PersistenceType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * 业务日志切面类
 *
 * 通过AOP技术拦截标记了@Log注解的方法，在方法执行前后记录相关信息。
 * 支持异步持久化日志，避免影响业务方法的执行性能。
 */
@Slf4j
@Aspect
@Order
@Component
public class LogAspect {

    /**
     * 持久化处理器映射表
     *
     * 存储不同持久化类型的处理器实例，用于根据日志配置的持久化类型执行相应的持久化操作。
     */
    private final Map<PersistenceType, PersistenceHandler> persistenceHandlerMap;

    /**
     * 异步执行服务
     *
     * 用于异步执行日志持久化操作，避免阻塞业务方法的执行。
     */
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    /**
     * 构造函数
     *
     * 初始化持久化处理器映射表，将注入的处理器列表转换为按类型索引的映射表。
     * @param persistenceHandlers 持久化处理器列表
     */
    @Autowired
    public LogAspect(List<PersistenceHandler> persistenceHandlers) {

        this.persistenceHandlerMap = persistenceHandlers.stream().collect(Collectors.toMap(PersistenceHandler::getPersistenceType, s -> s));
    }

    /**
     * 业务日志切点
     *
     * 定义切入点，拦截标记了@Log注解的方法。
     */
    @Pointcut("@annotation(org.smm.archetype.util.util.log.BizLog)")
    public void bizLogCut() {
    }

    /**
     * 环绕通知处理方法
     *
     * 拦截标记了@Log注解的方法，在方法执行前后收集相关信息并异步持久化。
     * 记录方法的入参、出参、执行时间、线程信息等，并在出现异常时记录异常信息。
     * @param joinPoint 连接点对象
     * @return 方法执行结果
     * @throws Throwable 方法执行过程中抛出的异常
     */
    @Around(value = "bizLogCut()")
    public Object doAround(ProceedingJoinPoint joinPoint) throws Throwable {
        // 获取注解信息
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Log log = signature.getMethod().getAnnotation(Log.class);
        // 构建日志信息
        LogDto LogDto = new LogDto();
        LogDto.setArgs(joinPoint.getArgs());
        LogDto.setSignature(signature);
        LogDto.setLog(log);
        LogDto.setThreadName(Thread.currentThread().getName());
        try {
            // 执行目标方法
            LogDto.setStartTime(Instant.now());
            Object result = joinPoint.proceed();
            LogDto.setResult(result);
            return result;
        } catch (Throwable e) {
            // 捕获异常, 记录异常信息
            LogDto.setError(e);
            throw e;
        } finally {
            // 持久化
            LogDto.setEndTime(Instant.now());
            executorService.execute(() -> {
                PersistenceType[] persistence = log.persistence();
                for (PersistenceType persistenceType : persistence) {
                    Optional.ofNullable(persistenceHandlerMap.get(persistenceType)).ifPresent(handler -> handler.persist(LogDto));
                }
            });
        }
    }

}

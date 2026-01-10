package org.smm.archetype.adapter.access.web.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Duration;
import java.time.Instant;

/**
 * Web层日志切面
 *
 * <p>记录所有Controller方法的调用日志，包括请求信息、响应信息和耗时。
 *
 * <p>记录内容：
 * <ul>
 *   <li>请求方法和URI</li>
 *   <li>处理耗时</li>
 *   <li>处理结果（成功/失败）</li>
 *   <li>异常信息（如果失败）</li>
 * </ul>
 * @author Leonardo
 * @since 2026/01/10
 */
@Slf4j
@Aspect
@Component
public class LoggingAspect {

    /**
     * 定义切点：所有Controller方法
     */
    @Pointcut("execution(* org.smm.archetype.adapter.access.web.api..*.*(..))")
    public void controllerPointcut() {
    }

    /**
     * 环绕通知：记录请求和响应日志
     * @param joinPoint 连接点
     * @return 方法执行结果
     * @throws Throwable 方法执行异常
     */
    @Around("controllerPointcut()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        Instant start = Instant.now();

        // 获取请求信息
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        String uri = attributes != null ? attributes.getRequest().getRequestURI() : "unknown";
        String method = attributes != null ? attributes.getRequest().getMethod() : "unknown";

        log.info(">>> Request: {} {}", method, uri);

        Object result = null;
        try {
            result = joinPoint.proceed();

            long duration = Duration.between(start, Instant.now()).toMillis();
            log.info("<<< Response: {} {}, Duration: {}ms", method, uri, duration);

            return result;
        } catch (Exception e) {
            long duration = Duration.between(start, Instant.now()).toMillis();
            log.error("<<< Error: {} {}, Duration: {}ms, Error: {}",
                    method, uri, duration, e.getMessage());
            throw e;
        }
    }

}

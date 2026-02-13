package org.smm.archetype.infrastructure.shared.event.publisher;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.smm.archetype.domain.shared.event.DomainEventPublisher;
import org.smm.archetype.infrastructure.shared.util.context.ScopedThreadContext;

import java.util.ArrayList;

/**
 * 领域事件收集切面
 * 默认的事务顺序是最低优先级，即在事务提交之后执行


 */
@Aspect
@RequiredArgsConstructor
public class DomainEventCollectAspectJ {

    private final DomainEventPublisher domainEventPublisher;

    @Pointcut("execution(* org.smm.archetype.app..*AppServiceImpl.*(..))")
    public void appLayer() {

    }

    @SneakyThrows
    @Around("appLayer()")
    public Object around(ProceedingJoinPoint joinPoint) {
        return ScopedThreadContext.callWithDomainEvents(new ArrayList<>(), () -> {
            try {
                return joinPoint.proceed();
            } catch (Throwable e) {
                if (e instanceof RuntimeException re) {
                    throw re;
                }
                throw new RuntimeException(e);
            }
        });
    }

}

package org.smm.archetype.infrastructure._shared.component.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.smm.archetype.domain._shared.base.AggregateRoot;
import org.smm.archetype.domain._shared.base.DomainEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 事务事件发布切面
 *
 * <p>负责在应用服务方法执行过程中，收集聚合根中的领域事件，
 * 并在事务提交后通过Spring事件机制发布。
 *
 * <p>工作流程：
 * <ol>
 *   <li>在方法执行前，从参数中收集聚合根</li>
 *   <li>执行业务逻辑</li>
 *   <li>在方法执行后，从返回值中收集聚合根</li>
 *   <li>合并参数和返回值中的聚合根</li>
 *   <li>发布Spring内部事件，携带聚合根事件列表</li>
 *   <li>由TransactionalEventPublisher在事务提交后处理实际的事件发布</li>
 * </ol>
 *
 * <p>设计优势：
 * <ul>
 *   <li>统一的事件发布时机</li>
 *   <li>保证事务一致性</li>
 *   <li>避免重复代码</li>
 *   <li>对业务代码零侵入</li>
 *   <li>即使Repository重建了聚合根，也能从原始参数收集事件</li>
 * </ul>
 *
 * @author Leonardo
 * @since 2025/12/30
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class TransactionEventPublishingAspect {

    private final ApplicationEventPublisher applicationEventPublisher;

    /**
     * 环绕通知：拦截应用服务方法
     *
     * <p>拦截规则：
     * <ul>
     *   <li>拦截app包下所有public方法</li>
     *   <li>类上有@Transactional注解（包括继承的）</li>
     * </ul>
     */
    @Around("execution(* org.smm.archetype.app..*.*(..)) && " +
            "@within(org.springframework.transaction.annotation.Transactional)")
    public Object collectAndPublishEvents(ProceedingJoinPoint joinPoint) throws Throwable {
        // 1. 方法执行前，从参数中收集聚合根
        List<AggregateRoot> aggregatesFromParams = new ArrayList<>();
        collectAggregates(joinPoint.getArgs(), aggregatesFromParams);

        // 2. 执行业务方法
        Object result = joinPoint.proceed();

        // 3. 方法执行后，从返回值中收集聚合根
        List<AggregateRoot> aggregatesFromResult = new ArrayList<>();
        if (result != null) {
            collectAggregates(new Object[]{result}, aggregatesFromResult);
        }

        // 4. 合并参数和返回值中的聚合根
        List<AggregateRoot> allAggregates = new ArrayList<>();
        allAggregates.addAll(aggregatesFromParams);
        allAggregates.addAll(aggregatesFromResult);

        // 5. 收集所有领域事件
        List<DomainEvent> allEvents = new ArrayList<>();
        for (AggregateRoot aggregate : allAggregates) {
            allEvents.addAll(aggregate.getUncommittedEvents());
        }

        // 6. 如果有事件，发布Spring内部事件
        if (!allEvents.isEmpty()) {
            log.debug("Collected {} domain events to publish after transaction commit",
                    allEvents.size());

            // 发布Spring内部事件，由TransactionalEventPublisher在事务提交后处理
            applicationEventPublisher.publishEvent(
                    new DomainEventsCollectionEvent(allAggregates, allEvents)
            );
        }

        return result;
    }

    /**
     * 收集约合根
     */
    private void collectAggregates(Object[] objects, List<AggregateRoot> aggregates) {
        for (Object obj : objects) {
            if (obj instanceof AggregateRoot aggregate) {
                if (aggregate.hasUncommittedEvents()) {
                    aggregates.add(aggregate);
                }
            }
            // 支持集合中的聚合根
            else if (obj instanceof Iterable<?> iterable) {
                for (Object item : iterable) {
                    if (item instanceof AggregateRoot aggregate) {
                        if (aggregate.hasUncommittedEvents()) {
                            aggregates.add(aggregate);
                        }
                    }
                }
            }
        }
    }

}

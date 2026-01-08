package org.smm.archetype.infrastructure._shared.component.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smm.archetype.domain._shared.base.AggregateRoot;
import org.smm.archetype.domain._shared.base.DomainEvent;
import org.smm.archetype.domain._shared.event.EventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

/**
 * 事务性事件发布器
 *
 * <p>监听Spring内部事件，在事务提交后发布实际的领域事件。
 * 使用@TransactionalEventListener确保事件只在事务成功提交后才发布。
 *
 * <p>工作流程：
 * <ol>
 *   <li>监听DomainEventsCollectionEvent（由切面发布）</li>
 *   <li>等待事务提交成功（AFTER_COMMIT阶段）</li>
 *   <li>调用EventPublisher发布领域事件</li>
 *   <li>标记聚合根的事件为已提交</li>
 * </ol>
 *
 * <p>设计优势：
 * <ul>
 *   <li>保证事件只在事务提交后发布</li>
 *   <li>如果事务回滚，事件不会发布</li>
 *   <li>异步发布，不阻塞主线程</li>
 * </ul>
 *
 * @author Leonardo
 * @since 2025/12/30
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionalEventPublisher {

    private final EventPublisher eventPublisher;

    /**
     * 在事务提交后发布领域事件
     *
     * <p>使用@TransactionalEventListener(phase = AFTER_COMMIT)确保：
     * <ul>
     *   <li>只在事务成功提交后才执行</li>
 *   *   <li>如果事务回滚，此方法不会执行</li>
     *   <li>保证事件发布与数据库事务的一致性</li>
     * </ul>
     *
     * @param event 领域事件集合
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onTransactionCommit(DomainEventsCollectionEvent event) {
        List<DomainEvent> domainEvents = event.getDomainEvents();
        List<AggregateRoot> aggregates = event.getAggregates();

        if (domainEvents.isEmpty()) {
            return;
        }

        log.debug("Transaction committed, publishing {} domain events", domainEvents.size());

        try {
            // 发布领域事件
            eventPublisher.publish(domainEvents);

            // 标记所有聚合根的事件为已提交
            for (AggregateRoot aggregate : aggregates) {
                aggregate.markEventsAsCommitted();
            }

            log.debug("Domain events published successfully");
        } catch (Exception e) {
            log.error("Failed to publish domain events after transaction commit", e);
            // 这里可以添加重试逻辑或记录到失败表
            // 可以考虑将失败的事件持久化到outbox表，由后台任务重试
        }
    }

}

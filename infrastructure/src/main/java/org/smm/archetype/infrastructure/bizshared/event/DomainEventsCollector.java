package org.smm.archetype.infrastructure.bizshared.event;

import lombok.Getter;
import org.smm.archetype.domain.bizshared.base.AggregateRoot;
import org.smm.archetype.domain.bizshared.base.DomainEvent;
import org.springframework.context.ApplicationEvent;

import java.util.List;

/**
 * 领域事件集合事件
 *
 * <p>这是一个Spring内部事件，用于在事务提交后传递领域事件。
 * 切面在业务方法执行后发布此事件，TransactionalEventPublisher
 * 在事务提交后监听此事件并发布实际的领域事件。
 *
 * @author Leonardo
 * @since 2025/12/30
 */
@Getter
public class DomainEventsCollector extends ApplicationEvent {

    private final List<AggregateRoot> aggregates;

    private final List<DomainEvent> domainEvents;

    public DomainEventsCollector(List<AggregateRoot> aggregates, List<DomainEvent> domainEvents) {
        super(aggregates);
        this.aggregates = aggregates;
        this.domainEvents = domainEvents;
    }

}

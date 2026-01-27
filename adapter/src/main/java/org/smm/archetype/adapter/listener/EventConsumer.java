package org.smm.archetype.adapter.listener;

import org.smm.archetype.domain.bizshared.base.DomainEvent;

/**
 * 消费接口
 * @author Leonardo
 * @since 2026/1/10
 */
public interface EventConsumer<T extends DomainEvent> {

    /**
     * 消费领域事件
     * @param event 领域事件
     */
    void consume(T event);

}

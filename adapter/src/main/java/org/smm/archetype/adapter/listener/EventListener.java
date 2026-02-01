package org.smm.archetype.adapter.listener;

import org.smm.archetype.domain.bizshared.event.Event;

/**
 * 消费接口
 * @author Leonardo
 * @since 2026/1/10
 */
public interface EventListener<T extends Event<?>> {

    /**
     * 消费领域事件
     * @param event 领域事件
     */
    void consume(T event);

}

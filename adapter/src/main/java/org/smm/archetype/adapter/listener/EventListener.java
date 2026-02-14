package org.smm.archetype.adapter.listener;

import org.smm.archetype.domain.shared.event.Event;

/**
 * 事件监听接口，消费领域事件。
 *
 * @param <T> 事件类型
 */
public interface EventListener<T extends Event<?>> {

    /**
     * 消费领域事件
     * @param event 领域事件
     */
    void consume(T event);

}

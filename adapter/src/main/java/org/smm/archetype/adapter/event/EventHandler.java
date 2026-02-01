package org.smm.archetype.adapter.event;

import org.smm.archetype.domain.bizshared.event.Event;
import org.springframework.core.Ordered;

/**
 * 事件处理接口
 * @param <T> 事件负载类型
 * @author Leonardo
 * @since 2026/1/31
 */
public interface EventHandler<T> extends Ordered {

    Event<T> canHandle(Event<Object> event);

    void handle(T payload);

    @Override
    default int getOrder() {
        return 0;
    }

}

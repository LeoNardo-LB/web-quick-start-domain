package org.smm.archetype.adapter.event;

import org.smm.archetype.domain.shared.event.Event;
import org.springframework.core.Ordered;

/**
 * 事件处理接口，定义事件处理契约。
 */
public interface EventHandler<T> extends Ordered {

    Event<T> canHandle(Event<Object> event);

    void handle(T payload);

    @Override
    default int getOrder() {
        return 0;
    }

}

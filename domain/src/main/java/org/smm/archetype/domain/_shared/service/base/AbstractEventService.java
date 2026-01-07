package org.smm.archetype.domain._shared.service.base;

import org.smm.archetype.domain._shared.base.BaseEvent;
import org.smm.archetype.domain._shared.service.EventService;

/**
 * 抽象事件服务
 * @author Leonardo
 * @since 2026/1/7
 */
public abstract class AbstractEventService implements EventService<BaseEvent<?>> {

    @Override
    public final void publish(BaseEvent<?> event) {
        publishEvent(event);
        if (event.getType().isPersistent()) {
            save(event);
        }
    }

    /**
     * 发布事件
     * @param event 事件
     */
    protected abstract void publishEvent(BaseEvent<?> event);

    /**
     * 保存事件
     * @param event 事件
     */
    protected abstract void save(BaseEvent<?> event);

}

package org.smm.archetype.shared.base;

/**
 *
 *
 * @author Leonardo
 * @since 2025/12/30
 */
public abstract class BaseEventPublisher<T extends BaseEvent<?>> {

    public final void publish(T event) {
        publishEvent(event);
        event.complete();
        if (event.persistent()) {
            save(event);
        }
    }

    /**
     * 发布事件
     * @param event 事件
     */
    protected abstract void publishEvent(T event);

    /**
     * 持久化事件
     * 如需持久化，必须实现此方法
     * @param event 事件
     */
    protected void save(T event) {
        throw new UnsupportedOperationException("Not implemented");
    }

}

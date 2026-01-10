package org.smm.archetype.adapter.access.listener;

import org.smm.archetype.domain._shared.base.DomainEvent;

/**
 * 事件监听器接口
 *
 * <p>作为入口调用方，接收外部事件消息并委托给 EventHandler 处理。
 * @author Leonardo
 * @since 2026/1/9
 */
public interface EventListener {

    /**
     * 处理领域事件
     *
     * <p>此方法作为入口点，将事件委托给合适的 EventHandler 处理。
     * @param event 领域事件
     */
    void onEvent(DomainEvent event);

}

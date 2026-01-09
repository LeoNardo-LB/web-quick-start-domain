package org.smm.archetype.adapter.access.listener;

import org.smm.archetype.adapter.handler.event.EventHandler;
import org.smm.archetype.domain._shared.base.DomainEvent;
import org.smm.archetype.domain._shared.event.EventType;

import java.util.List;

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

    /**
     * 获取支持的事件类型
     * @return 事件类型枚举
     */
    EventType getEventType();

    /**
     * 获取事件处理器列表
     * @return 事件处理器列表
     */
    List<EventHandler<DomainEvent>> getEventHandlers();

}

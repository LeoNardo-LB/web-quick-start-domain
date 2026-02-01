package org.smm.archetype.domain.bizshared.event;

/**
 * 事件发布器（Event Publisher）接口
 *
 * <p>事件发布器用于将事件发布到订阅者，实现最终一致性。
 *
 * <p>职责：
 * <ul>
 *   <li>发布事件到消息总线</li>
 *   <li>处理事件订阅和分发</li>
 *   <li>保证事件的可靠传递</li>
 *   <li>支持异步发布</li>
 * </ul>
 *
 * @author Leonardo
 * @since 2025/12/30
 */
public interface DomainEventPublisher {

    /**
     * 发布事件
     *
     * @param event 事件对象
     */
    void publish(Event<?> event);

}

package org.smm.archetype.domain.shared.event;

/**
 * 领域事件发布器接口，实现事件驱动最终一致性。
 */
public interface DomainEventPublisher {

    /**
     * 发布事件
     *
     * @param event 事件对象
     */
    void publish(Event<?> event);

}

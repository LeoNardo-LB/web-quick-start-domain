package org.smm.archetype.domain._shared.event;

import org.smm.archetype.domain._shared.base.DomainEvent;

/**
 * 事件消息处理器接口
 *
 * <p>定义事件消息处理的标准接口。
 *
 * @param <T> 事件类型
 * @author Leonardo
 * @since 2026/01/09
 */
public interface EventMessageHandler<T extends DomainEvent> {

    /**
     * 处理事件消息
     *
     * <p>此方法会在事务中执行，确保消息处理的原子性。
     *
     * @param event 领域事件
     * @throws Exception 处理异常
     */
    void handle(T event) throws Exception;

    /**
     * 获取消费者组名称
     * @return 消费者组名称
     */
    String getConsumerGroup();

    /**
     * 获取消费者名称
     * @return 消费者名称
     */
    String getConsumerName();

    /**
     * 获取幂等键
     *
     * <p>用于防止重复消费。
     * 通常使用事件ID或业务唯一标识。
     *
     * @param event 领域事件
     * @return 幂等键
     */
    default String getIdempotentKey(T event) {
        return event.getEventId();
    }

    /**
     * 判断是否可以处理该事件
     * @param event 领域事件
     * @return true-可以处理，false-不可以处理
     */
    default boolean canHandle(DomainEvent event) {
        return true;
    }

}

package org.smm.archetype.adapter.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smm.archetype.adapter.event.EventDispatcher;
import org.smm.archetype.domain.bizshared.event.Event;
import org.springframework.kafka.annotation.KafkaListener;

/**
 * Kafka 事件监听器
 *
 * <p>监听 Kafka 消息队列中的事件，并委托给 EventDispatcher 处理。
 *
 * <p>职责：
 * <ul>
 *   <li>作为 Kafka 消息的入口</li>
 *   <li>将事件委托给 EventDispatcher 进行统一处理</li>
 * </ul>
 *
 * <p>事件的完整生命周期（幂等检查、状态流转、重试等）由 EventDispatcher 统一控制。
 *
 * @author Leonardo
 * @since 2026/01/10
 */
@Slf4j
@RequiredArgsConstructor
public class KafkaDomainEventListener {

    private final EventDispatcher eventDispatcher;

    /**
     * 处理 Kafka 消息
     *
     * <p>Spring Kafka 会自动根据 __TypeId__ header 反序列化为具体的 Event 类型。
     *
     * @param event 事件（已自动反序列化）
     */
    @KafkaListener(topics = "${middleware.domain-event.consumer.kafka.topic}")
    public void onEvent(Event<?> event) {
        log.debug("Received event from Kafka: eventId={}, type={}",
                event.getEid(), event.getClass().getSimpleName());

        // 委托给 EventDispatcher 处理（首次消费，isRetry=false）
        eventDispatcher.dispatch(event, false);
    }

}

package org.smm.archetype.adapter.listener;

import lombok.extern.slf4j.Slf4j;
import org.smm.archetype.adapter.schedule.handler.EventHandler;
import org.smm.archetype.domain.bizshared.base.DomainEvent;
import org.smm.archetype.adapter.schedule.RetryStrategy;
import org.smm.archetype.infrastructure.bizshared.event.repository.EventConsumeRepository;
import org.smm.archetype.infrastructure.bizshared.event.repository.EventPublishRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Kafka 事件监听器
 *
 * <p>监听 Kafka 消息队列中的领域事件，使用 Spring Kafka 的自动反序列化功能。
 *
 * <p>Spring Kafka 的 JsonDeserializer 会根据消息中的 __TypeId__ header
 * 自动将 JSON 消息反序列化为对应的 DomainEvent 子类型。
 *
 * @author Leonardo
 * @since 2026/01/10
 */
@Slf4j
public class KafkaEventListener extends AbstractEventConsumer<DomainEvent> implements EventListener {

    public KafkaEventListener(
            EventConsumeRepository eventConsumeRepository,
            EventPublishRepository eventPublishRepository,
            List<EventHandler<DomainEvent>> eventHandlers,
            RetryStrategy retryStrategy) {
        super(eventConsumeRepository, eventPublishRepository, eventHandlers, retryStrategy);
    }

    @Override
    protected String getConsumerGroup() {
        return "kafka-consumer-group";
    }

    @Override
    protected String getConsumerName() {
        return "KafkaEventListener";
    }

    /**
     * 处理Kafka消息
     *
     * <p>Spring Kafka 会自动根据 __TypeId__ header 反序列化为具体的 DomainEvent 子类型。
     * @param event 领域事件（已自动反序列化）
     */
    @Override
    @KafkaListener(
            topics = "${middleware.kafka.consumer.topics}",
            groupId = "${middleware.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional(rollbackFor = Exception.class)
    public void onEvent(DomainEvent event) {
        log.debug("Received event from Kafka: eventId={}, type={}",
                event.getEventId(), event.getClass().getSimpleName());
        consume(event);
    }

}

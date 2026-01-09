package org.smm.archetype.adapter.access.listener;

import lombok.extern.slf4j.Slf4j;
import org.smm.archetype.adapter.handler.event.EventHandler;
import org.smm.archetype.domain._shared.base.DomainEvent;
import org.smm.archetype.domain._shared.event.EventType;
import org.smm.archetype.infrastructure._shared.event.EventConsumeRepository;
import org.smm.archetype.infrastructure._shared.event.EventSerializer;
import org.smm.archetype.infrastructure._shared.generated.repository.entity.EventConsumeDO;
import org.smm.archetype.infrastructure._shared.generated.repository.mapper.EventConsumeMapper;
import org.smm.archetype.infrastructure._shared.generated.repository.mapper.EventPublishMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Kafka 事件监听器
 *
 * <p>监听 Kafka 消息队列中的领域事件，并调用 EventHandler 处理。
 * @author Leonardo
 * @since 2026/01/09
 */
@Slf4j
@Component
public class KafkaEventListener extends AbstractEventConsumer<DomainEvent> implements EventListener {

    public KafkaEventListener(
            EventConsumeMapper eventConsumeMapper,
            EventConsumeRepository eventConsumeRepository,
            EventPublishMapper eventPublishMapper,
            EventSerializer eventSerializer,
            List<EventHandler<DomainEvent>> eventHandlers) {
        super(eventConsumeMapper, eventConsumeRepository, eventPublishMapper, eventSerializer, eventHandlers);
    }

    @Override
    protected String getConsumerGroup() {
        return "kafka-consumer-group";
    }

    @Override
    protected String getConsumerName() {
        return "KafkaEventListener";
    }

    @Override
    @KafkaListener(topics = "domain-events")
    public void onEvent(DomainEvent event) {
        log.debug("Received event from Kafka: eventId={}", event.getEventId());
        consume(event);
    }

    /**
     * 监听 Kafka 消息（字符串格式）
     * @param message JSON 格式的事件消息
     */
    @KafkaListener(topics = "domain-events")
    public void onMessage(String message) {
        try {
            log.debug("Received message from Kafka: {}", message);

            // 反序列化为 DomainEvent
            DomainEvent event = deserializeMessage(message);

            // 调用 consume 方法
            consume(event);

        } catch (Exception e) {
            log.error("Error processing Kafka message: {}", message, e);
        }
    }

    @Override
    protected void doConsume(DomainEvent event, EventConsumeDO consumeDO) throws Exception {
        // 调用对应的 EventHandler 处理事件
        for (EventHandler<DomainEvent> handler : eventHandlers) {
            if (handler.canHandle(event)) {
                log.debug("Delegating to handler: eventId={}, handler={}",
                        event.getEventId(), handler.getClass().getSimpleName());
                handler.handle(event);
                return;
            }
        }

        log.warn("No handler found for event: eventId={}, type={}",
                event.getEventId(), event.getEventTypeName());
    }

    @Override
    public EventType getEventType() {
        return EventType.UNKNOWN; // 支持所有事件类型
    }

    @Override
    public List<EventHandler<DomainEvent>> getEventHandlers() {
        return eventHandlers;
    }

    /**
     * 反序列化消息为 DomainEvent
     * @param message JSON 消息
     * @return DomainEvent
     */
    private DomainEvent deserializeMessage(String message) {
        // TODO: 从消息中提取事件类型和事件数据
        // 简化实现：假设消息是完整的 JSON，包含类型信息
        return eventSerializer.deserialize(message, DomainEvent.class.getName());
    }

}


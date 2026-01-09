package org.smm.archetype.adapter.access.listener;

import lombok.extern.slf4j.Slf4j;
import org.smm.archetype.adapter.handler.event.EventHandler;
import org.smm.archetype.domain._shared.base.DomainEvent;
import org.smm.archetype.domain._shared.event.EventType;
import org.smm.archetype.infrastructure._shared.event.DomainSpringEvent;
import org.smm.archetype.infrastructure._shared.event.EventConsumeRepository;
import org.smm.archetype.infrastructure._shared.event.EventSerializer;
import org.smm.archetype.infrastructure._shared.generated.repository.entity.EventConsumeDO;
import org.smm.archetype.infrastructure._shared.generated.repository.mapper.EventConsumeMapper;
import org.smm.archetype.infrastructure._shared.generated.repository.mapper.EventPublishMapper;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Spring 事件监听器
 *
 * <p>监听 Spring 事件总线中的领域事件，并调用 EventHandler 处理。
 * @author Leonardo
 * @since 2026/01/09
 */
@Slf4j
@Component
public class SpringEventListener extends AbstractEventConsumer<DomainEvent>
        implements org.smm.archetype.adapter.access.listener.EventListener {

    public SpringEventListener(
            EventConsumeMapper eventConsumeMapper,
            EventConsumeRepository eventConsumeRepository,
            EventPublishMapper eventPublishMapper,
            EventSerializer eventSerializer,
            List<EventHandler<DomainEvent>> eventHandlers) {
        super(eventConsumeMapper, eventConsumeRepository, eventPublishMapper, eventSerializer, eventHandlers);
    }

    @Override
    protected String getConsumerGroup() {
        return "spring-consumer-group";
    }

    @Override
    protected String getConsumerName() {
        return "SpringEventListener";
    }

    @Override
    @EventListener
    @Async("virtualTaskExecutor")
    public void onEvent(DomainEvent event) {
        log.debug("Received event from Spring: eventId={}", event.getEventId());
        consume(event);
    }

    /**
     * 监听 Spring 事件包装类
     * @param springEvent Spring 事件包装类
     */
    @EventListener
    @Async("virtualTaskExecutor")
    public void onDomainSpringEvent(DomainSpringEvent springEvent) {
        try {
            DomainEvent event = springEvent.getDomainEvent();
            log.debug("Received DomainSpringEvent: eventId={}", event.getEventId());
            consume(event);
        } catch (Exception e) {
            log.error("Error processing DomainSpringEvent", e);
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

}


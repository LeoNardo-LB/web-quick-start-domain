package org.smm.archetype.adapter.access.listener;

import lombok.extern.slf4j.Slf4j;
import org.smm.archetype.app._shared.event.EventHandler;
import org.smm.archetype.domain._shared.base.DomainEvent;
import org.smm.archetype.infrastructure._shared.event.EventConsumeRepository;
import org.smm.archetype.infrastructure._shared.generated.repository.mapper.EventConsumeMapper;
import org.smm.archetype.infrastructure._shared.generated.repository.mapper.EventPublishMapper;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Spring 事件监听器
 *
 * <p>监听 Spring 事件总线中的领域事件，并调用 EventHandler 处理。
 * @author Leonardo
 * @since 2026/01/09
 */
@Slf4j
public class SpringEventListener extends AbstractEventConsumer<DomainEvent>
        implements org.smm.archetype.adapter.access.listener.EventListener {

    public SpringEventListener(
            EventConsumeMapper eventConsumeMapper,
            EventConsumeRepository eventConsumeRepository,
            EventPublishMapper eventPublishMapper,
            List<EventHandler<DomainEvent>> eventHandlers) {
        super(eventConsumeMapper, eventConsumeRepository, eventPublishMapper, eventHandlers);
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
    @Transactional(rollbackFor = Exception.class)
    public void onEvent(DomainEvent event) {
        log.debug("Received event from Spring: eventId={}", event.getEventId());
        consume(event);
    }

}


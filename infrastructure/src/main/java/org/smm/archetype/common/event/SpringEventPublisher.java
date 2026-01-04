package org.smm.archetype.common.event;

import lombok.RequiredArgsConstructor;
import org.smm.archetype.shared.base.BaseEvent;
import org.smm.archetype.shared.base.BaseEventPublisher;
import org.smm.archetype.common.event.EventRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 *
 *
 * @author Leonardo
 * @since 2025/12/30
 */
@Component
@RequiredArgsConstructor
public class SpringEventPublisher extends BaseEventPublisher<BaseEvent<?>> {

    private final ApplicationEventPublisher applicationEventPublisher;

    private final EventRepository eventRepository;

    @Override
    protected void publishEvent(BaseEvent<?> event) {
        applicationEventPublisher.publishEvent(event);
    }

    @Override
    protected void save(BaseEvent<?> event) {
        eventRepository.insert(event);
    }

}

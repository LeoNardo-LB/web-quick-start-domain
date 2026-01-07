package org.smm.archetype.infrastructure._shared.component.event;

import lombok.RequiredArgsConstructor;
import org.smm.archetype.domain._shared.component.event.EventRepository;
import org.smm.archetype.domain._shared.base.BaseEvent;
import org.smm.archetype.domain._shared.base.BaseEvent.Status;
import org.smm.archetype.domain._shared.service.base.AbstractEventService;
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
public class SpringEventService extends AbstractEventService {

    private final ApplicationEventPublisher applicationEventPublisher;

    private final EventRepository eventRepository;

    @Override
    protected void publishEvent(BaseEvent<?> event) {
        applicationEventPublisher.publishEvent(event);
    }

    @Override
    protected void save(BaseEvent<?> event) {
        event.setStatus(Status.PUBLISHED);
        eventRepository.insert(event);
    }

}
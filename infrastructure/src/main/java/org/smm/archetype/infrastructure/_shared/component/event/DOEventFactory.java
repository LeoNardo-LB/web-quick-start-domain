package org.smm.archetype.infrastructure._shared.component.event;

import org.smm.archetype.domain._shared.component.event.EventFactory;
import org.smm.archetype.domain._shared.base.BaseEvent;
import org.smm.archetype.infrastructure._shared.component.event.repository.entity.EventPublishDO;
import org.springframework.stereotype.Component;

/**
 *
 *
 * @author Leonardo
 * @since 2026/1/7
 */
@Component
public class DOEventFactory<D extends BaseEvent<?>> implements EventFactory<D, EventPublishDO> {

    @Override
    public D createEvent(EventPublishDO eventPublishDO) {
        String type = eventPublishDO.getType();
        return switch (BaseEvent.Type.valueOf(type)) {
            case NON -> null;
            case null, default -> throw new IllegalArgumentException("Invalid event type: " + type);
        };
    }

}

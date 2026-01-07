package org.smm.archetype.infrastructure._shared.context.impl;

import lombok.experimental.SuperBuilder;
import org.smm.archetype.domain._shared.base.BaseEvent;
import org.smm.archetype.infrastructure._shared.context.Context;

/**
 *
 *
 * @author Leonardo
 * @since 2025/12/30
 */
@SuperBuilder(setterPrefix = "set")
public class EventContext<T extends BaseEvent<?>> implements Context<T> {

    private T event;

    @Override
    public T getData() {
        return event;
    }

}

package org.smm.archetype.shared.context.impl;

import lombok.experimental.SuperBuilder;

/**
 *
 *
 * @author Leonardo
 * @since 2025/12/30
 */
@SuperBuilder(setterPrefix = "set")
public class EventContext<T> implements Context<T> {

    private T event;

    @Override
    public T getData() {
        return event;
    }

}

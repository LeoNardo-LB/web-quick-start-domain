package org.smm.archetype.domain._shared.service;

import org.smm.archetype.domain._shared.base.BaseEvent;

/**
 *
 * @author Leonardo
 * @since 2026/1/7
 */
public interface EventService<T extends BaseEvent<?>> {

    void publish(T event);

}

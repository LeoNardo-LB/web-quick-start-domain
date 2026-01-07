package org.smm.archetype.domain._shared.component.event;

import org.smm.archetype.domain._shared.base.BaseEvent;

/**
 *
 *
 * @author Leonardo
 * @since 2025/12/31
 */
public interface EventFactory<E extends BaseEvent<?>, D> {

    E createEvent(D d);

}

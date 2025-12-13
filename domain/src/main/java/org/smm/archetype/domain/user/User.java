package org.smm.archetype.domain.user;

import org.smm.archetype.common.event.Event.Type;
import org.smm.archetype.common.event.EventContext;
import org.smm.archetype.common.event.EventFactory;
import org.smm.archetype.domain.Model;

/**
 *
 *
 * @author Leonardo
 * @since 2025/12/13
 */
public class User extends Model {

    public void create() {
        EventContext.runThenClear(() -> EventFactory.domainEvent(this, Type.USER_CREATED), () -> {

        });
    }

}

package org.smm.archetype.common.event;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.smm.archetype.shared.base.BaseEvent;

/**
 *
 * @author Leonardo
 * @since 2025/12/30
 */
@Getter
@SuperBuilder(setterPrefix = "set")
public class AccessEvent extends BaseEvent<Void> {

    @Override
    public boolean persistent() {
        return false;
    }

}

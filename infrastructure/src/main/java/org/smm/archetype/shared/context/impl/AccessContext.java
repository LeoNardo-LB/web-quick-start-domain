package org.smm.archetype.shared.context.impl;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.smm.archetype.common.event.AccessEvent;

/**
 *
 *
 * @author Leonardo
 * @since 2025/12/30
 */
@Getter
@SuperBuilder(setterPrefix = "set")
public class AccessContext extends EventContext<AccessEvent> {

}

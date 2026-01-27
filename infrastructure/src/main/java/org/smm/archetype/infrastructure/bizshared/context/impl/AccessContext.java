package org.smm.archetype.infrastructure.bizshared.context.impl;

import lombok.RequiredArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.smm.archetype.infrastructure.bizshared.context.Context;

/**
 *
 *
 * @author Leonardo
 * @since 2025/12/30
 */
@SuperBuilder(setterPrefix = "set")
@RequiredArgsConstructor
public class AccessContext implements Context<String> {

    private final String userId;

    @Override
    public String getData() {
        return userId;
    }

}

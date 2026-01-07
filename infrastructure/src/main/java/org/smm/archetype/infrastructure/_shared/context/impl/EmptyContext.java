package org.smm.archetype.infrastructure._shared.context.impl;

import org.smm.archetype.infrastructure._shared.context.Context;

/**
 *
 *
 * @author Leonardo
 * @since 2025/12/31
 */
public class EmptyContext<T> implements Context<T> {

    @Override
    public T getData() {
        return null;
    }

}

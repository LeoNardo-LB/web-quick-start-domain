package org.smm.archetype.shared.context.impl;

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

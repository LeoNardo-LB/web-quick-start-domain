package org.smm.archetype.infrastructure._shared.context;

/**
 *
 *
 * @author Leonardo
 * @since 2025/12/30
 */
public interface Context<T> {

    T getData();

    default Context<T> export() {
        return this;
    }

}

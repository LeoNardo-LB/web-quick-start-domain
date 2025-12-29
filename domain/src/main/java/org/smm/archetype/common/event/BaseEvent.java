package org.smm.archetype.common.event;

import lombok.Getter;
import org.smm.archetype.domain.Serializer;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 *
 * @author Leonardo
 * @since 2025/12/14
 */
@Getter
public abstract class BaseEvent<T> extends Serializer {

    private final Type type;

    private final String message;

    private final Instant expireTime;

    private final T dto;

    private final AtomicBoolean valid = new AtomicBoolean(true);

    protected BaseEvent(Type type, String message, Instant expireTime, T dto) {
        this.type = type;
        this.message = message;
        this.expireTime = expireTime;
        this.dto = dto;
    }

    /**
     * 类型
     */
    @Getter
    public enum Type {
    }

}

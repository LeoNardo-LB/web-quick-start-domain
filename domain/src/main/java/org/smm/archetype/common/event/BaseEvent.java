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
public abstract class BaseEvent extends Serializer {

    private final Source source;

    private final String message;

    private final Instant expireTime;

    private final AtomicBoolean valid = new AtomicBoolean(true);

    public BaseEvent(Source source, String message, Instant expireTime) {
        this.source = source;
        this.message = message;
        this.expireTime = expireTime;
    }

    /**
     * 失效
     */
    public void invalid() {
        valid.set(false);
    }

    @Getter
    public enum Source {
        ACCESS_WEB
    }

}

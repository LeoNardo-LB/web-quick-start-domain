package org.smm.archetype.common.event;

import lombok.Getter;

import java.time.Instant;

/**
 *
 *
 * @author Leonardo
 * @since 2025/12/30
 */
@Getter
public class AccessEvent extends BaseEvent {

    private final String userId;

    public AccessEvent(Source source, String message, Instant expireTime, String userId) {
        super(source, message, expireTime);
        this.userId = userId;
    }

}

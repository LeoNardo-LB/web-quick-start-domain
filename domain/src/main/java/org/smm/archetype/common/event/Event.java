package org.smm.archetype.common.event;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.smm.archetype.domain.Serial;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 *
 * @author Leonardo
 * @since 2025/12/13
 */
@Accessors(chain = true)
public class Event<T> extends Serial {

    volatile AtomicBoolean valid = new AtomicBoolean(true);

    @Getter
    @Setter
    private Source source;

    @Getter
    @Setter
    private Type type;

    @Getter
    @Setter
    private Instant expireTime;

    @Getter
    @Setter
    private T dto;

    /**
     * 事件来源
     */
    public enum Source {
        WEB,
        DOMAIN
    }

    /**
     * 事件类型
     */
    public enum Type {
        USER_CREATED,
        WEB_ACCESS
    }

}

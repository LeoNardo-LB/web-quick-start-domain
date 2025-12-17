package org.smm.archetype.common.event;

import lombok.Getter;
import org.smm.archetype.domain.Serial;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 *
 * @author Leonardo
 * @since 2025/12/14
 */
@Getter
public class Event<T> extends Serial {

    private final Source source;

    private final Type type;

    private final String message;

    private final Instant expireTime;

    private final T dto;

    private volatile AtomicBoolean valid = new AtomicBoolean(true);

    /**
     * 创建事件
     */
    public Event(Source source, Type type, String message, T dto, Instant expireTime) {
        Instant now = Instant.now();
        this.setCreateTime(now);
        this.source = source;
        this.type = type;
        this.expireTime = expireTime;
        this.dto = dto;
        this.message = message;
    }

    /**
     * 复制一份
     */
    public Event<T> copy() {
        Event<T> event = new Event<>(source, type, message, dto, expireTime);
        event.valid = this.valid;
        return event;
    }

    /**
     * 事件来源
     */
    public enum Source {
        ADAPTER,
        DOMAIN
    }

    /**
     * 事件类型
     */
    public enum Type {
        USER_CREATED,
        WEB_ACCESS,

    }

}

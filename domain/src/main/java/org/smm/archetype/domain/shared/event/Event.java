package org.smm.archetype.domain.shared.event;

import lombok.Builder;
import lombok.Getter;
import org.smm.archetype.domain.shared.base.ValueObject;

import java.time.Instant;

/**
 * 事件载体，通过type字段区分事件类型。
 */
@Getter
@Builder(setterPrefix = "set")
public final class Event<T> extends ValueObject {

    /**
     * 事件唯一标识
     */
    private final String eid;

    /**
     * 事件发生时间
     */
    private final Instant occurredOn;

    /**
     * 事件类型
     */
    private final Type type;

    /**
     * 最大重试次数（可选，未设置时使用默认值）
     */
    private final Integer maxRetryTimes;

    /**
     * 载荷
     */
    private final T payload;

}

package org.smm.archetype.domain.bizshared.event;

import lombok.Builder;
import lombok.Getter;
import org.smm.archetype.domain.bizshared.base.ValueObject;

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
     * 事件状态
     */
    private Status status;

    /**
     * 最大重试次数
     */
    private Integer maxRetryTimes;

    /**
     * 载荷
     */
    private T payload;

}

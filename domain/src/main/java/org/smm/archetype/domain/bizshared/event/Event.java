package org.smm.archetype.domain.bizshared.event;

import lombok.Builder;
import lombok.Getter;
import org.smm.archetype.domain.bizshared.base.ValueObject;

import java.time.Instant;

/**
 * 事件封闭类
 *
 * <p>统一的事件载体，通过 type 字段区分事件类型，通过泛型 payload 承载业务数据。
 *
 * <p>设计要点：
 * <ul>
 *   <li>封闭类（final），不允许继承</li>
 *   <li>事件类型通过 Type 枚举区分</li>
 *   <li>业务数据通过 T payload 承载</li>
 *   <li>基础设施层面向 Event，不区分具体类型</li>
 * </ul>
 * @author Leonardo
 * @since 2026/1/30
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

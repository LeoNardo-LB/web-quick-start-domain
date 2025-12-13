package org.smm.archetype.common.event;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * 事件工厂类
 *
 * 提供创建各种类型事件的工厂方法。
 * @author Leonardo
 * @since 2025/12/13
 */
public class EventFactory {

    /**
     * 创建Web访问事件
     * @return Web访问事件实例
     */
    public static Event<Void> webAccessEvent() {
        return new Event<Void>()
                       .setSource(Event.Source.WEB)
                       .setType(Event.Type.WEB_ACCESS)
                       .setExpireTime(Instant.now().plus(30, ChronoUnit.MINUTES)) // 1小时后过期
                       .setDto(null);
    }

    /**
     * 创建Domain事件
     * @param data 数据
     * @param type 事件类型
     * @param <T>  数据类型
     * @return Domain事件实例
     */
    public static <T> Event<T> domainEvent(T data, Event.Type type) {
        return new Event<T>()
                       .setSource(Event.Source.DOMAIN)
                       .setType(type)
                       .setDto(data);
    }

}
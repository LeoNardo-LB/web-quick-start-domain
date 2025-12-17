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
        return new Event<>(Event.Source.ADAPTER, Event.Type.WEB_ACCESS, null, null, Instant.now().plus(1, ChronoUnit.DAYS));
    }

    // /**
    //  * 创建Domain事件
    //  * @param data 数据
    //  * @param type 事件类型
    //  * @param <T>  数据类型
    //  * @return Domain事件实例
    //  */
    // public static <T> Event<T> domainEvent(T data, Event.Type type) {
    //     return new Event<T>()
    //                    .setContextFactory(Event.Source.DOMAIN)
    //                    .setType(type)
    //                    .setDto(data);
    // }

}
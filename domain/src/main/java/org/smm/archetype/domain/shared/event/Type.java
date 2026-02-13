package org.smm.archetype.domain.shared.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 事件类型
 *
 * <p>每个业务事件必须在此枚举中显式注册，未注册的事件无法解析类型，将被丢弃。</p>
 *
 * <p>注意：反序列化功能现在通过 {@link PayloadParserHolder} 获取的 {@link PayloadParser} 实现，
 * 具体的JSON解析实现由Infrastructure层提供，保持Domain层的纯净性。</p>
 */
@Getter
@RequiredArgsConstructor
public enum Type {

    /**
     * 未知事件，解析为泛型对象
     */
    UNKNOW("未知事件", Source.DOMAIN),

    /**
     * 订单创建事件
     */
    ORDER_CREATED("订单创建", Source.DOMAIN),

    /**
     * 订单支付事件
     */
    ORDER_PAID("订单支付", Source.DOMAIN),

    /**
     * 订单取消事件
     */
    ORDER_CANCELLED("订单取消", Source.DOMAIN),

    /**
     * 订单发货事件
     */
    ORDER_SHIPPED("订单发货", Source.DOMAIN),

    ;

    /**
     * 事件描述（中文描述）
     */
    private final String description;

    /**
     * 事件来源
     */
    private final Source source;

    /**
     * 反序列化事件载荷
     *
     * <p>使用 {@link PayloadParserHolder} 中配置的 {@link PayloadParser} 进行解析。</p>
     *
     * @param payload JSON 载荷字符串
     * @param type    目标类型
     * @param <T>     返回类型
     * @return 反序列化后的对象
     */
    public <T> T deserialize(String payload, Class<T> type) {
        return PayloadParserHolder.getParser().parseObject(payload, type);
    }

    /**
     * 反序列化事件载荷（兼容旧API）
     *
     * @param payload JSON 载荷字符串
     * @return 反序列化后的对象（Object类型）
     * @deprecated 使用 {@link #deserialize(String, Class)} 替代，提供类型安全
     */
    @Deprecated
    @SuppressWarnings("unchecked")
    public <T> T deserialize(String payload) {
        // 使用PayloadParser解析为通用Object类型
        return (T) PayloadParserHolder.getParser().parseObject(payload, Object.class);
    }

    /**
     * 序列化事件载荷
     *
     * <p>使用 {@link PayloadParserHolder} 中配置的 {@link PayloadParser} 进行序列化。</p>
     *
     * @param payload 载荷对象
     * @return JSON 字符串
     */
    public String serialize(Object payload) {
        return PayloadParserHolder.getParser().toJSONString(payload);
    }

}

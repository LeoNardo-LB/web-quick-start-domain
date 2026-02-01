package org.smm.archetype.domain.bizshared.event;

import com.alibaba.fastjson2.JSON;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.function.Function;

/**
 * 事件类型
 *
每个业务事件必须在此枚举中显式注册，未注册的事件无法解析类型，将被丢弃。


 */
@Getter
@RequiredArgsConstructor
public enum Type {

    /**
     * 未知事件，解析为JSONObject对象
     */
    UNKNOW("未知事件", Source.DOMAIN, JSON::parseObject),

    /**
     * 订单创建事件
     */
    ORDER_CREATED("订单创建", Source.DOMAIN, null),

    /**
     * 订单支付事件
     */
    ORDER_PAID("订单支付", Source.DOMAIN, null),

    /**
     * 订单取消事件
     */
    ORDER_CANCELLED("订单取消", Source.DOMAIN, null),

    /**
     * 订单发货事件
     */
    ORDER_SHIPPED("订单发货", Source.DOMAIN, null),

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
     * 载荷反序列化器
     */
    private final Function<String, ?> deserializer;

    /**
     * 反序列化事件载荷
     *
    如果当前类型未指定反序列化器（null），则使用 UNKNOW 的反序列化器。
     * @param payload JSON 载荷字符串
     * @return 反序列化后的对象
     */
    @SuppressWarnings("unchecked")
    public <T> T deserialize(String payload) {
        Function<String, ?> actualDeserializer = (deserializer != null) ? deserializer : UNKNOW.deserializer;
        return (T) actualDeserializer.apply(payload);
    }

    /**
     * 序列化事件载荷
     * @param payload 载荷对象
     * @return JSON 字符串
     */
    public String serialize(Object payload) {
        return JSON.toJSONString(payload);
    }

}

package org.smm.archetype.domain.shared.event;

/**
 * 载荷解析器接口
 *
 * <p>用于抽象JSON序列化/反序列化操作，使Domain层不直接依赖具体的JSON库。
 * 具体实现由Infrastructure层提供（如FastJsonPayloadParser）。</p>
 *
 * <p>使用方式：</p>
 * <pre>
 * // 解析JSON字符串为对象
 * MyObject obj = payloadParser.parseObject(jsonString, MyObject.class);
 *
 * // 将对象序列化为JSON字符串
 * String json = payloadParser.toJSONString(obj);
 * </pre>
 *
 * @see org.smm.archetype.infrastructure.shared.event.FastJsonPayloadParser
 */
public interface PayloadParser {

    /**
     * 将JSON字符串解析为指定类型的对象
     *
     * @param json JSON字符串
     * @param type 目标类型
     * @param <T>  返回类型
     * @return 解析后的对象
     */
    <T> T parseObject(String json, Class<T> type);

    /**
     * 将对象序列化为JSON字符串
     *
     * @param object 要序列化的对象
     * @return JSON字符串
     */
    String toJSONString(Object object);

}

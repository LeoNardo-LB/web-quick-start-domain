package org.smm.archetype.infrastructure.shared.event;

import com.alibaba.fastjson2.JSON;
import org.smm.archetype.domain.shared.event.PayloadParser;
import org.springframework.stereotype.Component;

/**
 * 基于 FastJSON2 的载荷解析器实现
 *
 * <p>使用 FastJSON2 提供JSON序列化/反序列化功能。
 * 此实现位于Infrastructure层，保持Domain层的纯净性。</p>
 *
 * @see org.smm.archetype.domain.shared.event.PayloadParser
 * @see org.smm.archetype.domain.shared.event.PayloadParserHolder
 */
@Component
public class FastJsonPayloadParser implements PayloadParser {

    @Override
    public <T> T parseObject(String json, Class<T> type) {
        return JSON.parseObject(json, type);
    }

    @Override
    public String toJSONString(Object object) {
        return JSON.toJSONString(object);
    }

}

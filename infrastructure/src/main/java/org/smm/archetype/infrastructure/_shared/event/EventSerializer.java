package org.smm.archetype.infrastructure._shared.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.smm.archetype.domain._shared.base.DomainEvent;

/**
 * 事件序列化器
 *
 * <p>负责领域事件与JSON字符串之间的转换。
 *
 * @author Leonardo
 * @since 2026/01/09
 */
@Slf4j
public class EventSerializer {

    private final ObjectMapper objectMapper;

    public EventSerializer() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * 将领域事件序列化为JSON字符串
     * @param event 领域事件
     * @return JSON字符串
     */
    public String serialize(DomainEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize event: {}", event.getEventId(), e);
            throw new RuntimeException("Event serialization failed", e);
        }
    }

    /**
     * 将JSON字符串反序列化为领域事件
     * @param json JSON字符串
     * @param eventType 事件类型
     * @return 领域事件
     */
    public DomainEvent deserialize(String json, String eventType) {
        try {
            Class<?> eventClass = Class.forName(eventType);
            return (DomainEvent) objectMapper.readValue(json, eventClass);
        } catch (Exception e) {
            log.error("Failed to deserialize event: type={}", eventType, e);
            throw new RuntimeException("Event deserialization failed", e);
        }
    }

    /**
     * 获取ObjectMapper实例
     * @return ObjectMapper
     */
    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }
}

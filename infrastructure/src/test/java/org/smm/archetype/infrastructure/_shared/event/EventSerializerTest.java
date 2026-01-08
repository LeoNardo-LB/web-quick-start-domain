package org.smm.archetype.infrastructure._shared.event;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * 事件序列化器测试
 *
 * @author Leonardo
 * @since 2026/01/09
 */
@Slf4j
class EventSerializerTest {

    @Test
    void testObjectMapperCreation() {
        // Given: 创建EventSerializer实例
        EventSerializer eventSerializer = new EventSerializer();

        // When: 获取ObjectMapper
        assertNotNull(eventSerializer.getObjectMapper());

        // Then: 验证ObjectMapper不为null
        log.info("EventSerializer ObjectMapper created successfully");
    }

}

package org.smm.archetype.infrastructure._shared.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.smm.archetype.domain._shared.event.ConsumeStatus;
import org.smm.archetype.domain._shared.event.EventPriority;
import org.smm.archetype.domain._shared.event.EventStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * DO转换工具类测试
 * @author Leonardo
 * @since 2026/01/09
 */
@DisplayName("DO转换工具类测试")
class DoConverterUtilsTest {

    @Test
    @DisplayName("应该成功转换有效的事件状态")
    void should_convert_valid_event_status() {
        // Given
        String status = "PUBLISHED";

        // When
        EventStatus result = DoConverterUtils.convertEventStatus(status);

        // Then
        assertEquals(EventStatus.PUBLISHED, result);
    }

    @Test
    @DisplayName("应该对无效的事件状态使用默认值")
    void should_use_default_for_invalid_event_status() {
        // Given
        String status = "INVALID_STATUS";

        // When
        EventStatus result = DoConverterUtils.convertEventStatus(status);

        // Then
        assertEquals(EventStatus.CREATED, result);
    }

    @Test
    @DisplayName("应该对空的事件状态使用默认值")
    void should_use_default_for_null_event_status() {
        // Given
        String status = null;

        // When
        EventStatus result = DoConverterUtils.convertEventStatus(status);

        // Then
        assertEquals(EventStatus.CREATED, result);
    }

    @Test
    @DisplayName("应该支持不区分大小写的状态转换")
    void should_support_case_insensitive_status_conversion() {
        // Given
        String status = "published"; // 小写

        // When
        EventStatus result = DoConverterUtils.convertEventStatus(status);

        // Then
        assertEquals(EventStatus.PUBLISHED, result);
    }

    @Test
    @DisplayName("应该成功转换有效的事件优先级")
    void should_convert_valid_event_priority() {
        // Given
        String priority = "HIGH";

        // When
        EventPriority result = DoConverterUtils.convertEventPriority(priority);

        // Then
        assertEquals(EventPriority.HIGH, result);
    }

    @Test
    @DisplayName("应该成功转换有效的消费状态")
    void should_convert_valid_consume_status() {
        // Given
        String status = "CONSUMED";

        // When
        ConsumeStatus result = DoConverterUtils.convertConsumeStatus(status);

        // Then
        assertEquals(ConsumeStatus.CONSUMED, result);
    }

    @Test
    @DisplayName("应该对无效的消费状态使用默认值")
    void should_use_default_for_invalid_consume_status() {
        // Given
        String status = "INVALID";

        // When
        ConsumeStatus result = DoConverterUtils.convertConsumeStatus(status);

        // Then
        assertEquals(ConsumeStatus.READY, result);
    }

    @Test
    @DisplayName("应该成功转换有效的Long值")
    void should_convert_valid_long() {
        // Given
        String value = "12345";

        // When
        Long result = DoConverterUtils.convertLong(value, 0L);

        // Then
        assertEquals(12345L, result);
    }

    @Test
    @DisplayName("应该对无效的Long值使用默认值")
    void should_use_default_for_invalid_long() {
        // Given
        String value = "invalid";

        // When
        Long result = DoConverterUtils.convertLong(value, 0L);

        // Then
        assertEquals(0L, result);
    }

    @Test
    @DisplayName("应该成功转换有效的Integer值")
    void should_convert_valid_integer() {
        // Given
        String value = "100";

        // When
        Integer result = DoConverterUtils.convertInteger(value, 0);

        // Then
        assertEquals(100, result);
    }

    @Test
    @DisplayName("应该成功转换有效的Boolean值")
    void should_convert_valid_boolean() {
        // Given
        String value = "true";

        // When
        Boolean result = DoConverterUtils.convertBoolean(value, false);

        // Then
        assertTrue(result);
    }

    @Test
    @DisplayName("应该对无效的Boolean值使用默认值")
    void should_use_default_for_invalid_boolean() {
        // Given
        String value = null;

        // When
        Boolean result = DoConverterUtils.convertBoolean(value, false);

        // Then
        assertFalse(result);
    }

}

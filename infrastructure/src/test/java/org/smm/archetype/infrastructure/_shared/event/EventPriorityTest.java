package org.smm.archetype.infrastructure._shared.event;

import org.junit.jupiter.api.Test;
import org.smm.archetype.domain._shared.event.EventPriority;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 事件优先级枚举测试
 *
 * @author Leonardo
 * @since 2026/01/09
 */
class EventPriorityTest {

    @Test
    void testHighPriority() {
        EventPriority high = EventPriority.HIGH;
        assertTrue(high.isHigh());
        assertFalse(high.isLow());
        assertEquals("高优先级", high.getDescription());
    }

    @Test
    void testLowPriority() {
        EventPriority low = EventPriority.LOW;
        assertTrue(low.isLow());
        assertFalse(low.isHigh());
        assertEquals("低优先级", low.getDescription());
    }

    @Test
    void testEnumValues() {
        EventPriority[] values = EventPriority.values();
        assertEquals(2, values.length);
        assertEquals(EventPriority.HIGH, values[0]);
        assertEquals(EventPriority.LOW, values[1]);
    }

}

package org.smm.archetype.infrastructure._shared.event;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.smm.archetype.domain.example.order.event.OrderCreatedEvent;
import org.smm.archetype.domain.example.order.model.Money;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 事件序列化器测试
 *
 * @author Leonardo
 * @since 2026/01/09
 */
class EventSerializerTest {

    private EventSerializer eventSerializer;

    @BeforeEach
    void setUp() {
        eventSerializer = new EventSerializer();
    }

    @Test
    void testSerializeAndDeserialize() {
        // 创建测试事件
        OrderCreatedEvent event = new OrderCreatedEvent(
                123L,
                456L,
                Money.of(BigDecimal.valueOf(100.00), "CNY")
        );

        // 序列化
        String json = eventSerializer.serialize(event);
        assertNotNull(json);
        assertFalse(json.isEmpty());

        System.out.println("Serialized JSON: " + json);

        // 反序列化
        OrderCreatedEvent deserialized = (OrderCreatedEvent) eventSerializer.deserialize(
                json,
                OrderCreatedEvent.class.getSimpleName()
        );

        assertNotNull(deserialized);
        assertEquals(event.getOrderId(), deserialized.getOrderId());
        assertEquals(event.getCustomerId(), deserialized.getCustomerId());
    }

}

package org.smm.archetype.infrastructure._shared.event;

import lombok.extern.slf4j.Slf4j;
import org.smm.archetype.domain._shared.base.DomainEvent;
import org.smm.archetype.domain._shared.event.EventStore;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 内存事件存储实现
 *
 * <p>这是一个简单的事件存储实现，仅用于演示。
 * 生产环境应该使用数据库或其他持久化存储。
 * @author Leonardo
 * @since 2025/12/30
 */
@Slf4j
@Repository
public class InMemoryEventStore implements EventStore {

    // 按聚合根ID存储事件
    private final ConcurrentHashMap<String, List<DomainEvent>> eventStore = new ConcurrentHashMap<>();

    @Override
    public void append(List<DomainEvent> events) {
        log.debug("Appending {} events", events.size());

        for (DomainEvent event : events) {
            // 使用事件ID作为key（简化实现）
            String aggregateId = event.getEventId();
            eventStore.computeIfAbsent(aggregateId, k -> new ArrayList<>()).add(event);
        }

        log.debug("Events appended successfully, total events: {}", eventStore.size());
    }

    @Override
    public List<DomainEvent> getEvents(String aggregateId) {
        return new ArrayList<>(eventStore.getOrDefault(aggregateId, new ArrayList<>()));
    }

    @Override
    public List<DomainEvent> getEvents(String aggregateId, long fromVersion) {
        List<DomainEvent> allEvents = getEvents(aggregateId);
        // 简化实现：返回所有事件
        // 生产环境应该根据version过滤
        return allEvents;
    }

}

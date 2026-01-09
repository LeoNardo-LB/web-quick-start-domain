package org.smm.archetype.infrastructure._shared.event;

import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smm.archetype.domain._shared.base.DomainEvent;
import org.smm.archetype.domain._shared.event.EventStatus;
import org.smm.archetype.domain._shared.event.EventStore;
import org.smm.archetype.infrastructure._shared.generated.repository.entity.EventPublishDO;
import org.smm.archetype.infrastructure._shared.generated.repository.mapper.EventPublishMapper;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 基于数据库的事件存储实现
 *
 * <p>将领域事件持久化到数据库，支持事件溯源和重放。
 *
 * <p>实现说明：
 * <ul>
 *   <li>使用event_publish表存储事件</li>
 *   <li>每个事件包含 eventId, aggregateId, aggregateType, data 等信息</li>
 *   <li>支持按聚合根ID查询事件流</li>
 *   <li>保证事件的顺序性（通过occurredOn排序）</li>
 * </ul>
 *
 * @author Leonardo
 * @since 2026/01/09
 */
@Slf4j
@Repository
@RequiredArgsConstructor
@Primary
public class DbEventStore implements EventStore {

    private final EventPublishMapper eventPublishMapper;
    private final EventSerializer eventSerializer;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void append(List<DomainEvent> events) {
        if (events == null || events.isEmpty()) {
            return;
        }

        log.debug("Appending {} events to database", events.size());

        for (DomainEvent event : events) {
            EventPublishDO eventDO = toEventPublishDO(event);
            eventPublishMapper.insert(eventDO);
            log.debug("Event saved: eventId={}, type={}, aggregateId={}",
                    event.getEventId(), event.getEventType(), event.getAggregateId());
        }

        log.info("Successfully appended {} events to database", events.size());
    }

    @Override
    public List<DomainEvent> getEvents(String aggregateId) {
        log.debug("Loading events for aggregate: {}", aggregateId);

        List<EventPublishDO> eventDOs = eventPublishMapper.selectListByQuery(
                QueryWrapper.create()
                        .where("aggregate_id = ?", aggregateId)
                        .orderBy("occurred_on", true)
        );

        List<DomainEvent> events = new ArrayList<>();
        for (EventPublishDO eventDO : eventDOs) {
            DomainEvent event = eventSerializer.deserialize(
                    eventDO.getData(),
                    eventDO.getType()
            );
            events.add(event);
        }

        log.debug("Loaded {} events for aggregate: {}", events.size(), aggregateId);
        return events;
    }

    @Override
    public List<DomainEvent> getEvents(String aggregateId, long fromVersion) {
        log.debug("Loading events for aggregate: {} from version: {}", aggregateId, fromVersion);

        List<EventPublishDO> eventDOs = eventPublishMapper.selectListByQuery(
                QueryWrapper.create()
                        .where("aggregate_id = ? AND step >= ?", aggregateId, fromVersion)
                        .orderBy("occurred_on", true)
        );

        List<DomainEvent> events = new ArrayList<>();
        for (EventPublishDO eventDO : eventDOs) {
            DomainEvent event = eventSerializer.deserialize(
                    eventDO.getData(),
                    eventDO.getType()
            );
            events.add(event);
        }

        log.debug("Loaded {} events for aggregate: {} from version: {}",
                events.size(), aggregateId, fromVersion);
        return events;
    }

    /**
     * 将领域事件转换为EventPublishDO
     * @param event 领域事件
     * @return EventPublishDO
     */
    private EventPublishDO toEventPublishDO(DomainEvent event) {
        EventPublishDO eventDO = new EventPublishDO();
        eventDO.setEventId(event.getEventId());
        eventDO.setAggregateId(event.getAggregateId());
        eventDO.setAggregateType(event.getAggregateType());
        eventDO.setType(event.getEventTypeName());
        eventDO.setPriority(event.getPriority().name());
        eventDO.setOccurredOn(event.getOccurredOn());
        eventDO.setData(eventSerializer.serialize(event));
        eventDO.setStatus(EventStatus.CREATED.name());
        eventDO.setMaxRetryTimes(event.getMaxRetryTimes());
        eventDO.setStep(0); // 初始步骤为0
        eventDO.setSource("DOMAIN_EVENT"); // 来源标识
        return eventDO;
    }

    /**
     * 获取指定状态的事件
     * @param status 事件状态
     * @return 事件列表
     */
    public List<EventPublishDO> getEventsByStatus(EventStatus status) {
        return eventPublishMapper.selectListByQuery(
                QueryWrapper.create()
                        .where("status = ?", status.name())
                        .orderBy("occurred_on", true)
        );
    }

    /**
     * 更新事件状态
     * @param eventId 事件ID
     * @param status 新状态
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateEventStatus(String eventId, EventStatus status) {
        EventPublishDO eventDO = new EventPublishDO();
        eventDO.setStatus(status.name());

        eventPublishMapper.updateByQuery(
                eventDO,
                QueryWrapper.create().where("event_id = ?", eventId)
        );

        log.debug("Updated event status: eventId={}, status={}", eventId, status);
    }

}

package org.smm.archetype.infrastructure._shared.event.publisher;

import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smm.archetype.domain._shared.base.DomainEvent;
import org.smm.archetype.domain._shared.event.EventPublisher;
import org.smm.archetype.domain._shared.event.EventStatus;
import org.smm.archetype.infrastructure._shared.event.EventSerializer;
import org.smm.archetype.infrastructure._shared.generated.entity.EventPublishDO;
import org.smm.archetype.infrastructure._shared.generated.mapper.EventPublishMapper;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 抽象事件发布器
 *
 * <p>提供事件发布的通用流程模板，处理事件持久化和状态管理。
 *
 * <p>发布流程：
 * <ol>
 *   <li>持久化事件到数据库（状态为 CREATED）</li>
 *   <li>调用 doPublish 方法进行实际发布</li>
 *   <li>根据发布结果更新状态（PUBLISHED 或保持 CREATED）</li>
 * </ol>
 * @param <T> 领域事件类型
 * @author Leonardo
 * @since 2026/01/09
 */
@Slf4j
@RequiredArgsConstructor
public abstract class AbstractEventPublisher<T extends DomainEvent> implements EventPublisher {

    protected final EventPublishMapper eventPublishMapper;
    protected final EventSerializer    eventSerializer;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publish(List<DomainEvent> events) {
        if (events == null || events.isEmpty()) {
            return;
        }

        log.info("Publishing {} events", events.size());

        for (DomainEvent event : events) {
            try {
                // 1. 持久化事件到数据库
                EventPublishDO eventDO = saveEvent(event);

                // 2. 调用子类实现的发布逻辑
                doPublish(event, eventDO);

            } catch (Exception e) {
                log.error("Error publishing event: eventId={}", event.getEventId(), e);
                // 保持 CREATED 状态，由定时任务重试
            }
        }
    }

    /**
     * 保存事件到数据库
     * @param event 领域事件
     * @return EventPublishDO
     */
    protected EventPublishDO saveEvent(DomainEvent event) {
        EventPublishDO eventDO = toEventPublishDO(event);
        eventPublishMapper.insert(eventDO);
        log.debug("Event saved: eventId={}, status={}", event.getEventId(), eventDO.getStatus());
        return eventDO;
    }

    /**
     * 执行实际的事件发布逻辑
     *
     * <p>由子类实现具体的发布方式（Kafka、Spring事件等）。
     * @param event   领域事件
     * @param eventDO 事件DO对象
     * @throws Exception 发布异常
     */
    protected abstract void doPublish(DomainEvent event, EventPublishDO eventDO) throws Exception;

    /**
     * 更新事件状态
     * @param eventId 事件ID
     * @param status  新状态
     */
    protected void updateEventStatus(String eventId, EventStatus status) {
        try {
            EventPublishDO eventDO = new EventPublishDO();
            eventDO.setStatus(status.name());

            eventPublishMapper.updateByQuery(
                    eventDO,
                    QueryWrapper.create()
                            .where("event_id = ?", eventId)
            );
            log.debug("Event status updated: eventId={}, status={}", eventId, status);
        } catch (Exception e) {
            log.error("Failed to update event status: eventId={}, status={}", eventId, status, e);
        }
    }

    /**
     * 将领域事件转换为 EventPublishDO
     * @param event 领域事件
     * @return EventPublishDO
     */
    protected EventPublishDO toEventPublishDO(DomainEvent event) {
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
        eventDO.setStep(0);
        eventDO.setSource(getClass().getSimpleName());
        return eventDO;
    }

}

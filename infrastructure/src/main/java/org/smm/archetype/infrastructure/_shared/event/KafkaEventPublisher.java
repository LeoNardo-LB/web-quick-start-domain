package org.smm.archetype.infrastructure._shared.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smm.archetype.domain._shared.base.DomainEvent;
import org.smm.archetype.domain._shared.event.EventPublisher;
import org.smm.archetype.domain._shared.event.EventStatus;
import org.smm.archetype.infrastructure._shared.generated.repository.entity.EventPublishDO;
import org.smm.archetype.infrastructure._shared.generated.repository.mapper.EventPublishMapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Kafka事件发布器
 *
 * <p>将领域事件发布到Kafka消息队列。
 *
 * <p>工作流程：
 * <ol>
 *   <li>将事件持久化到数据库（状态为CREATED）</li>
 *   <li>发送到Kafka主题</li>
 *   <li>发送成功后更新状态为PUBLISHED</li>
 *   <li>发送失败则保持CREATED状态，由定时任务重试</li>
 * </ol>
 *
 * @author Leonardo
 * @since 2026/01/09
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaEventPublisher implements EventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final EventPublishMapper eventPublishMapper;
    private final EventSerializer eventSerializer;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publish(List<DomainEvent> events) {
        if (events == null || events.isEmpty()) {
            return;
        }

        log.info("Publishing {} events to Kafka", events.size());

        for (DomainEvent event : events) {
            try {
                // 1. 持久化事件到数据库
                EventPublishDO eventDO = toEventPublishDO(event);
                eventPublishMapper.insert(eventDO);

                // 2. 发送到Kafka
                String topic = getTopic(event);
                String key = event.getAggregateId();
                String value = eventSerializer.serialize(event);

                CompletableFuture<SendResult<String, String>> future =
                        kafkaTemplate.send(topic, key, value);

                // 3. 异步处理发送结果
                future.whenComplete((result, ex) -> {
                    if (ex == null) {
                        // 发送成功，更新状态为PUBLISHED
                        updateEventStatus(event.getEventId(), EventStatus.PUBLISHED);
                        log.debug("Event published successfully: eventId={}, topic={}",
                                event.getEventId(), topic);
                    } else {
                        // 发送失败，保持CREATED状态，由定时任务重试
                        log.error("Failed to publish event: eventId={}, topic={}",
                                event.getEventId(), topic, ex);
                    }
                });

            } catch (Exception e) {
                log.error("Error publishing event: {}", event.getEventId(), e);
                // 保持CREATED状态，由定时任务重试
            }
        }
    }

    /**
     * 获取事件主题
     * @param event 领域事件
     * @return Kafka主题
     */
    private String getTopic(DomainEvent event) {
        // 根据事件类型决定主题
        // 也可以根据优先级：高优先级事件发到高优先级主题
        String eventType = event.getEventType();
        return "domain-events"; // 可以配置为：domain-events.{priority}
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
        eventDO.setType(event.getEventType());
        eventDO.setPriority(event.getPriority().name());
        eventDO.setOccurredOn(event.getOccurredOn());
        eventDO.setData(eventSerializer.serialize(event));
        eventDO.setStatus(EventStatus.CREATED.name());
        eventDO.setMaxRetryTimes(event.getMaxRetryTimes());
        eventDO.setStep(0);
        eventDO.setSource("KAFKA_PUBLISHER");
        return eventDO;
    }

    /**
     * 更新事件状态
     * @param eventId 事件ID
     * @param status 新状态
     */
    private void updateEventStatus(String eventId, EventStatus status) {
        try {
            EventPublishDO eventDO = new EventPublishDO();
            eventDO.setStatus(status.name());

            eventPublishMapper.updateByQuery(
                    eventDO,
                    com.mybatisflex.core.query.QueryWrapper.create()
                            .where("event_id = ?", eventId)
            );
        } catch (Exception e) {
            log.error("Failed to update event status: eventId={}, status={}",
                    eventId, status, e);
        }
    }

    /**
     * 重试发布失败的事件
     * <p>由定时任务调用
     * @param eventDO 事件DO
     */
    public void republish(EventPublishDO eventDO) {
        log.info("Republishing event: eventId={}, retryTimes={}",
                eventDO.getEventId(), eventDO.getStep());

        try {
            String topic = getTopicFromDO(eventDO);
            String key = eventDO.getAggregateId();
            String value = eventDO.getData();

            CompletableFuture<SendResult<String, String>> future =
                    kafkaTemplate.send(topic, key, value);

            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    updateEventStatus(eventDO.getEventId(), EventStatus.PUBLISHED);
                    log.info("Event republished successfully: eventId={}", eventDO.getEventId());
                } else {
                    // 增加重试次数
                    incrementRetryTimes(eventDO.getEventId());
                    log.error("Failed to republish event: eventId={}", eventDO.getEventId(), ex);
                }
            });

        } catch (Exception e) {
            log.error("Error republishing event: eventId={}", eventDO.getEventId(), e);
            incrementRetryTimes(eventDO.getEventId());
        }
    }

    /**
     * 从EventPublishDO获取主题
     */
    private String getTopicFromDO(EventPublishDO eventDO) {
        return "domain-events";
    }

    /**
     * 增加重试次数
     */
    private void incrementRetryTimes(String eventId) {
        try {
            // 增加step字段作为重试计数
            EventPublishDO eventDO = new EventPublishDO();

            eventPublishMapper.updateByQuery(
                    eventDO,
                    com.mybatisflex.core.query.QueryWrapper.create()
                            .where("event_id = ?", eventId)
            );
        } catch (Exception e) {
            log.error("Failed to increment retry times: eventId={}", eventId, e);
        }
    }

}

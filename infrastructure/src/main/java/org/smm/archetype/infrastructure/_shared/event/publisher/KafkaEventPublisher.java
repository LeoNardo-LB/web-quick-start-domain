package org.smm.archetype.infrastructure._shared.event.publisher;

import lombok.extern.slf4j.Slf4j;
import org.smm.archetype.domain._shared.base.DomainEvent;
import org.smm.archetype.domain._shared.event.EventStatus;
import org.smm.archetype.infrastructure._shared.event.EventSerializer;
import org.smm.archetype.infrastructure._shared.generated.entity.EventPublishDO;
import org.smm.archetype.infrastructure._shared.generated.mapper.EventPublishMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Async;

import java.util.concurrent.CompletableFuture;

/**
 * Kafka 事件发布器
 *
 * <p>将领域事件发布到 Kafka 消息队列，支持异步处理。
 *
 * <p>工作流程：
 * <ol>
 *   <li>继承 AbstractEventPublisher，事件持久化到数据库（状态为 CREATED）</li>
 *   <li>使用 IO 线程池异步发送到 Kafka 主题</li>
 *   <li>发送成功后更新状态为 PUBLISHED</li>
 *   <li>发送失败则保持 CREATED 状态，由定时任务重试</li>
 * </ol>
 *
 * <p>适用于：
 * <ul>
 *   <li>分布式应用</li>
 *   <li>生产环境</li>
 *   <li>需要事件解耦的场景</li>
 * </ul>
 *
 * @author Leonardo
 * @since 2026-01-10
 */
@Slf4j
@ConditionalOnProperty(
        name = "middleware.event.publisher.type",
        havingValue = "kafka"
)
public class KafkaEventPublisher extends AbstractEventPublisher<DomainEvent> {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public KafkaEventPublisher(
            KafkaTemplate<String, String> kafkaTemplate,
            EventPublishMapper eventPublishMapper,
            EventSerializer eventSerializer) {
        super(eventPublishMapper, eventSerializer);
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    @Async("ioTaskExecutor")
    protected void doPublish(DomainEvent event, EventPublishDO eventDO) throws Exception {
        String topic = getTopic(event);
        String key = event.getAggregateId();
        String value = eventSerializer.serialize(event);

        log.debug("Sending event to Kafka: topic={}, eventId={}", topic, event.getEventId());

        CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(topic, key, value);

        // 异步处理发送结果
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                // 发送成功，更新状态为 PUBLISHED
                updateEventStatus(event.getEventId(), EventStatus.PUBLISHED);
                log.debug("Event published successfully: eventId={}, topic={}",
                        event.getEventId(), topic);
            } else {
                // 发送失败，保持 CREATED 状态，由定时任务重试
                log.error("Failed to publish event: eventId={}, topic={}",
                        event.getEventId(), topic, ex);
            }
        });
    }

    /**
     * 获取事件主题
     *
     * @param event 领域事件
     * @return Kafka 主题
     */
    private String getTopic(DomainEvent event) {
        // 根据事件类型决定主题
        // 也可以根据优先级：高优先级事件发到高优先级主题
        String eventTypeName = event.getEventTypeName();
        return "domain-events"; // 可以配置为：domain-events.{priority}
    }

}

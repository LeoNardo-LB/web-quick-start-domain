package org.smm.archetype.infrastructure.bizshared.event.publisher;

import lombok.extern.slf4j.Slf4j;
import org.smm.archetype.domain.bizshared.base.DomainEvent;
import org.smm.archetype.domain.bizshared.event.EventStatus;
import org.smm.archetype.infrastructure.bizshared.dal.generated.entity.EventPublishDO;
import org.smm.archetype.infrastructure.bizshared.dal.generated.mapper.EventPublishMapper;
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

    private final KafkaTemplate<String, DomainEvent> kafkaTemplate;
    private final String                             topicPrefix;

    public KafkaEventPublisher(
            KafkaTemplate<String, DomainEvent> kafkaTemplate,
            EventPublishMapper eventPublishMapper,
            String topicPrefix) {
        super(eventPublishMapper);
        this.kafkaTemplate = kafkaTemplate;
        this.topicPrefix = topicPrefix;
    }

    @Override
    @Async("ioTaskExecutor")
    protected void doPublish(DomainEvent event, EventPublishDO eventDO) throws Exception {
        String topic = getTopic(event);
        String key = event.getAggregateId();

        log.debug("Sending event to Kafka: topic={}, eventId={}", topic, event.getEventId());

        // Spring Kafka会自动序列化DomainEvent并添加__TypeId__ header
        CompletableFuture<SendResult<String, DomainEvent>> future = kafkaTemplate.send(topic, key, event);

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
        String eventTypeName = event.getEventTypeName();
        return topicPrefix + eventTypeName;
    }

    /**
     * 序列化领域事件为JSON字符串
     *
     * <p>实现父类的抽象方法，用于持久化事件到数据库。
     * @param event 领域事件
     * @return JSON字符串
     */
    @Override
    protected String serializeEvent(DomainEvent event) {
        // 注意：这里仍然需要序列化为JSON用于数据库持久化
        // 但Kafka发送使用Spring Kafka的自动序列化
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
            return mapper.writeValueAsString(event);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            log.error("Failed to serialize event: {}", event.getEventId(), e);
            throw new RuntimeException("Event serialization failed", e);
        }
    }

}

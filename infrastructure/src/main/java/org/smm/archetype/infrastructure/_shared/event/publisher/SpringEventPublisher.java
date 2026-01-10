package org.smm.archetype.infrastructure._shared.event.publisher;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.smm.archetype.domain._shared.base.DomainEvent;
import org.smm.archetype.domain._shared.event.EventStatus;
import org.smm.archetype.infrastructure._shared.event.DomainSpringEvent;
import org.smm.archetype.infrastructure._shared.generated.repository.entity.EventPublishDO;
import org.smm.archetype.infrastructure._shared.generated.repository.mapper.EventPublishMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;

/**
 * Spring 事件发布器
 *
 * <p>使用 Spring 的 ApplicationEventPublisher 发布领域事件。
 *
 * <p>工作流程：
 * <ol>
 *   <li>继承 AbstractEventPublisher，事件持久化到数据库（状态为 CREATED）</li>
 *   <li>使用 ApplicationEventPublisher 异步发布 Spring 事件</li>
 *   <li>发布成功后更新状态为 PUBLISHED</li>
 *   <li>发布失败则保持 CREATED 状态，由定时任务重试</li>
 * </ol>
 *
 * <p>适用于：
 * <ul>
 *   <li>单机应用</li>
 *   <li>开发测试环境</li>
 *   <li>Kafka 的降级方案</li>
 * </ul>
 *
 * @author Leonardo
 * @since 2026-01-10
 */
@Slf4j
@ConditionalOnProperty(
        name = "middleware.event.publisher.type",
        havingValue = "spring"
)
public class SpringEventPublisher extends AbstractEventPublisher<DomainEvent> {

    private final ApplicationEventPublisher applicationEventPublisher;
    private final ObjectMapper objectMapper;

    public SpringEventPublisher(
            ApplicationEventPublisher applicationEventPublisher,
            EventPublishMapper eventPublishMapper) {
        super(eventPublishMapper);
        this.applicationEventPublisher = applicationEventPublisher;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @Override
    @Async("ioTaskExecutor")
    protected void doPublish(DomainEvent event, EventPublishDO eventDO) throws Exception {
        try {
            // 发布 Spring 事件
            DomainSpringEvent springEvent = new DomainSpringEvent(this, event);
            applicationEventPublisher.publishEvent(springEvent);

            // 发布成功，更新状态为 PUBLISHED
            updateEventStatus(event.getEventId(), EventStatus.PUBLISHED);
            log.debug("Event published successfully via Spring: eventId={}", event.getEventId());

        } catch (Exception e) {
            // 发布失败，保持 CREATED 状态，由定时任务重试
            log.error("Failed to publish event via Spring: eventId={}", event.getEventId(), e);
            throw e;
        }
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
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize event: {}", event.getEventId(), e);
            throw new RuntimeException("Event serialization failed", e);
        }
    }

}

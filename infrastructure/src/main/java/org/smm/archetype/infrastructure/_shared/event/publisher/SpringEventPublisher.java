package org.smm.archetype.infrastructure._shared.event.publisher;

import lombok.extern.slf4j.Slf4j;
import org.smm.archetype.domain._shared.base.DomainEvent;
import org.smm.archetype.domain._shared.event.EventStatus;
import org.smm.archetype.infrastructure._shared.event.DomainSpringEvent;
import org.smm.archetype.infrastructure._shared.event.EventSerializer;
import org.smm.archetype.infrastructure._shared.generated.repository.entity.EventPublishDO;
import org.smm.archetype.infrastructure._shared.generated.repository.mapper.EventPublishMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

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
 * @author Leonardo
 * @since 2026/01/09
 */
@Slf4j
@Component
public class SpringEventPublisher extends AbstractEventPublisher<DomainEvent> {

    private final ApplicationEventPublisher applicationEventPublisher;

    public SpringEventPublisher(
            ApplicationEventPublisher applicationEventPublisher,
            EventPublishMapper eventPublishMapper,
            EventSerializer eventSerializer) {
        super(eventPublishMapper, eventSerializer);
        this.applicationEventPublisher = applicationEventPublisher;
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

}

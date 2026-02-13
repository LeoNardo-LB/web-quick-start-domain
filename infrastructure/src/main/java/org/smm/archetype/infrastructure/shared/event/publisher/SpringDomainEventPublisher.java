package org.smm.archetype.infrastructure.shared.event.publisher;

import lombok.extern.slf4j.Slf4j;
import org.smm.archetype.domain.shared.event.Event;
import org.smm.archetype.infrastructure.shared.dal.generated.mapper.EventMapper;
import org.springframework.context.ApplicationEventPublisher;

/**
 * Spring 事件发布器
 *
使用 Spring 的 ApplicationEventPublisher 发布事件。
 *
工作流程：
 * <ol>
 *   <li>继承 DomainEventCollectPublisher，事件持久化到数据库（状态为 CREATED）</li>
 *   <li>使用 ApplicationEventPublisher 异步发布 Spring 事件</li>
 *   <li>发布成功后更新状态为 PUBLISHED</li>
 *   <li>发布失败则保持 CREATED 状态，由定时任务重试</li>
 * </ol>
 *
适用于：
 * <ul>
 *   <li>单机应用</li>
 *   <li>开发测试环境</li>
 *   <li>Kafka 的降级方案</li>
 * </ul>


 */
@Slf4j
public class SpringDomainEventPublisher extends DomainEventCollectPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    public SpringDomainEventPublisher(ApplicationEventPublisher applicationEventPublisher, EventMapper eventMapper) {
        super(eventMapper);
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    protected void doPublish(Event<?> event) {
        applicationEventPublisher.publishEvent(event);
    }

}

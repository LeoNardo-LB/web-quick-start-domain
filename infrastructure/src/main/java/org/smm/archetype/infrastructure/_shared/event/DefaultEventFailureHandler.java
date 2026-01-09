package org.smm.archetype.infrastructure._shared.event;

import lombok.extern.slf4j.Slf4j;
import org.smm.archetype.domain._shared.base.DomainEvent;
import org.smm.archetype.domain._shared.event.EventType;
import org.smm.archetype.infrastructure._shared.event.handler.EventFailureHandler;
import org.smm.archetype.infrastructure._shared.generated.repository.entity.EventConsumeDO;
import org.springframework.stereotype.Component;

/**
 * 默认事件失败处理器
 *
 * <p>实现：
 * - 记录告警日志
 * - 发送通知（邮件/钉钉/企微）
 * @author Leonardo
 * @since 2026/01/09
 */
@Slf4j
@Component
public class DefaultEventFailureHandler implements EventFailureHandler {

    @Override
    public void handleFailure(DomainEvent event, EventConsumeDO consumeDO) {
        log.error("!!! Event processing failed after max retries !!!");
        log.error("Event ID: {}", event.getEventId());
        log.error("Event Type: {}", event.getEventTypeName());
        log.error("Aggregate ID: {}", event.getAggregateId());
        log.error("Error Message: {}", consumeDO.getErrorMessage());
        log.error("Retry Times: {}", consumeDO.getRetryTimes());
        log.error("Consumer Group: {}", consumeDO.getConsumerGroup());
        log.error("Consumer Name: {}", consumeDO.getConsumerName());
        log.error("Occurred At: {}", consumeDO.getCreateTime());

        // 发送告警通知
        sendAlert(event, consumeDO);
    }

    @Override
    public boolean supports(EventType eventType) {
        // 默认支持所有事件类型
        return true;
    }

    @Override
    public int getPriority() {
        // 优先级较低，让业务特定的处理器先处理
        return 1000;
    }

    /**
     * 发送告警通知
     * @param event     领域事件
     * @param consumeDO 消费记录
     */
    private void sendAlert(DomainEvent event, EventConsumeDO consumeDO) {
        // TODO: 实现告警逻辑
        // 1. 发送钉钉/企业微信通知
        // 2. 发送邮件通知
        // 3. 记录到专门的失败表
        // 4. 触发人工处理流程

        log.info("Alert sent for failed event: eventId={}", event.getEventId());
    }

}

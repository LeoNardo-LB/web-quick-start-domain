package org.smm.archetype.infrastructure._shared.event.handler;

import org.smm.archetype.domain._shared.base.DomainEvent;
import org.smm.archetype.domain._shared.event.EventType;
import org.smm.archetype.infrastructure._shared.generated.repository.entity.EventConsumeDO;

/**
 * 事件失败处理器接口
 *
 * <p>用于处理最终失败的事件（达到最大重试次数）。
 *
 * <p>实现类可以根据不同的业务场景实现不同的处理策略：
 * <ul>
 *   <li>发送告警通知</li>
 *   <li>记录到专门的失败表</li>
 *   <li>调用人工介入接口</li>
 *   <li>执行补偿事务</li>
 * </ul>
 * @author Leonardo
 * @since 2026/01/09
 */
public interface EventFailureHandler {

    /**
     * 处理失败事件
     * @param event     领域事件
     * @param consumeDO 消费记录
     */
    void handleFailure(DomainEvent event, EventConsumeDO consumeDO);

    /**
     * 判断是否支持该事件类型
     * @param eventType 事件类型
     * @return true-支持，false-不支持
     */
    boolean supports(EventType eventType);

    /**
     * 获取处理器优先级（数值越小优先级越高）
     * @return 优先级
     */
    default int getPriority() {
        return 100;
    }

}

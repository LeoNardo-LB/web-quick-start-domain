package org.smm.archetype.adapter.event;

import org.smm.archetype.domain.bizshared.event.Event;
import org.smm.archetype.domain.bizshared.event.Type;
import org.smm.archetype.infrastructure.bizshared.event.repository.EventConsumeRecord;
import org.springframework.core.Ordered;

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
 *
 * <p>设计说明：
 * <ul>
 *   <li>使用EventConsumeRecord（Domain层值对象）而非EventConsumeDO（Infrastructure层）</li>
 *   <li>避免App层直接依赖Infrastructure层的DO对象</li>
 *   <li>符合DDD分层架构原则</li>
 * </ul>
 * @author Leonardo
 * @since 2026/01/09
 */
public interface FailureHandler extends Ordered {

    /**
     * 处理失败事件
     * @param event         事件
     * @param consumeRecord 消费记录值对象
     * @param e             异常信息
     */
    void handleFailure(Event<?> event, EventConsumeRecord consumeRecord, Exception e);

    /**
     * 判断是否支持该事件类型
     * @param eventType 事件类型
     * @return true-支持，false-不支持
     */
    boolean supports(Type eventType);

    /**
     * 获取处理器的顺序
     * @return 处理器的顺序
     */
    @Override
    default int getOrder() {
        return 0;
    }
}

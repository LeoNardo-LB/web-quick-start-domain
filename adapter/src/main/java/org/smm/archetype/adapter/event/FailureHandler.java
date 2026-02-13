package org.smm.archetype.adapter.event;

import org.smm.archetype.domain.shared.event.Event;
import org.smm.archetype.domain.shared.event.Type;
import org.smm.archetype.infrastructure.shared.event.persistence.EventConsumeRecord;
import org.springframework.core.Ordered;

/**
 * 事件失败处理器接口，处理达到最大重试次数的失败事件。
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

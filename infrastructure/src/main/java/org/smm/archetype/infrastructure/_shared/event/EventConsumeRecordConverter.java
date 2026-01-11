package org.smm.archetype.infrastructure._shared.event;

import org.smm.archetype.domain._shared.event.ConsumeStatus;
import org.smm.archetype.domain._shared.event.EventConsumeRecord;
import org.smm.archetype.domain._shared.event.EventPriority;
import org.smm.archetype.infrastructure._shared.generated.repository.entity.EventConsumeDO;
import org.springframework.stereotype.Component;

/**
 * 事件消费记录转换器
 *
 * <p>负责在EventConsumeDO（Infrastructure层）和EventConsumeRecord（Domain层）之间转换。
 *
 * <p>为什么需要转换器：
 * <ul>
 *   <li>Domain层不应该依赖Infrastructure层的DO对象</li>
 *   <li>App层需要使用Domain层的值对象</li>
 *   <li>转换逻辑集中在一个地方，便于维护</li>
 * </ul>
 * @author Leonardo
 * @since 2026/01/10
 */
@Component
public class EventConsumeRecordConverter {

    /**
     * 从EventConsumeDO转换为EventConsumeRecord
     *
     * <p>转换规则：
     * <ul>
     *   <li>字符串枚举值转换为对应的枚举类型</li>
     *   <li>保留所有业务字段</li>
     *   <li>确保类型安全</li>
     * </ul>
     * @param consumeDO 消费记录DO对象
     * @return 消费记录值对象
     */
    public EventConsumeRecord from(EventConsumeDO consumeDO) {
        if (consumeDO == null) {
            return null;
        }

        return EventConsumeRecord.builder()
                       .setEventId(consumeDO.getEventId())
                       .setPriority(stringToEventPriority(consumeDO.getPriority()))
                       .setIdempotentKey(consumeDO.getIdempotentKey())
                       .setConsumerGroup(consumeDO.getConsumerGroup())
                       .setConsumerName(consumeDO.getConsumerName())
                       .setConsumeStatus(stringToConsumeStatus(consumeDO.getConsumeStatus()))
                       .setConsumeTime(consumeDO.getConsumeTime())
                       .setCompleteTime(consumeDO.getCompleteTime())
                       .setNextRetryTime(consumeDO.getNextRetryTime())
                       .setRetryTimes(consumeDO.getRetryTimes())
                       .setMaxRetryTimes(consumeDO.getMaxRetryTimes())
                       .setErrorMessage(consumeDO.getErrorMessage())
                       .setCreateTime(consumeDO.getCreateTime())
                       .setVersion(consumeDO.getVersion())
                       .build();
    }

    /**
     * 字符串转EventPriority枚举
     */
    private EventPriority stringToEventPriority(String priority) {
        if (priority == null) {
            return null;
        }
        return EventPriority.valueOf(priority);
    }

    /**
     * 字符串转ConsumeStatus枚举
     */
    private ConsumeStatus stringToConsumeStatus(String consumeStatus) {
        if (consumeStatus == null) {
            return null;
        }
        return ConsumeStatus.fromString(consumeStatus);
    }

}

package org.smm.archetype.infrastructure.shared.event;

import org.mapstruct.Mapper;
import org.smm.archetype.infrastructure.shared.dal.generated.entity.EventDO;
import org.smm.archetype.infrastructure.shared.event.persistence.EventConsumeRecord;

/**
 * 事件消费记录转换器（MapStruct实现）
 *
负责在EventDO（Infrastructure层）和EventConsumeRecord（Domain层）之间转换。
 *
为什么需要转换器：
 * <ul>
 *   <li>Domain层不应该依赖Infrastructure层的DO对象</li>
 *   <li>App层需要使用Domain层的值对象</li>
 *   <li>转换逻辑集中在一个地方，便于维护</li>
 * </ul>
 *
通过@Mapper(componentModel = "spring")自动生成@Component，支持Spring依赖注入


 */
@Mapper(componentModel = "spring")
public interface EventRecordConverter {

    /**
     * 从EventDO转换为EventConsumeRecord
     *
    转换规则：
     * <ul>
     *   <li>字符串枚举值转换为对应的枚举类型</li>
     *   <li>保留所有业务字段</li>
     *   <li>确保类型安全</li>
     * </ul>
     * @param eventDO 事件DO对象
     * @return 消费记录值对象
     */
    EventConsumeRecord from(EventDO eventDO);

}

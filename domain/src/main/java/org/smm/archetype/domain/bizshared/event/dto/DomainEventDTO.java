package org.smm.archetype.domain.bizshared.event.dto;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.smm.archetype.domain.bizshared.base.AggregateRoot.AggregateType;

/**
 * 领域事件基类
 * @author Leonardo
 * @since 2026/1/31
 */
@Getter
@SuperBuilder(setterPrefix = "set")
public abstract class DomainEventDTO {

    /**
     * 聚合根ID（事件所属的聚合根）
     */
    protected String aggregateId;

    /**
     * 聚合根类型
     */
    protected AggregateType aggregateType;

}

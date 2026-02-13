package org.smm.archetype.domain.shared.base;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import org.smm.archetype.domain.shared.event.DomainEventPublisher;
import org.smm.archetype.domain.shared.event.Event;
import org.smm.archetype.domain.shared.event.Status;
import org.smm.archetype.domain.shared.event.dto.DomainEventDTO;

import java.util.UUID;

/**
 * 聚合根基类，提供事件发布和一致性边界管理。
 */
@Slf4j
@Getter
@SuperBuilder(setterPrefix = "set", builderMethodName = "ABuilder")
public abstract class AggregateRoot extends Entity {

    private final DomainEventPublisher domainEventPublisher;

    /**
     * 记录领域事件（符合DDD规范）。
     * @param eventDTO 领域事件DTO
     */
    protected void recordEvent(DomainEventDTO eventDTO) {
        if (eventDTO == null) {
            throw new IllegalArgumentException("Domain event cannot be null");
        }
        // 如果没有配置事件发布器，仅记录日志（支持单元测试场景）
        if (domainEventPublisher == null) {
            log.debug("DomainEventPublisher not set, skipping event publish: {} from aggregate: {}",
                    eventDTO.getClass().getSimpleName(), this.getClass().getSimpleName());
            return;
        }
        Event<DomainEventDTO> event = Event.<DomainEventDTO>builder()
                                              .setEid(UUID.randomUUID().toString())
                                              .setOccurredOn(java.time.Instant.now())
                                              .setPayload(eventDTO)
                                              .build();
        domainEventPublisher.publish(event);
        log.debug("Recorded domain event: {} to aggregate: {}", eventDTO.getClass().getSimpleName(), this.getClass().getSimpleName());
    }

    /**
     * @deprecated 使用 recordEvent() 替代此方法
     */
    @Deprecated
    protected void addEvent(DomainEventDTO eventDTO) {
        recordEvent(eventDTO);
    }

/**
     * 聚合根类型枚举。
     */
    @Getter
    public enum AggregateType {

        /**
         * 未知类型
         */
        UNKNOWN

    }

}

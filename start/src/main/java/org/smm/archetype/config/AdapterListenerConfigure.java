package org.smm.archetype.config;

import org.smm.archetype.adapter.listener.SpringEventListener;
import org.smm.archetype.adapter.schedule.handler.EventHandler;
import org.smm.archetype.domain.bizshared.base.DomainEvent;
import org.smm.archetype.adapter.schedule.RetryStrategy;
import org.smm.archetype.infrastructure.bizshared.event.repository.EventConsumeRepository;
import org.smm.archetype.infrastructure.bizshared.event.repository.EventPublishRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Adapter层事件监听器配置
 *
 * <p>负责创建事件监听器的Bean。
 * @author Leonardo
 * @since 2026-01-10
 */
@Configuration
public class AdapterListenerConfigure {

    /**
     * Spring事件监听器
     *
     * <p>监听Spring本地事件。
     * @param eventConsumeRepository 事件消费仓储
     * @param eventPublishRepository 事件发布仓储
     * @param eventHandlers          所有事件处理器
     * @param retryStrategy          重试策略
     * @return Spring事件监听器
     */
    @Bean
    @ConditionalOnProperty(
            prefix = "middleware.event.publisher",
            name = "type",
            havingValue = "spring"
    )
    public SpringEventListener springEventListener(
            final EventConsumeRepository eventConsumeRepository,
            final EventPublishRepository eventPublishRepository,
            final List<EventHandler<DomainEvent>> eventHandlers,
            final RetryStrategy retryStrategy) {
        return new SpringEventListener(eventConsumeRepository, eventPublishRepository, eventHandlers, retryStrategy);
    }

}

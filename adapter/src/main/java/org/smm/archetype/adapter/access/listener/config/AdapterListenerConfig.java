package org.smm.archetype.adapter.access.listener.config;

import org.smm.archetype.adapter.access.listener.KafkaEventListener;
import org.smm.archetype.adapter.access.listener.SpringEventListener;
import org.smm.archetype.app._shared.event.EventHandler;
import org.smm.archetype.domain._shared.base.DomainEvent;
import org.smm.archetype.infrastructure._shared.event.EventConsumeRepository;
import org.smm.archetype.infrastructure._shared.event.EventSerializer;
import org.smm.archetype.infrastructure._shared.generated.mapper.EventConsumeMapper;
import org.smm.archetype.infrastructure._shared.generated.mapper.EventPublishMapper;
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
public class AdapterListenerConfig {

    /**
     * Spring事件监听器
     *
     * <p>监听Spring本地事件。
     * @param eventConsumeMapper     事件消费Mapper
     * @param eventConsumeRepository 事件消费仓储
     * @param eventPublishMapper     事件发布Mapper
     * @param eventSerializer        事件序列化器
     * @param eventHandlers          所有事件处理器
     * @return Spring事件监听器
     */
    @Bean
    @ConditionalOnProperty(
            prefix = "middleware.event.publisher",
            name = "type",
            havingValue = "spring"
    )
    public SpringEventListener springEventListener(
            final EventConsumeMapper eventConsumeMapper,
            final EventConsumeRepository eventConsumeRepository,
            final EventPublishMapper eventPublishMapper,
            final EventSerializer eventSerializer,
            final List<EventHandler<DomainEvent>> eventHandlers) {
        return new SpringEventListener(eventConsumeMapper, eventConsumeRepository, eventPublishMapper, eventSerializer, eventHandlers);
    }

    /**
     * Kafka事件监听器
     *
     * <p>监听Kafka消息队列事件。
     * @param eventConsumeMapper     事件消费Mapper
     * @param eventConsumeRepository 事件消费仓储
     * @param eventPublishMapper     事件发布Mapper
     * @param eventSerializer        事件序列化器
     * @param eventHandlers          所有事件处理器
     * @return Kafka事件监听器
     */
    @Bean
    @ConditionalOnProperty(
            prefix = "middleware.event.publisher",
            name = "type",
            havingValue = "kafka"
    )
    public KafkaEventListener kafkaEventListener(
            final EventConsumeMapper eventConsumeMapper,
            final EventConsumeRepository eventConsumeRepository,
            final EventPublishMapper eventPublishMapper,
            final EventSerializer eventSerializer,
            final List<EventHandler<DomainEvent>> eventHandlers) {
        return new KafkaEventListener(eventConsumeMapper, eventConsumeRepository, eventPublishMapper, eventSerializer, eventHandlers);
    }

}

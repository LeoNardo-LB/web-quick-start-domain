package org.smm.archetype.infrastructure._shared.config;

import org.smm.archetype.domain._shared.event.EventPublisher;
import org.smm.archetype.infrastructure._shared.event.EventSerializer;
import org.smm.archetype.infrastructure._shared.event.publisher.KafkaEventPublisher;
import org.smm.archetype.infrastructure._shared.event.publisher.SpringEventPublisher;
import org.smm.archetype.infrastructure._shared.generated.repository.mapper.EventPublishMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.core.KafkaTemplate;

/**
 * 事件发布器配置类
 *
 * <p>配置事件发布相关的 Bean。
 * @author Leonardo
 * @since 2026/01/09
 */
@Configuration
public class EventPublisherConfig {

    private final ApplicationEventPublisher     applicationEventPublisher;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final EventPublishMapper            eventPublishMapper;
    private final EventSerializer               eventSerializer;

    public EventPublisherConfig(
            ApplicationEventPublisher applicationEventPublisher,
            KafkaTemplate<String, String> kafkaTemplate,
            EventPublishMapper eventPublishMapper,
            EventSerializer eventSerializer) {
        this.applicationEventPublisher = applicationEventPublisher;
        this.kafkaTemplate = kafkaTemplate;
        this.eventPublishMapper = eventPublishMapper;
        this.eventSerializer = eventSerializer;
    }

    /**
     * 配置 EventPublisher 的默认实现
     * @param publisherType 发布器类型（kafka 或 spring）
     * @return EventPublisher
     */
    @Bean
    @Primary
    public EventPublisher eventPublisher(@Value("${event.publisher.type:kafka}") String publisherType) {

        KafkaEventPublisher kafkaPublisher = new KafkaEventPublisher(
                kafkaTemplate,
                eventPublishMapper,
                eventSerializer);

        SpringEventPublisher springPublisher = new SpringEventPublisher(
                applicationEventPublisher,
                eventPublishMapper,
                eventSerializer);

        if ("kafka".equalsIgnoreCase(publisherType)) {
            return kafkaPublisher;
        } else if ("spring".equalsIgnoreCase(publisherType)) {
            return springPublisher;
        }

        return kafkaPublisher; // 默认使用 Kafka
    }

}

package org.smm.archetype.config;

import lombok.RequiredArgsConstructor;
import org.smm.archetype.config.properties.EventProperties;
import org.smm.archetype.config.properties.KafkaProperties;
import org.smm.archetype.domain.bizshared.base.DomainEvent;
import org.smm.archetype.infrastructure.bizshared.event.repository.EventConsumeRepository;
import org.smm.archetype.infrastructure.bizshared.event.repository.EventPublishRepository;
import org.smm.archetype.infrastructure.bizshared.event.publisher.KafkaEventPublisher;
import org.smm.archetype.infrastructure.bizshared.dal.generated.mapper.EventPublishMapper;
import org.smm.archetype.adapter.listener.KafkaEventListener;
import org.smm.archetype.adapter.schedule.handler.EventHandler;
import org.smm.archetype.adapter.schedule.RetryStrategy;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Kafka 事件相关配置
 *
 * <p>条件：KafkaTemplate Bean存在时生效（即Kafka依赖已添加）。
 *
 * <p>包含：
 * <ul>
 *   <li>Kafka 监听器容器工厂</li>
 *   <li>Kafka 事件发布器（标注@Primary，优先于Spring事件发布器）</li>
 *   <li>Kafka 事件监听器</li>
 * </ul>
 *
 * <p>使用@ConditionalOnBean(KafkaTemplate.class)检测Kafka依赖的存在性，
 * 替代原有的@ConditionalOnProperty配置方式，实现自动依赖检测。
 *
 * @author Leonardo
 * @since 2026-01-16
 */
@Configuration
@EnableKafka
@EnableConfigurationProperties(KafkaProperties.class)
@ConditionalOnBean(KafkaTemplate.class)
@RequiredArgsConstructor
public class EventKafkaConfigure {

    private final EventProperties eventProperties;

    /**
     * Kafka监听器容器工厂
     *
     * <p>配置JsonDeserializer，实现自动反序列化。
     * @param kafkaProperties Kafka配置属性
     * @return Kafka监听器容器工厂
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, DomainEvent> kafkaListenerContainerFactory(
            KafkaProperties kafkaProperties) {
        Map<String, Object> props = new HashMap<>();

        // 使用KafkaProperties配置
        props.put("bootstrap.servers", kafkaProperties.getBootstrapServers());
        props.put("group.id", kafkaProperties.getGroupId());
        props.put("key.deserializer", kafkaProperties.getKeyDeserializer());
        props.put("value.deserializer", kafkaProperties.getValueDeserializer());
        props.put(JsonDeserializer.TRUSTED_PACKAGES, kafkaProperties.getTrustedPackages());
        props.put("enable.auto.commit", kafkaProperties.getEnableAutoCommit());
        props.put("auto.offset.reset", kafkaProperties.getAutoOffsetReset());
        props.put("max.poll.records", kafkaProperties.getMaxPollRecords());
        props.put("max.poll.interval.ms", kafkaProperties.getMaxPollIntervalMs());

        ConsumerFactory<String, DomainEvent> consumerFactory =
                new DefaultKafkaConsumerFactory<>(
                        props,
                        new org.apache.kafka.common.serialization.StringDeserializer(),
                        new JsonDeserializer<>(DomainEvent.class)
                );

        ConcurrentKafkaListenerContainerFactory<String, DomainEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);

        return factory;
    }

    /**
     * Kafka事件发布器
     *
     * <p>标注@Primary，确保当Kafka依赖存在时，此Bean优先于SpringEventPublisher被注入。
     *
     * @param kafkaTemplate      Kafka模板
     * @param eventPublishMapper 事件发布Mapper
     * @return Kafka事件发布器
     */
    @Bean
    @Primary
    public KafkaEventPublisher kafkaEventPublisher(KafkaTemplate<String, DomainEvent> kafkaTemplate,
                                                   EventPublishMapper eventPublishMapper) {
        return new KafkaEventPublisher(kafkaTemplate, eventPublishMapper, eventProperties.getPublisher().getKafka().getTopicPrefix());
    }

    /**
     * Kafka 事件监听器
     * @param eventConsumeRepository 事件消费仓储
     * @param eventPublishRepository 事件发布仓储
     * @param eventHandlers          事件处理器列表
     * @param retryStrategy          重试策略
     * @return Kafka事件监听器
     */
    @Bean
    public KafkaEventListener kafkaEventListener(
            EventConsumeRepository eventConsumeRepository,
            EventPublishRepository eventPublishRepository,
            List<EventHandler<DomainEvent>> eventHandlers,
            RetryStrategy retryStrategy) {
        return new KafkaEventListener(eventConsumeRepository, eventPublishRepository, eventHandlers, retryStrategy);
    }

}

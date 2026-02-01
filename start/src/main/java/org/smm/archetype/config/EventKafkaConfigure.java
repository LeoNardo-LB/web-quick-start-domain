package org.smm.archetype.config;

import lombok.RequiredArgsConstructor;
import org.smm.archetype.config.properties.KafkaProperties;
import org.smm.archetype.domain.bizshared.event.Event;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;

import java.util.HashMap;
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
 * @author Leonardo
 * @since 2026-01-16
 */
@Configuration
@RequiredArgsConstructor
@EnableKafka
@EnableConfigurationProperties(KafkaProperties.class)
public class EventKafkaConfigure {

    /**
     * Kafka监听器容器工厂
     *
     * <p>配置JsonDeserializer，实现自动反序列化。
     * @param kafkaProperties Kafka配置属性
     * @return Kafka监听器容器工厂
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Event<?>> kafkaListenerContainerFactory(
            KafkaProperties kafkaProperties) {
        Map<String, Object> props = new HashMap<>();

        // 使用KafkaProperties配置
        props.put("bootstrap.servers", kafkaProperties.getBootstrapServers());
        props.put("group.id", kafkaProperties.getGroupId());
        props.put("key.deserializer", kafkaProperties.getKeyDeserializer());
        props.put("value.deserializer", kafkaProperties.getValueDeserializer());
        props.put(JacksonJsonDeserializer.TRUSTED_PACKAGES, kafkaProperties.getTrustedPackages());
        props.put("enable.auto.commit", kafkaProperties.getEnableAutoCommit());
        props.put("auto.offset.reset", kafkaProperties.getAutoOffsetReset());
        props.put("max.poll.records", kafkaProperties.getMaxPollRecords());
        props.put("max.poll.interval.ms", kafkaProperties.getMaxPollIntervalMs());

        ConsumerFactory<String, Event<?>> consumerFactory = new DefaultKafkaConsumerFactory<>(
                props,
                new org.apache.kafka.common.serialization.StringDeserializer(),
                new JacksonJsonDeserializer<>()
        );

        ConcurrentKafkaListenerContainerFactory<String, Event<?>> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);

        return factory;
    }

}

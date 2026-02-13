// package org.smm.archetype.config;
//
// import lombok.RequiredArgsConstructor;
// import org.smm.archetype.adapter.event.EventDispatcher;
// import org.smm.archetype.adapter.listener.KafkaDomainEventListener;
// import org.smm.archetype.config.properties.KafkaProperties;
// import org.smm.archetype.domain.shared.event.Event;
// import org.smm.archetype.infrastructure.shared.dal.generated.mapper.EventMapper;
// import org.smm.archetype.infrastructure.shared.event.publisher.KafkaDomainEventPublisher;
// import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
// import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
// import org.springframework.boot.context.properties.EnableConfigurationProperties;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.context.annotation.Primary;
// import org.springframework.kafka.annotation.EnableKafka;
// import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
// import org.springframework.kafka.core.ConsumerFactory;
// import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
// import org.springframework.kafka.core.KafkaTemplate;
// import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;
//
// import java.util.HashMap;
// import java.util.Map;
//
// /**
//  * Kafka事件配置类，配置Kafka监听器容器工厂。
//  */
// @Configuration
// @RequiredArgsConstructor
// @ConditionalOnClass(name = "org.springframework.kafka.core.KafkaTemplate")
// @EnableKafka
// @EnableConfigurationProperties(KafkaProperties.class)
// public class EventKafkaConfigure {
//
//     /**
//      * Kafka监听器容器工厂
//      *
//     配置JsonDeserializer，实现自动反序列化。
//      * @param kafkaProperties Kafka配置属性
//      * @return Kafka监听器容器工厂
//      */
//     @Bean
//     public ConcurrentKafkaListenerContainerFactory<String, Event<?>> kafkaListenerContainerFactory(
//             KafkaProperties kafkaProperties) {
//         Map<String, Object> props = new HashMap<>();
//
//         // 使用KafkaProperties配置
//         props.put("bootstrap.servers", kafkaProperties.getBootstrapServers());
//         props.put("group.id", kafkaProperties.getGroupId());
//         props.put("key.deserializer", kafkaProperties.getKeyDeserializer());
//         props.put("value.deserializer", kafkaProperties.getValueDeserializer());
//         props.put(JacksonJsonDeserializer.TRUSTED_PACKAGES, kafkaProperties.getTrustedPackages());
//         props.put("enable.auto.commit", kafkaProperties.getEnableAutoCommit());
//         props.put("auto.offset.reset", kafkaProperties.getAutoOffsetReset());
//         props.put("max.poll.records", kafkaProperties.getMaxPollRecords());
//         props.put("max.poll.interval.ms", kafkaProperties.getMaxPollIntervalMs());
//
//         ConsumerFactory<String, Event<?>> consumerFactory = new DefaultKafkaConsumerFactory<>(
//                 props,
//                 new org.apache.kafka.common.serialization.StringDeserializer(),
//                 new JacksonJsonDeserializer<>()
//         );
//
//         ConcurrentKafkaListenerContainerFactory<String, Event<?>> factory = new ConcurrentKafkaListenerContainerFactory<>();
//         factory.setConsumerFactory(consumerFactory);
//
//         return factory;
//     }
//
//     /**
//      * Kafka 事件发布器
//      * @param kafkaTemplate Kafka 模板
//      * @param mapper        事件 Mapper
//      * @return Kafka 事件发布器
//      */
//     @Bean
//     @Primary
//     @ConditionalOnBean(KafkaTemplate.class)
//     public KafkaDomainEventPublisher kafkaEventPublisher(
//             KafkaTemplate<String, Event<?>> kafkaTemplate,
//             EventMapper mapper) {
//         return new KafkaDomainEventPublisher(kafkaTemplate, mapper);
//     }
//
//     /**
//      * Kafka 事件监听器
//      * @param eventDispatcher 事件分发器
//      * @return Kafka 事件监听器
//      */
//     @Bean
//     @Primary
//     @ConditionalOnBean(KafkaTemplate.class)
//     public KafkaDomainEventListener kafkaEventListener(EventDispatcher eventDispatcher) {
//         return new KafkaDomainEventListener(eventDispatcher);
//     }
//
// }

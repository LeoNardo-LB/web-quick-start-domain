package org.smm.archetype.config;

import lombok.RequiredArgsConstructor;
import org.smm.archetype.app._shared.event.OrderCancelledEventHandler;
import org.smm.archetype.app._shared.event.OrderCreatedEventHandler;
import org.smm.archetype.app._shared.event.OrderPaidEventHandler;
import org.smm.archetype.config.properties.EventProperties;
import org.smm.archetype.config.properties.KafkaProperties;
import org.smm.archetype.config.properties.RetryDelayProperties;
import org.smm.archetype.domain._shared.base.DomainEvent;
import org.smm.archetype.domain._shared.event.EventPublisher;
import org.smm.archetype.infrastructure._shared.event.EventConsumeRepository;
import org.smm.archetype.infrastructure._shared.event.EventPublishRepository;
import org.smm.archetype.infrastructure._shared.event.TransactionEventPublishingAspect;
import org.smm.archetype.infrastructure._shared.event.publisher.AsyncEventPublisher;
import org.smm.archetype.infrastructure._shared.event.publisher.KafkaEventPublisher;
import org.smm.archetype.infrastructure._shared.event.publisher.SpringEventPublisher;
import org.smm.archetype.infrastructure._shared.event.publisher.TransactionalEventPublisher;
import org.smm.archetype.infrastructure._shared.generated.repository.mapper.EventConsumeMapper;
import org.smm.archetype.infrastructure._shared.generated.repository.mapper.EventPublishMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

/**
 * Infrastructure层事件发布相关配置
 *
 * <p>负责创建事件发布、事件消费、序列化等相关的Bean。
 *
 * <p>对于同一配置类中的Bean依赖，使用@Bean方法参数注入（Spring推荐方式）
 * 对于跨配置类的Bean依赖，使用构造器注入（遵循文档第2.6节规范）
 * @author Leonardo
 * @since 2026-01-10
 */
@Configuration
@EnableKafka
@EnableAsync
@EnableConfigurationProperties({
        EventProperties.class,
        KafkaProperties.class,
        RetryDelayProperties.class
})
@RequiredArgsConstructor
public class EventConfigure implements AsyncConfigurer {

    private final EventProperties eventProperties;
    private final KafkaProperties kafkaProperties;

    /**
     * 事件消费仓储
     * @param eventConsumeMapper 事件消费Mapper
     * @return 事件消费仓储
     */
    @Bean
    public EventConsumeRepository eventConsumeRepository(final EventConsumeMapper eventConsumeMapper) {
        return new EventConsumeRepository(eventConsumeMapper);
    }

    /**
     * 事件发布仓储
     * @param eventPublishMapper 事件发布Mapper
     * @return 事件发布仓储
     */
    @Bean
    public EventPublishRepository eventPublishRepository(final EventPublishMapper eventPublishMapper) {
        return new EventPublishRepository(eventPublishMapper);
    }

    /**
     * Kafka监听器容器工厂
     *
     * <p>配置JsonDeserializer，实现自动反序列化。
     * @return Kafka监听器容器工厂
     */
    @Bean
    @ConditionalOnProperty(
            prefix = "middleware.event.publisher",
            name = "type",
            havingValue = "kafka"
    )
    public ConcurrentKafkaListenerContainerFactory<String, DomainEvent> kafkaListenerContainerFactory() {
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
     * <p>条件：middleware.event.publisher.type=kafka
     *
     * <p>注意：不使用@Primary注解，Primary由EventPublisherConfig中的代理Bean统一提供。
     * @param kafkaTemplate      Kafka模板
     * @param eventPublishMapper 事件发布Mapper
     * @return Kafka事件发布器
     */
    @Bean
    @ConditionalOnProperty(
            prefix = "middleware.event.publisher",
            name = "type",
            havingValue = "kafka"
    )
    public KafkaEventPublisher kafkaEventPublisher(KafkaTemplate<String, DomainEvent> kafkaTemplate,
                                                   EventPublishMapper eventPublishMapper) {
        return new KafkaEventPublisher(kafkaTemplate, eventPublishMapper, eventProperties.getPublisher().getKafka().getTopicPrefix());
    }

    /**
     * Spring事件发布器
     *
     * <p>条件：middleware.event.publisher.type=spring
     *
     * <p>注意：不使用@Primary注解，Primary由EventPublisherConfig中的代理Bean统一提供。
     * @param applicationEventPublisher Application事件发布器
     * @param eventPublishMapper        事件发布Mapper
     * @return Spring事件发布器
     */
    @Bean
    @ConditionalOnProperty(
            prefix = "middleware.event.publisher",
            name = "type",
            havingValue = "spring",
            matchIfMissing = true
    )
    public SpringEventPublisher springEventPublisher(
            final org.springframework.context.ApplicationEventPublisher applicationEventPublisher,
            final EventPublishMapper eventPublishMapper) {
        return new SpringEventPublisher(applicationEventPublisher, eventPublishMapper);
    }

    /**
     * 异步事件发布器
     *
     * <p>包装具体的事件发布器，提供异步发布能力。
     *
     * <p>对于同一配置类中的Bean依赖，使用@Bean方法参数注入（Spring推荐方式，避免循环依赖）
     * @param kafkaEventPublisher  Kafka事件发布器（可选）
     * @param springEventPublisher Spring事件发布器（可选）
     * @return 异步事件发布器
     */
    @Bean
    public AsyncEventPublisher asyncEventPublisher(
            @Autowired(required = false) final KafkaEventPublisher kafkaEventPublisher,
            @Autowired(required = false) final SpringEventPublisher springEventPublisher) {

        // 根据哪个Bean存在，选择作为被包装对象
        // Spring的条件装配确保只有一个不为null
        EventPublisher delegate = (kafkaEventPublisher != null) ? kafkaEventPublisher : springEventPublisher;

        return new AsyncEventPublisher(delegate);
    }

    /**
     * 事务性事件发布器
     *
     * <p>确保事件在事务提交后才发布。
     *
     * <p>对于同一配置类中的Bean依赖，使用@Bean方法参数注入（Spring推荐方式，避免循环依赖）
     * @param kafkaEventPublisher  Kafka事件发布器（可选）
     * @param springEventPublisher Spring事件发布器（可选）
     * @return 事务性事件发布器
     */
    @Bean
    public TransactionalEventPublisher transactionalEventPublisher(
            @Autowired(required = false) final KafkaEventPublisher kafkaEventPublisher,
            @Autowired(required = false) final SpringEventPublisher springEventPublisher) {

        // 根据哪个Bean存在，选择作为被包装对象
        // Spring的条件装配确保只有一个不为null
        EventPublisher delegate = (kafkaEventPublisher != null) ? kafkaEventPublisher : springEventPublisher;

        return new TransactionalEventPublisher(delegate);
    }

    /**
     * 事务事件发布切面
     *
     * <p>拦截@Transactional注解的方法，在事务提交后发布事件。
     * @param applicationEventPublisher Application事件发布器
     * @return 事务事件发布切面
     */
    @Bean
    public TransactionEventPublishingAspect transactionEventPublishingAspect(
            final org.springframework.context.ApplicationEventPublisher applicationEventPublisher) {
        return new TransactionEventPublishingAspect(applicationEventPublisher);
    }

    /**
     * 配置异步执行器
     *
     * <p>用于异步事件发布。
     * @return 异步执行器
     */
    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(eventProperties.getPublisher().getSpring().getThreadPoolCoreSize());
        executor.setMaxPoolSize(eventProperties.getPublisher().getSpring().getThreadPoolMaxSize());
        executor.setQueueCapacity(eventProperties.getPublisher().getSpring().getThreadPoolQueueCapacity());
        executor.setThreadNamePrefix("event-async-");
        executor.initialize();
        return executor;
    }

    /**
     * 订单已创建事件处理器
     * @return 订单已创建事件处理器
     */
    @Bean
    public OrderCreatedEventHandler orderCreatedEventHandler() {
        return new OrderCreatedEventHandler();
    }

    /**
     * 订单已支付事件处理器
     * @return 订单已支付事件处理器
     */
    @Bean
    public OrderPaidEventHandler orderPaidEventHandler() {
        return new OrderPaidEventHandler();
    }

    /**
     * 订单已取消事件处理器
     * @return 订单已取消事件处理器
     */
    @Bean
    public OrderCancelledEventHandler orderCancelledEventHandler() {
        return new OrderCancelledEventHandler();
    }

}

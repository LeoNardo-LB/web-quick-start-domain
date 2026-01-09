package org.smm.archetype.infrastructure._shared.config;

import org.smm.archetype.domain._shared.event.EventPublisher;
import org.smm.archetype.infrastructure._shared.event.EventConsumeRepository;
import org.smm.archetype.infrastructure._shared.event.EventSerializer;
import org.smm.archetype.infrastructure._shared.event.TransactionEventPublishingAspect;
import org.smm.archetype.infrastructure._shared.event.handler.DefaultEventFailureHandler;
import org.smm.archetype.infrastructure._shared.event.publisher.AsyncEventPublisher;
import org.smm.archetype.infrastructure._shared.event.publisher.KafkaEventPublisher;
import org.smm.archetype.infrastructure._shared.event.publisher.SpringEventPublisher;
import org.smm.archetype.infrastructure._shared.event.publisher.TransactionalEventPublisher;
import org.smm.archetype.infrastructure._shared.generated.mapper.EventConsumeMapper;
import org.smm.archetype.infrastructure._shared.generated.mapper.EventPublishMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

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
@EnableAsync
public class InfrastructureEventConfig implements AsyncConfigurer {

    /**
     * 事件序列化器
     * @return 事件序列化器
     */
    @Bean
    public EventSerializer eventSerializer() {
        return new EventSerializer();
    }

    /**
     * 事件消费仓储
     * @param eventConsumeMapper 事件消费Mapper
     * @return 事件消费仓储
     */
    @Bean
    public EventConsumeRepository eventConsumeRepository(
            final EventConsumeMapper eventConsumeMapper) {
        return new EventConsumeRepository(eventConsumeMapper);
    }

    /**
     * 默认事件失败处理器
     * @return 事件失败处理器
     */
    @Bean
    public DefaultEventFailureHandler defaultEventFailureHandler() {
        return new DefaultEventFailureHandler();
    }

    /**
     * Kafka事件发布器
     *
     * <p>条件：middleware.event.publisher.type=kafka
     *
     * <p>注意：不使用@Primary注解，Primary由EventPublisherConfig中的代理Bean统一提供。
     * @param kafkaTemplate      Kafka模板
     * @param eventPublishMapper 事件发布Mapper
     * @param eventSerializer    事件序列化器
     * @return Kafka事件发布器
     */
    @Bean
    @ConditionalOnProperty(
            prefix = "middleware.event.publisher",
            name = "type",
            havingValue = "kafka"
    )
    public KafkaEventPublisher kafkaEventPublisher(
            final KafkaTemplate<String, String> kafkaTemplate,
            final EventPublishMapper eventPublishMapper,
            final EventSerializer eventSerializer) {
        return new KafkaEventPublisher(kafkaTemplate, eventPublishMapper, eventSerializer);
    }

    /**
     * Spring事件发布器
     *
     * <p>条件：middleware.event.publisher.type=spring
     *
     * <p>注意：不使用@Primary注解，Primary由EventPublisherConfig中的代理Bean统一提供。
     * @param applicationEventPublisher Application事件发布器
     * @param eventPublishMapper        事件发布Mapper
     * @param eventSerializer           事件序列化器
     * @return Spring事件发布器
     */
    @Bean
    @ConditionalOnProperty(
            prefix = "middleware.event.publisher",
            name = "type",
            havingValue = "spring"
    )
    public SpringEventPublisher springEventPublisher(
            final org.springframework.context.ApplicationEventPublisher applicationEventPublisher,
            final EventPublishMapper eventPublishMapper,
            final EventSerializer eventSerializer) {
        return new SpringEventPublisher(applicationEventPublisher, eventPublishMapper, eventSerializer);
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
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("event-async-");
        executor.initialize();
        return executor;
    }

}

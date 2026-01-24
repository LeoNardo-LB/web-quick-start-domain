package org.smm.archetype.config;

import lombok.RequiredArgsConstructor;
import org.smm.archetype.config.properties.EventProperties;
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
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
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
 * <h3>事件发布器自动检测机制</h3>
 *
 * <p>本配置使用Spring Boot的自动配置机制，根据Kafka依赖的存在性自动选择事件发布器：
 *
 * <ul>
 *   <li><b>Kafka场景</b>：当KafkaTemplate Bean存在时（即Kafka依赖已添加），
 *       EventKafkaConfigure中的kafkaEventPublisher会被创建，并且标注为@Primary，
 *       因此成为主要的事件发布器。</li>
 *   <li><b>本地场景</b>：当KafkaTemplate Bean不存在时（即未添加Kafka依赖），
 *       springEventPublisher会被创建，作为默认的本地事件发布器。</li>
 * </ul>
 *
 * <p>异步事件发布器（asyncEventPublisher）和事务性事件发布器（transactionalEventPublisher）
 * 仅在KafkaTemplate存在时创建，因为它们主要用于Kafka事件的异步和事务发布场景。
 *
 * <p>对于同一配置类中的Bean依赖，使用@Bean方法参数注入（Spring推荐方式）
 * 对于跨配置类的Bean依赖，使用构造器注入（遵循文档第2.6节规范）
 *
 * @author Leonardo
 * @since 2026-01-10
 */
@Configuration
@EnableAsync
@EnableConfigurationProperties({
        EventProperties.class,
        RetryDelayProperties.class
})
@RequiredArgsConstructor
public class EventConfigure implements AsyncConfigurer {

    private final EventProperties eventProperties;

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
     * Spring事件发布器
     *
     * <p>条件：Kafka依赖不存在时，使用Spring事件发布器（本地默认方案）。
     *
     * <p>使用@ConditionalOnMissingBean确保只有当KafkaTemplate不存在时才创建此Bean，
     * 与kafkaEventPublisher形成互斥关系。
     *
     * @param applicationEventPublisher Application事件发布器
     * @param eventPublishMapper        事件发布Mapper
     * @return Spring事件发布器
     */
    @Bean
    @ConditionalOnMissingBean(KafkaTemplate.class)
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
     * <p>条件：KafkaTemplate Bean存在时创建，支持异步Kafka事件发布。
     *
     * <p>对于同一配置类中的Bean依赖，使用@Bean方法参数注入（Spring推荐方式，避免循环依赖）
     *
     * @param kafkaEventPublisher  Kafka事件发布器
     * @return 异步事件发布器
     */
    @Bean
    @ConditionalOnBean(KafkaTemplate.class)
    public AsyncEventPublisher asyncEventPublisher(
            final KafkaEventPublisher kafkaEventPublisher) {
        return new AsyncEventPublisher(kafkaEventPublisher);
    }

    /**
     * 事务性事件发布器
     *
     * <p>确保事件在事务提交后才发布。
     *
     * <p>条件：KafkaTemplate Bean存在时创建，支持事务性Kafka事件发布。
     *
     * <p>对于同一配置类中的Bean依赖，使用@Bean方法参数注入（Spring推荐方式，避免循环依赖）
     *
     * @param kafkaEventPublisher  Kafka事件发布器
     * @return 事务性事件发布器
     */
    @Bean
    @ConditionalOnBean(KafkaTemplate.class)
    public TransactionalEventPublisher transactionalEventPublisher(
            final KafkaEventPublisher kafkaEventPublisher) {
        return new TransactionalEventPublisher(kafkaEventPublisher);
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

}

package org.smm.archetype.config;

import lombok.RequiredArgsConstructor;
import org.smm.archetype.adapter.event.EventDispatcher;
import org.smm.archetype.adapter.event.EventHandler;
import org.smm.archetype.adapter.event.FailureHandler;
import org.smm.archetype.adapter.listener.KafkaDomainEventListener;
import org.smm.archetype.adapter.listener.SpringDomainEventListener;
import org.smm.archetype.adapter.schedule.RetryStrategy;
import org.smm.archetype.config.properties.EventProperties;
import org.smm.archetype.domain.bizshared.event.Event;
import org.smm.archetype.infrastructure.bizshared.dal.generated.mapper.EventMapper;
import org.smm.archetype.infrastructure.bizshared.event.EventRecordConverter;
import org.smm.archetype.infrastructure.bizshared.event.publisher.KafkaDomainEventPublisher;
import org.smm.archetype.infrastructure.bizshared.event.publisher.SpringDomainEventPublisher;
import org.smm.archetype.infrastructure.bizshared.event.repository.EventRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.Collections;
import java.util.List;

/**
 * 领域事件配置类，负责创建事件发布和消费相关的Bean。
 */
 *   <li><b>Kafka 场景</b>：当 KafkaTemplate Bean 存在时，
 *       kafkaEventPublisher 会被创建（见 KafkaEventConfigure）。</li>
 *   <li><b>本地场景</b>：当 KafkaTemplate Bean 不存在时，
 *       springEventPublisher 会被创建，作为默认的本地事件发布器。</li>
 * </ul>
 * @author Leonardo
 * @see KafkaEventConfigure
 * @since 2026-01-10
 */
@Configuration
@EnableAsync
@RequiredArgsConstructor
@EnableConfigurationProperties(EventProperties.class)
public class DomainEventConfigure implements AsyncConfigurer {

    @Value("${middleware.kafka.consumer.group-id}")
    private String executorGroup;

    /**
     * 事件仓储
     * @param eventMapper 事件 Mapper
     * @return 事件仓储
     */
    @Bean
    public EventRepository eventRepository(EventMapper eventMapper) {
        return new EventRepository(eventMapper);
    }

    /**
     * 事件分发器
     *
     * <p>统一控制事件消费的完整生命周期。
     * @param eventRepository 事件仓储
     * @param eventHandlers   事件处理器列表（可选）
     * @param retryStrategy   重试策略
     * @param failureHandlers 失败处理器列表（可选）
     * @param recordConverter 记录转换器
     * @param eventProperties 事件配置属性
     * @return 事件分发器
     */
    @Bean
    public EventDispatcher eventDispatcher(
            EventRepository eventRepository,
            List<EventHandler<?>> eventHandlers,
            RetryStrategy retryStrategy,
            List<FailureHandler> failureHandlers,
            EventRecordConverter recordConverter,
            EventProperties eventProperties) {

        // 处理空列表情况
        List<EventHandler<?>> handlers = eventHandlers != null ? eventHandlers : Collections.emptyList();
        List<FailureHandler> failures = failureHandlers != null ? failureHandlers : Collections.emptyList();

        int maxRetryTimes = eventProperties.getRetry().getMaxRetryTimes();

        return new EventDispatcher(
                eventRepository,
                handlers,
                retryStrategy,
                failures,
                recordConverter,
                executorGroup,
                maxRetryTimes);
    }

    /**
     * Spring 事件发布器
     * @param publisher Application 事件发布器
     * @param mapper    事件 Mapper
     * @return Spring 事件发布器
     */
    @Bean
    public SpringDomainEventPublisher springEventPublisher(
            ApplicationEventPublisher publisher,
            EventMapper mapper) {
        return new SpringDomainEventPublisher(publisher, mapper);
    }

    /**
     * Spring 事件监听器
     * @param eventDispatcher 事件分发器
     * @return Spring 事件监听器
     */
    @Bean
    public SpringDomainEventListener springEventListener(EventDispatcher eventDispatcher) {
        return new SpringDomainEventListener(eventDispatcher);
    }

    /**
     * Kafka 事件发布器
     * @param kafkaTemplate Kafka 模板
     * @param mapper        事件 Mapper
     * @return Kafka 事件发布器
     */
    @Bean
    @Primary
    @ConditionalOnBean(KafkaTemplate.class)
    public KafkaDomainEventPublisher kafkaEventPublisher(
            KafkaTemplate<String, Event<?>> kafkaTemplate,
            EventMapper mapper) {
        return new KafkaDomainEventPublisher(kafkaTemplate, mapper);
    }

    /**
     * Kafka 事件监听器
     * @param eventDispatcher 事件分发器
     * @return Kafka 事件监听器
     */
    @Bean
    @Primary
    @ConditionalOnBean(KafkaTemplate.class)
    public KafkaDomainEventListener kafkaEventListener(EventDispatcher eventDispatcher) {
        return new KafkaDomainEventListener(eventDispatcher);
    }

}

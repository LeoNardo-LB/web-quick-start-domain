package org.smm.archetype.config;

import org.smm.archetype.adapter.event.EventDispatcher;
import org.smm.archetype.adapter.event.EventHandler;
import org.smm.archetype.adapter.event.FailureHandler;
import org.smm.archetype.adapter.listener.SpringDomainEventListener;
import org.smm.archetype.adapter.schedule.ExponentialBackoffRetryStrategy;
import org.smm.archetype.adapter.schedule.ExternalSchedulerRetryStrategy;
import org.smm.archetype.adapter.schedule.RetryStrategy;
import org.smm.archetype.config.properties.EventProperties;
import org.smm.archetype.domain.shared.event.DomainEventPublisher;
import org.smm.archetype.domain.shared.event.PayloadParser;
import org.smm.archetype.domain.shared.event.PayloadParserHolder;
import org.smm.archetype.infrastructure.shared.dal.generated.mapper.EventMapper;
import org.smm.archetype.infrastructure.shared.event.EventRecordConverter;
import org.smm.archetype.infrastructure.shared.event.FastJsonPayloadParser;
import org.smm.archetype.infrastructure.shared.event.publisher.DomainEventCollectAspectJ;
import org.smm.archetype.infrastructure.shared.event.publisher.SpringDomainEventPublisher;
import org.smm.archetype.infrastructure.shared.event.persistence.EventRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.Collections;
import java.util.List;

/**
 * 领域事件配置类，负责创建事件发布和消费相关的Bean。
 */
@Configuration
@EnableAsync
@EnableConfigurationProperties(EventProperties.class)
public class EventConfigure implements AsyncConfigurer {

    /**
     * 事件仓储
     * @param eventMapper 事件 Mapper
     * @param recordConverter 记录转换器
     * @return 事件仓储
     */
    @Bean
    public EventRepository eventRepository(
            EventMapper eventMapper,
            EventRecordConverter recordConverter) {
        return new EventRepository(eventMapper, recordConverter);
    }

    /**
     * 重试策略（指数退避，默认）
     * @return 重试策略
     */
    @Bean
    @ConditionalOnProperty(
            prefix = "middleware.event.retry",
            name = "strategy",
            havingValue = "exponential",
            matchIfMissing = true
    )
    public RetryStrategy exponentialBackoffRetryStrategy() {
        return new ExponentialBackoffRetryStrategy();
    }

    /**
     * 重试策略（外部调度器）
     * @return 重试策略
     */
    @Bean
    @ConditionalOnProperty(
            prefix = "middleware.event.retry",
            name = "strategy",
            havingValue = "external-scheduler"
    )
    public RetryStrategy externalSchedulerRetryStrategy() {
        return new ExternalSchedulerRetryStrategy();
    }

    /**
     * 事件分发器
     * @param eventRepository 事件仓储
     * @param eventHandlers   事件处理器列表（可选）
     * @param failureHandlers 失败处理器列表（可选）
     * @param recordConverter 记录转换器
     * @param eventProperties 事件配置属性
     * @param retryStrategy 重试策略（根据配置选择指数退避或外部调度器）
     * @return 事件分发器
     */
    @Bean
    public EventDispatcher eventDispatcher(
            EventRepository eventRepository,
            List<EventHandler<?>> eventHandlers,
            List<FailureHandler> failureHandlers,
            EventRecordConverter recordConverter,
            EventProperties eventProperties,
            RetryStrategy retryStrategy) {

        // 处理空列表情况
        List<EventHandler<?>> handlers = eventHandlers != null ? eventHandlers : Collections.emptyList();
        List<FailureHandler> failures = failureHandlers != null ? failureHandlers : Collections.emptyList();

        int maxRetryTimes = eventProperties.getRetry().getMaxRetryTimes();
        String executorGroup = "test-executor-group";

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
     * Spring事件监听器
     * @param eventDispatcher 事件分发器
     * @return Spring 事件监听器
     */
    @Bean
    public SpringDomainEventListener springEventListener(EventDispatcher eventDispatcher) {
        return new SpringDomainEventListener(eventDispatcher);
    }

    /**
     * 领域事件收集切面
     * 拦截Application层方法调用，自动收集和发布领域事件。
     * @param domainEventPublisher 领域事件发布器
     * @return 领域事件收集切面
     */
    @Bean
    @Order(2)
    public DomainEventCollectAspectJ domainEventCollectAspectJ(DomainEventPublisher domainEventPublisher) {
        return new DomainEventCollectAspectJ(domainEventPublisher);
    }

    /**
     * 载荷解析器（基于FastJSON2）
     * 
     * <p>同时将解析器注册到Domain层的PayloadParserHolder中，
     * 使Domain层能够使用JSON序列化/反序列化功能，同时保持纯净性（无外部依赖）。</p>
     * 
     * @return 载荷解析器实现
     */
    @Bean
    public PayloadParser payloadParser() {
        PayloadParser parser = new FastJsonPayloadParser();
        // 在创建Bean时立即初始化Holder，避免循环引用
        PayloadParserHolder.setParser(parser);
        return parser;
    }

}

package org.smm.archetype.config;

import org.smm.archetype.adapter.access.listener.EventListener;
import org.smm.archetype.adapter.access.schedule.EventRetrySchedulerImpl;
import org.smm.archetype.app._shared.event.EventFailureHandler;
import org.smm.archetype.config.properties.EventProperties;
import org.smm.archetype.config.properties.RetryDelayProperties;
import org.smm.archetype.infrastructure._shared.event.EventConsumeRecordConverter;
import org.smm.archetype.infrastructure._shared.event.EventConsumeRepository;
import org.smm.archetype.infrastructure._shared.event.EventPublishRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * Adapter层定时任务配置
 *
 * <p>负责创建定时任务的Bean，如事件重试调度器等。
 * @author Leonardo
 * @since 2026-01-10
 */
@Configuration
@EnableScheduling
public class AdapterScheduleConfigure {

    /**
     * 事件重试调度器
     *
     * <p>定时扫描失败的事件，按照重试策略进行重试。
     * @param eventConsumeRepository  事件消费仓储
     * @param eventListeners         所有事件监听器
     * @param failureHandlers        所有失败处理器
     * @param virtualThreadExecutor   虚拟线程池
     * @param recordConverter        消费记录转换器
     * @param eventPublishRepository  事件发布仓储
     * @param eventProperties         事件配置属性
     * @param retryDelayProperties    重试延迟配置属性
     * @return 事件重试调度器
     */
    @Bean
    public EventRetrySchedulerImpl eventRetryScheduler(
            final EventConsumeRepository eventConsumeRepository,
            final List<EventListener> eventListeners,
            final List<EventFailureHandler> failureHandlers,
            @Qualifier("virtualThreadExecutor") final ExecutorService virtualThreadExecutor,
            final EventConsumeRecordConverter recordConverter,
            final EventPublishRepository eventPublishRepository,
            final EventProperties eventProperties,
            final RetryDelayProperties retryDelayProperties) {

        // 从配置对象中提取需要的值，解耦配置类依赖
        int batchSize = eventProperties.getRetry().getBatchSize();
        double highPriorityRatio = eventProperties.getRetry().getHighPriorityRatio();
        int maxRetryTimes = retryDelayProperties.getMaxRetryTimes();
        List<Integer> retryDelays = retryDelayProperties.getDelays();

        return new EventRetrySchedulerImpl(
                eventConsumeRepository,
                eventListeners,
                failureHandlers,
                virtualThreadExecutor,
                recordConverter,
                eventPublishRepository,
                batchSize,
                highPriorityRatio,
                maxRetryTimes,
                retryDelays);
    }

}

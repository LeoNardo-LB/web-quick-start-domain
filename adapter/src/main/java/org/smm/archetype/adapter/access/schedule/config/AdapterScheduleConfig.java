package org.smm.archetype.adapter.access.schedule.config;

import org.smm.archetype.adapter.access.listener.EventListener;
import org.smm.archetype.adapter.access.schedule.EventRetrySchedulerImpl;
import org.smm.archetype.infrastructure._shared.event.EventConsumeRepository;
import org.smm.archetype.infrastructure._shared.event.EventSerializer;
import org.smm.archetype.infrastructure._shared.event.handler.EventFailureHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.List;

/**
 * Adapter层定时任务配置
 *
 * <p>负责创建定时任务的Bean，如事件重试调度器等。
 * @author Leonardo
 * @since 2026-01-10
 */
@Configuration
@EnableScheduling
public class AdapterScheduleConfig {

    /**
     * 事件重试调度器
     *
     * <p>定时扫描失败的事件，按照重试策略进行重试。
     * @param eventConsumeRepository 事件消费仓储
     * @param eventListeners         所有事件监听器
     * @param eventSerializer        事件序列化器
     * @param failureHandlers        所有失败处理器
     * @return 事件重试调度器
     */
    @Bean
    public EventRetrySchedulerImpl eventRetryScheduler(
            final EventConsumeRepository eventConsumeRepository,
            final List<EventListener> eventListeners,
            final EventSerializer eventSerializer,
            final List<EventFailureHandler> failureHandlers) {
        return new EventRetrySchedulerImpl(eventConsumeRepository, eventListeners, eventSerializer, failureHandlers);
    }

}

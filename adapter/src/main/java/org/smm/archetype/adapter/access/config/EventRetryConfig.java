package org.smm.archetype.adapter.access.config;

import lombok.RequiredArgsConstructor;
import org.smm.archetype.adapter.access.listener.EventListener;
import org.smm.archetype.adapter.access.schedule.EventRetryScheduler;
import org.smm.archetype.adapter.access.schedule.EventRetrySchedulerImpl;
import org.smm.archetype.infrastructure._shared.event.EventConsumeRepository;
import org.smm.archetype.infrastructure._shared.event.EventSerializer;
import org.smm.archetype.infrastructure._shared.event.handler.EventFailureHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 事件重试配置类
 *
 * <p>配置事件重试相关的 Bean。
 * @author Leonardo
 * @since 2026/01/09
 */
@Configuration
@RequiredArgsConstructor
public class EventRetryConfig {

    private final EventConsumeRepository    eventConsumeRepository;
    private final List<EventListener>       eventListeners;
    private final EventSerializer           eventSerializer;
    private final List<EventFailureHandler> failureHandlers;

    /**
     * 配置 EventRetryScheduler
     * @return EventRetryScheduler
     */
    @Bean
    public EventRetryScheduler eventRetryScheduler() {
        return new EventRetrySchedulerImpl(
                eventConsumeRepository,
                eventListeners,
                eventSerializer,
                failureHandlers);
    }

}

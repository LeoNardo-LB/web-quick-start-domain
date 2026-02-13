package org.smm.archetype.config;

import org.smm.archetype.adapter.event.EventDispatcher;
import org.smm.archetype.adapter.schedule.EventRetrySchedulerImpl;
import org.smm.archetype.config.properties.EventProperties;
import org.smm.archetype.infrastructure.shared.event.persistence.EventRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.concurrent.ExecutorService;

/**
 * 定时任务配置类，创建事件重试调度器等定时任务Bean。
 */
@Configuration
@EnableScheduling
@EnableConfigurationProperties(EventProperties.class)
public class ScheduleConfigure {

    /**
     * 事件重试调度器
     *
    定时扫描 RETRYING 状态的事件，交给 EventDispatcher 进行重试处理。
     *
     * @param eventRepository       事件仓储
     * @param eventDispatcher       事件分发器
     * @param virtualThreadExecutor 虚拟线程池
     * @param eventProperties       事件配置属性
     * @return 事件重试调度器
     */
    @Bean
    public EventRetrySchedulerImpl eventRetryScheduler(
            EventRepository eventRepository,
            EventDispatcher eventDispatcher,
            @Qualifier("virtualThreadExecutor") ExecutorService virtualThreadExecutor,
            EventProperties eventProperties) {

        int batchSize = eventProperties.getRetry().getBatchSize();

        return new EventRetrySchedulerImpl(
                eventRepository,
                eventDispatcher,
                virtualThreadExecutor,
                batchSize);
    }

}

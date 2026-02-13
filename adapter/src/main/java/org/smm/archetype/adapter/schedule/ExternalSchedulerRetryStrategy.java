package org.smm.archetype.adapter.schedule;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import java.time.Instant;

/**
 * 外部调度框架重试策略，适用于XXL-JOB、PowerJob等。
 * 注意：此类不使用 @Component 注解，Bean 装配由 EventConfigure 配置类统一管理。
 */
@Slf4j
public class ExternalSchedulerRetryStrategy implements RetryStrategy {

    /**
     * 重试间隔（分钟）。
     */
    @Value("${middleware.event.retry.interval-minutes:5}")
    private int retryIntervalMinutes;

    @Override
    public Instant calculateNextRetryTime(int retryTimes) {
        log.debug("外部调度器重试策略: retryTimes={}, nextRetry={}min",
                retryTimes, retryIntervalMinutes);

        // 返回预估的下次重试时间
        // 实际重试时间由外部调度框架配置决定
        return Instant.now().plusSeconds(retryIntervalMinutes * 60L);
    }

    @Override
    public boolean shouldRetry(int currentRetryTimes, int maxRetryTimes) {
        // 外部调度框架有自己的重试次数控制
        // 这里返回true，让外部框架来决定是否重试
        return true;
    }
}

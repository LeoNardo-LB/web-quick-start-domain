package org.smm.archetype.adapter.schedule;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * 指数退避重试策略，默认重试延迟：1分钟→5分钟→15分钟→30分钟→60分钟。
 */
@Slf4j
@Component
@ConditionalOnProperty(
    prefix = "middleware.event.retry",
    name = "strategy",
    havingValue = "exponential",
    matchIfMissing = true  // 默认策略
)
public class ExponentialBackoffRetryStrategy implements RetryStrategy {

    /**
     * 指数退避延迟数组（单位：分钟）
     */
    private static final int[] DELAYS = {1, 5, 15, 30, 60};

    @Override
    public Instant calculateNextRetryTime(int retryTimes) {
        int index = Math.min(retryTimes - 1, DELAYS.length - 1);
        int delayMinutes = DELAYS[index];

        log.debug("计算下次重试时间: retryTimes={}, delay={}min", retryTimes, delayMinutes);

        return Instant.now().plusSeconds(delayMinutes * 60L);
    }

}

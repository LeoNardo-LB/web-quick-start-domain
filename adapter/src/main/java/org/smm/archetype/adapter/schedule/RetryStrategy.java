package org.smm.archetype.adapter.schedule;

import java.time.Instant;

/**
 * 重试策略接口，定义事件消费失败时的重试策略。
 */
public interface RetryStrategy {

/**
     * 计算下次重试时间。
     * @param retryTimes 当前重试次数
     * @return 下次重试时间
     */
    Instant calculateNextRetryTime(int retryTimes);

/**
     * 判断是否应该重试。
     * @param currentRetryTimes 当前重试次数
     * @param maxRetryTimes 最大重试次数
     * @return true-应该重试
     */
    default boolean shouldRetry(int currentRetryTimes, int maxRetryTimes) {
        return currentRetryTimes < maxRetryTimes;
    }
}

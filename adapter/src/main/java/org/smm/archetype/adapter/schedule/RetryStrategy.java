package org.smm.archetype.adapter.schedule;

import java.time.Instant;

/**
 * 重试策略接口
 *
 * <p>定义事件消费失败时的重试策略，支持多种重试算法和外部调度框架集成。
 *
 * <p>使用场景：
 * <ul>
 *   <li>指数退避重试（默认）</li>
 *   <li>固定间隔重试</li>
 *   <li>外部调度框架（XXL-JOB、PowerJob等）</li>
 * </ul>
 *
 * <p>扩展方式：
 * <pre>{@code
 * // 使用内置的外部调度策略（支持XXL-JOB、PowerJob等）
 * // 在application.yml中配置：
 * // middleware:
 * //   event:
 * //     retry:
 * //       strategy: external-scheduler
 * //       interval-minutes: 5
 *
 * // 或者实现自定义策略：
 * @Component
 * @ConditionalOnProperty(name="retry.strategy", havingValue="custom")
 * public class CustomRetryStrategy implements RetryStrategy {
 *     @Override
 *     public Instant calculateNextRetryTime(int retryTimes) {
 *         // 自定义的重试逻辑
 *         return Instant.now().plusSeconds(60);
 *     }
 * }
 * }</pre>
 *
 * @author Leonardo
 * @since 2026-01-16
 */
public interface RetryStrategy {

    /**
     * 计算下次重试时间
     *
     * <p>根据当前重试次数计算下次重试的时间点。
     *
     * @param retryTimes 当前重试次数（从1开始）
     * @return 下次重试时间
     */
    Instant calculateNextRetryTime(int retryTimes);

    /**
     * 判断是否应该重试
     *
     * <p>根据当前重试次数和最大重试次数判断是否继续重试。
     *
     * @param currentRetryTimes 当前重试次数
     * @param maxRetryTimes     最大重试次数
     * @return true-应该重试，false-不再重试
     */
    default boolean shouldRetry(int currentRetryTimes, int maxRetryTimes) {
        return currentRetryTimes < maxRetryTimes;
    }
}

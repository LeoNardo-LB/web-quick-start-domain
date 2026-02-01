package org.smm.archetype.adapter.schedule;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * 外部调度框架重试策略（通用实现）
 *
 * <p>适用于所有外部任务调度框架，如：
 * <ul>
 *   <li>XXL-JOB</li>
 *   <li>PowerJob</li>
 *   <li>SchedulerX</li>
 *   <li>其他分布式任务调度框架</li>
 * </ul>
 *
 * <p>设计理念：
 * <ul>
 *   <li>外部调度框架负责任务调度和重试触发</li>
 *   <li>此类仅返回预估的下次重试时间，用于数据库记录</li>
 *   <li>实际重试时间由外部框架配置决定</li>
 * </ul>
 *
 * <p>接入方式：
 * <ol>
 *   <li>在外部调度框架控制台配置任务</li>
 *   <li>设置重试间隔和次数</li>
 *   <li>在application.yml中启用此策略</li>
 * </ol>
 *
 * <p>配置示例（application.yml）：
 * <pre>
 * middleware:
 *   event:
 *     retry:
 *       strategy: external-scheduler
 *       interval-minutes: 5
 * </pre>
 *
 * <p>支持的框架：XXL-JOB、PowerJob、SchedulerX等
 * <p>配置项说明：
 * <ul>
 *   <li>strategy: 重试策略，固定值为 external-scheduler</li>
 *   <li>interval-minutes: 重试间隔（分钟），默认5分钟</li>
 * </ul>
 *
 * @author Leonardo
 * @since 2026-01-16
 * @see <a href="https://www.xuxueli.com/xxl-job/">XXL-JOB官方文档</a>
 * @see <a href="https://www.powerjob.tech/">PowerJob官方文档</a>
 */
@Slf4j
@Component
@ConditionalOnProperty(
    prefix = "middleware.event.retry",
    name = "strategy",
    havingValue = "external-scheduler"
)
public class ExternalSchedulerRetryStrategy implements RetryStrategy {

    /**
     * 重试间隔（单位：分钟）
     *
     * <p>可通过配置文件覆盖，默认5分钟
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

package org.smm.archetype.domain.bizshared.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.smm.archetype.domain.bizshared.base.ValueObject;

import java.time.Instant;

/**
 * 事件消费记录值对象
 *
 * <p>封装事件消费过程中的状态信息，用于跨层传递。
 *
 * <p>职责：
 * <ul>
 *   <li>封装消费状态信息</li>
 *   <li>提供类型安全的访问</li>
 *   <li>避免跨层直接依赖DO对象</li>
 *   <li>提供业务方法判断消费状态</li>
 * </ul>
 *
 * <p>设计说明：
 * <ul>
 *   <li>继承ValueObject，基于值的相等性</li>
 *   <li>所有字段final，确保不可变性</li>
 *   <li>使用Builder模式，便于创建对象</li>
 * </ul>
 *
 * <p>注意：from()转换方法应在Infrastructure层或App层实现，以避免Domain层依赖Infrastructure层。
 * @author Leonardo
 * @since 2026/01/10
 */
@Getter
@Builder(setterPrefix = "set", toBuilder = true)
@AllArgsConstructor
public class EventConsumeRecord extends ValueObject {

    /**
     * 事件ID，关联event_publish.event_id
     */
    private final String eventId;

    /**
     * 优先级：HIGH(高)/LOW(低)
     */
    private final EventPriority priority;

    /**
     * 幂等键（防止重复消费）
     */
    private final String idempotentKey;

    /**
     * 消费者组
     */
    private final String consumerGroup;

    /**
     * 消费者名称
     */
    private final String consumerName;

    /**
     * 消费状态：READY(准备消费)/CONSUMED(已消费)/RETRY(重试中)/FAILED(失败)
     */
    private final ConsumeStatus consumeStatus;

    /**
     * 消费开始时间
     */
    private final Instant consumeTime;

    /**
     * 消费完成时间
     */
    private final Instant completeTime;

    /**
     * 下次重试时间
     */
    private final Instant nextRetryTime;

    /**
     * 当前重试次数
     */
    private final Integer retryTimes;

    /**
     * 最大重试次数
     */
    private final Integer maxRetryTimes;

    /**
     * 错误信息
     */
    private final String errorMessage;

    /**
     * 创建时间
     */
    private final Instant createTime;

    /**
     * 版本号（乐观锁）
     */
    private final Long version;

    /**
     * 判断是否可以重试
     *
     * <p>重试条件：
     * <ul>
     *   <li>当前重试次数 < 最大重试次数</li>
     *   <li>消费状态为RETRY</li>
     * </ul>
     * @return true-可以重试，false-不可以重试
     */
    public boolean canRetry() {
        return retryTimes != null
                       && maxRetryTimes != null
                       && retryTimes < maxRetryTimes
                       && consumeStatus != null
                       && consumeStatus.canRetry();
    }

    /**
     * 判断是否处理完成
     *
     * <p>完成状态包括：
     * <ul>
     *   <li>CONSUMED - 成功消费</li>
     *   <li>FAILED - 重试次数用尽，失败</li>
     * </ul>
     * @return true-已完成，false-未完成
     */
    public boolean isCompleted() {
        return consumeStatus != null && consumeStatus.isCompleted();
    }

    /**
     * 判断是否消费成功
     * @return true-成功，false-未成功
     */
    public boolean isSuccess() {
        return consumeStatus != null && consumeStatus.isSuccess();
    }

    /**
     * 获取剩余重试次数
     * @return 剩余重试次数，如果maxRetryTimes为null则返回0
     */
    public int getRemainingRetries() {
        if (retryTimes == null || maxRetryTimes == null) {
            return 0;
        }
        return Math.max(0, maxRetryTimes - retryTimes);
    }

}

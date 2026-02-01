package org.smm.archetype.infrastructure.bizshared.event.repository;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.smm.archetype.domain.bizshared.base.Entity;
import org.smm.archetype.domain.bizshared.event.Action;
import org.smm.archetype.domain.bizshared.event.Source;
import org.smm.archetype.domain.bizshared.event.Status;
import org.smm.archetype.domain.bizshared.event.Type;

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
@SuperBuilder(setterPrefix = "set")
public class EventConsumeRecord extends Entity {

    /**
     * 事件ID，关联event_publish.event_id
     */
    private final String eid;

    /**
     * 事件动作
     */
    private final Action action;

    /**
     * 事件来源
     */
    private final Source source;

    /**
     * 事件类型
     */
    private final Type type;

    /**
     * 消费状态：READY(准备消费)/CONSUMED(已消费)/RETRY(重试中)/FAILED(失败)
     */
    private final Status status;

    /**
     * 载荷
     */
    private final String payload;

    /**
     * 执行者
     */
    private final String executor;

    /**
     * 执行者组
     */
    private final String executor_group;

    /**
     * 消息内容
     */
    private final String message;

    /**
     * traceId
     */
    private final String traceId;

    /**
     * 当前重试次数
     */
    private final Integer retryTimes;

    /**
     * 下次重试时间
     */
    private final Instant nextRetryTime;

    /**
     * 最大重试次数
     */
    private final Integer maxRetryTimes;

    // ==================== 别名方法（兼容性） ====================

    /**
     * 获取错误消息（别名）
     *
     * @return 错误消息
     */
    public String getErrorMessage() {
        return this.message;
    }

    /**
     * 获取消费者组（别名）
     *
     * @return 消费者组
     */
    public String getConsumerGroup() {
        return this.executor_group;
    }

    /**
     * 获取消费者名称（别名）
     *
     * @return 消费者名称
     */
    public String getConsumerName() {
        return this.executor;
    }

}

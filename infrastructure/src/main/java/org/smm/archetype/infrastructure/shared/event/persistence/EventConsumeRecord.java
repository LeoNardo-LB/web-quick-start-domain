package org.smm.archetype.infrastructure.shared.event.persistence;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.smm.archetype.domain.shared.base.Entity;
import org.smm.archetype.domain.shared.event.Action;
import org.smm.archetype.domain.shared.event.Source;
import org.smm.archetype.domain.shared.event.Status;
import org.smm.archetype.domain.shared.event.Type;

import java.time.Instant;

/**
 * 事件消费记录实体。


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
    private final String executorGroup;

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
        return this.executorGroup;
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

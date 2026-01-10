package org.smm.archetype.infrastructure._shared.generated.repository.entity;

import com.mybatisflex.annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.smm.archetype.infrastructure._shared.dal.BaseDO;
import org.smm.archetype.infrastructure._shared.dal.BaseDOFillListener;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

/**
 * 事件消费表 实体类。
 *
 * @author Administrator
 * @since 2026-01-10
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Table(value = "event_consume", onInsert = BaseDOFillListener.class, onUpdate = BaseDOFillListener.class)
public class EventConsumeDO extends BaseDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 事件ID，关联event_publish.event_id
     */
    private String eventId;

    /**
     * 优先级：HIGH(高)/LOW(低)
     */
    private String priority;

    /**
     * 幂等键（防止重复消费）
     */
    private String idempotentKey;

    /**
     * 消费者组
     */
    private String consumerGroup;

    /**
     * 消费者名称
     */
    private String consumerName;

    /**
     * 消费状态：READY(准备消费)/CONSUMED(已消费)/RETRY(重试中)/FAILED(失败)
     */
    private String consumeStatus;

    /**
     * 消费开始时间
     */
    private Instant consumeTime;

    /**
     * 消费完成时间
     */
    private Instant completeTime;

    /**
     * 下次重试时间
     */
    private Instant nextRetryTime;

    /**
     * 当前重试次数
     */
    private Integer retryTimes;

    /**
     * 最大重试次数
     */
    private Integer maxRetryTimes;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 版本号（乐观锁）
     */
    private Long version;

}

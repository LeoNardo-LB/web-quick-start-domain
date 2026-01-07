package org.smm.archetype.infrastructure._shared.component.event.repository.entity;

import com.mybatisflex.annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.smm.archetype.infrastructure._shared.dal.entity.BaseDO;
import org.smm.archetype.infrastructure._shared.dal.listener.BaseDOFillListener;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

/**
 * 事件消费表 实体类。
 * @author Administrator
 * @since 2025-12-31
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
     * 事件ID，关联 event_publish.id
     */
    private String eventId;

    /**
     * 消费者组
     */
    private String consumerGroup;

    /**
     * 消费者名称
     */
    private String consumerName;

    /**
     * 消费状态：PENDING/CONSUMING/SUCCESS/FAILED
     */
    private String consumeStatus;

    /**
     * 消费时间
     */
    private Instant consumeTime;

    /**
     * 完成时间
     */
    private Instant completeTime;

    /**
     * 重试次数
     */
    private Integer retryTimes;

    /**
     * 最大重试次数
     */
    private Integer maxRetryTimes;

    /**
     * 下次重试时间
     */
    private Instant nextRetryTime;

    /**
     * 错误信息
     */
    private String errorMessage;

}

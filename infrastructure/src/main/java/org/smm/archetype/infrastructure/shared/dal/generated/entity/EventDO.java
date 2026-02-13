package org.smm.archetype.infrastructure.shared.dal.generated.entity;

import com.mybatisflex.annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.smm.archetype.infrastructure.shared.dal.BaseDO;
import org.smm.archetype.infrastructure.shared.dal.BaseDOFillListener;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

/**
 * 事件发布表DO实体。


 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Table(value = "event", onInsert = BaseDOFillListener.class, onUpdate = BaseDOFillListener.class)
public class EventDO extends BaseDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 事件id
     */
    private String eid;

    /**
     * 事件动作 PUBLISH(发布)/CONSUME(消费)
     */
    private String action;

    /**
     * 事件来源 INTERNAL(内部)/xxx(外部)
     */
    private String source;

    /**
     * 事件类型
     */
    private String type;

    /**
     * 事件状态：PUBLISH CREATED(已创建)/READY(就绪)/PUBLISHED(已发布)；CONSUME READY(准备消费)/PROCESSING(处理中)/CONSUMED(已消费)/RETRY(重试中)/FAILED(失败)
     */
    private String status;

    /**
     * 事件载荷（JSON格式）
     */
    private String payload;

    /**
     * 执行者 发布者id或消费者id
     */
    private String executor;

    /**
     * 执行者组 发布者组或消费者组
     */
    private String executorGroup;

    /**
     * 事件消息
     */
    private String message;

    /**
     * 跟踪ID
     */
    private String traceId;

    /**
     * 当前重试次数
     */
    private Integer retryTimes = 5;

    /**
     * 最大重试次数
     */
    private Integer maxRetryTimes;

    /**
     * 下次重试时间
     */
    private Instant nextRetryTime;

    /**
     * 删除标记：0=未删除，非0=删除时间戳
     */
    private Long deleteTime;

    /**
     * 删除人ID
     */
    private String deleteUser;

}

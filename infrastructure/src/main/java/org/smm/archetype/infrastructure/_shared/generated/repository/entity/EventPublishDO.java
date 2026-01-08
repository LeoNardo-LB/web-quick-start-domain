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
 * 事件发布表 实体类。
 *
 * @author Administrator
 * @since 2026-01-09
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Table(value = "event_publish", onInsert = BaseDOFillListener.class, onUpdate = BaseDOFillListener.class)
public class EventPublishDO extends BaseDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 事件唯一标识（UUID）
     */
    private String eventId;

    /**
     * 聚合根ID
     */
    private String aggregateId;

    /**
     * 聚合根类型（如Order、Customer）
     */
    private String aggregateType;

    /**
     * 优先级：HIGH(高)/LOW(低)
     */
    private String priority;

    /**
     * 事件发生时间
     */
    private Instant occurredOn;

    /**
     * 前驱事件ID
     */
    private Long prevId;

    /**
     * 步骤，第几步
     */
    private Integer step;

    /**
     * 事件来源
     */
    private String source;

    /**
     * 事件类型
     */
    private String type;

    /**
     * 事件载荷（JSON格式）
     */
    private String data;

    /**
     * 事件状态：CREATED(已创建)/READY(就绪)/PUBLISHED(已发布)
     */
    private String status;

    /**
     * 最大重试次数
     */
    private Integer maxRetryTimes;

    /**
     * 版本号（乐观锁）
     */
    private Long version;

}

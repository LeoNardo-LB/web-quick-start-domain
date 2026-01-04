package org.smm.archetype.common.event.dal.entity;

import com.mybatisflex.annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.smm.archetype.shared.dal.entity.BaseDO;
import org.smm.archetype.shared.dal.listener.BaseDOFillListener;

import java.io.Serial;
import java.io.Serializable;

/**
 * 事件发布表 实体类。
 * @author Administrator
 * @since 2025-12-31
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
     * 事件状态：CREATED/READY/PUBLISHED
     */
    private String status;

}

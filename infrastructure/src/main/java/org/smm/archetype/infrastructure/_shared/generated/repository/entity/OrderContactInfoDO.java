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
 * 订单联系信息表-值对象 实体类。
 * @author Administrator
 * @since 2026-01-11
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Table(value = "order_contact_info", onInsert = BaseDOFillListener.class, onUpdate = BaseDOFillListener.class)
public class OrderContactInfoDO extends BaseDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 联系人姓名
     */
    private String contactName;

    /**
     * 联系人电话
     */
    private String contactPhone;

    /**
     * 联系人邮箱
     */
    private String contactEmail;

    /**
     * 删除时间
     */
    private Instant deleteTime;

    /**
     * 删除人ID
     */
    private String deleteUser;

}

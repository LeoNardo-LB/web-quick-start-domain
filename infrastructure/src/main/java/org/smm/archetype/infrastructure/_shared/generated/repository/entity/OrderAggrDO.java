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
import java.math.BigDecimal;
import java.time.Instant;

/**
 * 订单表-聚合根 实体类。
 * @author Administrator
 * @since 2026-01-11
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Table(value = "order_aggr", onInsert = BaseDOFillListener.class, onUpdate = BaseDOFillListener.class)
public class OrderAggrDO extends BaseDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 订单编号
     */
    private String orderNo;

    /**
     * 客户ID
     */
    private String customerId;

    /**
     * 客户姓名
     */
    private String customerName;

    /**
     * 订单状态：CREATED-已创建, PAID-已支付, CANCELLED-已取消, SHIPPED-已发货, COMPLETED-已完成
     */
    private String status;

    /**
     * 订单总金额
     */
    private BigDecimal totalAmount;

    /**
     * 货币类型
     */
    private String currency;

    /**
     * 支付方式
     */
    private String paymentMethod;

    /**
     * 备注
     */
    private String remark;

    /**
     * 删除时间
     */
    private Instant deleteTime;

    /**
     * 删除人ID
     */
    private String deleteUser;

}

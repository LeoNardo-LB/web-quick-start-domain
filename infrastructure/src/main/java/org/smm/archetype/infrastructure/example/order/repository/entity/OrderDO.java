package org.smm.archetype.infrastructure.example.order.repository.entity;

import com.mybatisflex.annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.smm.archetype.infrastructure._shared.dal.BaseDO;
import org.smm.archetype.infrastructure._shared.dal.BaseDOFillListener;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * 订单数据对象
 * @author Leonardo
 * @since 2025/12/30
 */
@Getter
@Setter
@Builder(setterPrefix = "set")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Table(value = "order", onInsert = BaseDOFillListener.class, onUpdate = BaseDOFillListener.class)
public class OrderDO extends BaseDO {

    /**
     * 订单ID（对应聚合根的业务ID）
     */
    private Long orderId;

    /**
     * 客户ID
     */
    private Long customerId;

    /**
     * 订单总金额
     */
    private BigDecimal totalAmount;

    /**
     * 币种
     */
    private String currency;

    /**
     * 订单状态
     */
    private String status;

    /**
     * 收货地址
     */
    private String shippingAddress;

    /**
     * 联系电话
     */
    private String phoneNumber;

    /**
     * 支付时间
     */
    private Instant paymentTime;

    /**
     * 发货时间
     */
    private Instant shippingTime;

    /**
     * 完成时间
     */
    private Instant completedTime;

    /**
     * 取消时间
     */
    private Instant cancelledTime;

    /**
     * 取消原因
     */
    private String cancelReason;

    /**
     * 版本号（乐观锁）
     */
    private Long version;

}

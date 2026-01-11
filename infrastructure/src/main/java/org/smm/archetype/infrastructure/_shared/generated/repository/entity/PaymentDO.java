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
 * 支付记录表 实体类。
 * @author Administrator
 * @since 2026-01-11
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Table(value = "payment", onInsert = BaseDOFillListener.class, onUpdate = BaseDOFillListener.class)
public class PaymentDO extends BaseDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 支付编号
     */
    private String paymentNo;

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 订单编号
     */
    private String orderNo;

    /**
     * 支付方式：ALIPAY-支付宝, WECHAT-微信, STRIPE-Stripe
     */
    private String paymentMethod;

    /**
     * 支付金额
     */
    private BigDecimal amount;

    /**
     * 货币类型
     */
    private String currency;

    /**
     * 支付状态：PENDING-待支付, SUCCESS-成功, FAILED-失败, REFUNDED-已退款
     */
    private String status;

    /**
     * 第三方交易ID
     */
    private String transactionId;

    /**
     * 失败原因
     */
    private String failedReason;

    /**
     * 删除时间
     */
    private Instant deleteTime;

    /**
     * 删除人ID
     */
    private String deleteUser;

}

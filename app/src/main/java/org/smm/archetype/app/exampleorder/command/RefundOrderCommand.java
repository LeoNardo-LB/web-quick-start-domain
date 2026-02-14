package org.smm.archetype.app.exampleorder.command;

import lombok.Getter;
import lombok.Setter;
import org.smm.archetype.domain.shared.base.Command;

/**
 * 退款订单命令，包含订单ID、退款金额、退款类型和退款原因。
 */
@Setter
@Getter
public class RefundOrderCommand implements Command {

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 退款金额（金额值）
     */
    private String refundAmount;

    /**
     * 币种
     */
    private String currency;

    /**
     * 退款类型（FULL/PARTIAL）
     */
    private String refundType;

    /**
     * 退款原因
     */
    private String refundReason;

    public RefundOrderCommand() {
    }

    /**
     * 全参数构造方法
     * @param orderId      订单ID
     * @param refundAmount 退款金额
     * @param currency     币种
     * @param refundType   退款类型
     * @param refundReason 退款原因
     */
    public RefundOrderCommand(Long orderId, String refundAmount, String currency,
                              String refundType, String refundReason) {
        this.orderId = orderId;
        this.refundAmount = refundAmount;
        this.currency = currency;
        this.refundType = refundType;
        this.refundReason = refundReason;
    }

}

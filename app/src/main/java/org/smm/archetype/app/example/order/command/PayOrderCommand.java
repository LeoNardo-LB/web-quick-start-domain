package org.smm.archetype.app.example.order.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.smm.archetype.domain._shared.base.Command;

/**
 * 支付订单命令
 * @author Leonardo
 * @since 2025/12/30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayOrderCommand implements Command {

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 支付方式
     */
    private String paymentMethod;

}

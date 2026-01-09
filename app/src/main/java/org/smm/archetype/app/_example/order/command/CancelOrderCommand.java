package org.smm.archetype.app._example.order.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.smm.archetype.domain._shared.base.Command;

/**
 * 取消订单命令
 * @author Leonardo
 * @since 2025/12/30
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CancelOrderCommand implements Command {

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 取消原因
     */
    private String reason;

}

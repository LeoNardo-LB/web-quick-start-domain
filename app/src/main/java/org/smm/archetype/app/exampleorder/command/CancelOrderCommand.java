package org.smm.archetype.app.exampleorder.command;

import lombok.Getter;
import lombok.Setter;
import org.smm.archetype.domain.shared.base.Command;

/**
 * 取消订单命令，包含订单ID和取消原因。
 */
@Setter
@Getter
public class CancelOrderCommand implements Command {

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 取消原因
     */
    private String reason;

    public CancelOrderCommand() {
    }

    /**
     * 全参数构造方法
     * @param orderId 订单ID
     * @param reason  取消原因
     */
    public CancelOrderCommand(Long orderId, String reason) {
        this.orderId = orderId;
        this.reason = reason;
    }

}

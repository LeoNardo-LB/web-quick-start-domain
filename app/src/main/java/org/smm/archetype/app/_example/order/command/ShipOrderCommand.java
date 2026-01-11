package org.smm.archetype.app._example.order.command;

import org.smm.archetype.domain._shared.base.Command;

/**
 * 发货订单命令
 * @author Leonardo
 * @since 2026/1/11
 */
public class ShipOrderCommand implements Command {

    /**
     * 订单ID
     */
    private Long orderId;

    public ShipOrderCommand() {
    }

    public ShipOrderCommand(Long orderId) {
        this.orderId = orderId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

}

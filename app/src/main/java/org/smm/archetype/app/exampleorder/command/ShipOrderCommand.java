package org.smm.archetype.app.exampleorder.command;

import lombok.Getter;
import lombok.Setter;
import org.smm.archetype.domain.shared.base.Command;

/**
 * 发货订单命令，包含订单ID。
 */
@Setter
@Getter
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

}

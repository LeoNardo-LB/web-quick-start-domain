package org.smm.archetype.app.example.order.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.smm.archetype.domain._shared.base.Command;
import org.smm.archetype.domain.example.order.model.OrderItem;

import java.util.List;

/**
 * 创建订单命令
 *
 * <p>命令示例：展示如何定义命令
 * @author Leonardo
 * @since 2025/12/30
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderCommand implements Command {

    /**
     * 客户ID
     */
    private Long customerId;

    /**
     * 订单项列表
     */
    private List<OrderItem> items;

    /**
     * 收货地址
     */
    private String shippingAddress;

    /**
     * 联系电话
     */
    private String phoneNumber;

}

package org.smm.archetype.app.exampleorder.command;

import lombok.Getter;
import lombok.Setter;
import org.smm.archetype.domain.shared.base.Command;
import org.smm.archetype.domain.exampleorder.model.valueobject.Address;
import org.smm.archetype.domain.exampleorder.model.valueobject.ContactInfo;
import org.smm.archetype.domain.exampleorder.model.valueobject.Money;
import org.smm.archetype.domain.exampleorder.model.valueobject.OrderItemInfo;

import java.util.List;

/**
 * 创建订单命令，包含订单基本信息和订单项。
 */
@Setter
@Getter
public class CreateOrderCommand implements Command {

    /**
     * 客户ID
     */
    private String customerId;

    /**
     * 客户名称
     */
    private String customerName;

    /**
     * 订单项列表
     */
    private List<OrderItemInfo> items;

    /**
     * 总金额
     */
    private Money totalAmount;

    /**
     * 收货地址
     */
    private Address shippingAddress;

    /**
     * 联系信息
     */
    private ContactInfo contactInfo;

    /**
     * 备注
     */
    private String remark;

    public CreateOrderCommand() {
    }

    public CreateOrderCommand(String customerId, String customerName, List<OrderItemInfo> items,
                              Money totalAmount, Address shippingAddress, ContactInfo contactInfo, String remark) {
        this.customerId = customerId;
        this.customerName = customerName;
        this.items = items;
        this.totalAmount = totalAmount;
        this.shippingAddress = shippingAddress;
        this.contactInfo = contactInfo;
        this.remark = remark;
    }

}

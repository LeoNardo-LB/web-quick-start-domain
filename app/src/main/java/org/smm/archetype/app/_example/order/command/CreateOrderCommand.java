package org.smm.archetype.app._example.order.command;

import org.smm.archetype.domain._example.order.model.valueobject.Address;
import org.smm.archetype.domain._example.order.model.valueobject.ContactInfo;
import org.smm.archetype.domain._example.order.model.valueobject.Money;
import org.smm.archetype.domain._example.order.model.valueobject.OrderItemInfo;
import org.smm.archetype.domain._shared.base.Command;

import java.util.List;

/**
 * 创建订单命令
 * @author Leonardo
 * @since 2026/1/11
 */
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

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public List<OrderItemInfo> getItems() {
        return items;
    }

    public void setItems(List<OrderItemInfo> items) {
        this.items = items;
    }

    public Money getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Money totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Address getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(Address shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public ContactInfo getContactInfo() {
        return contactInfo;
    }

    public void setContactInfo(ContactInfo contactInfo) {
        this.contactInfo = contactInfo;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

}

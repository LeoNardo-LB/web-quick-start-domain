package org.smm.archetype.app._example.dto;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.smm.archetype.domain.example.model.OrderStatus;
import org.smm.archetype.domain.example.model.PaymentMethod;

import java.time.Instant;
import java.util.List;

/**
 * 订单DTO
 * @author Leonardo
 * @since 2026/1/11
 */
@Getter
@Builder(setterPrefix = "set")
public class OrderDTO {

    /**
     * 订单ID
     */
    private Long id;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 客户ID
     */
    private String customerId;

    /**
     * 客户名称
     */
    private String customerName;

    /**
     * 订单状态
     */
    private OrderStatus status;

    /**
     * 支付方式
     */
    private PaymentMethod paymentMethod;

    /**
     * 总金额
     */
    private MoneyDTO totalAmount;

    /**
     * 订单项列表
     */
    private List<OrderItemDTO> items;

    /**
     * 收货地址
     */
    private AddressDTO shippingAddress;

    /**
     * 联系信息
     */
    private ContactInfoDTO contactInfo;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private Instant createTime;

    /**
     * 支付时间
     */
    private Instant paymentTime;

    /**
     * 发货时间
     */
    private Instant shippedTime;

    /**
     * 完成时间
     */
    private Instant completedTime;

    /**
     * 取消时间
     */
    private Instant cancelledTime;

    /**
     * 取消原因
     */
    private String cancelReason;

}

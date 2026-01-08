package org.smm.archetype.adapter.access.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * 订单DTO
 * @author Leonardo
 * @since 2025/12/30
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {

    /**
     * 数据库ID
     */
    private Long id;

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 客户ID
     */
    private Long customerId;

    /**
     * 订单项列表
     */
    private List<OrderItemDTO> items;

    /**
     * 订单总金额
     */
    private BigDecimal totalAmount;

    /**
     * 币种
     */
    private String currency;

    /**
     * 订单状态
     */
    private String status;

    /**
     * 收货地址
     */
    private String shippingAddress;

    /**
     * 联系电话
     */
    private String phoneNumber;

    /**
     * 支付时间
     */
    private Instant paymentTime;

    /**
     * 发货时间
     */
    private Instant shippingTime;

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

    /**
     * 创建时间
     */
    private Instant createTime;

    /**
     * 更新时间
     */
    private Instant updateTime;

}

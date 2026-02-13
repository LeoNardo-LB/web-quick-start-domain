package org.smm.archetype.adapter.exampleorder.web.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.smm.archetype.domain.exampleorder.model.OrderStatus;
import org.smm.archetype.domain.exampleorder.model.PaymentMethod;

import java.time.Instant;
import java.util.List;

/**
 * 订单响应对象，用于Web层返回订单数据。
 */
@Getter
@Builder(setterPrefix = "set")
public class OrderResponse {

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
    private MoneyResponse totalAmount;

    /**
     * 订单项列表
     */
    private List<OrderItemResponse> items;

    /**
     * 收货地址
     */
    private AddressResponse shippingAddress;

    /**
     * 联系信息
     */
    private ContactInfoResponse contactInfo;

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

    // ==================== 退款相关字段 ====================

    /**
     * 本次退款金额
     */
    private MoneyResponse refundedAmount;

    /**
     * 累计已退款金额
     */
    private MoneyResponse totalRefundedAmount;

    /**
     * 退款原因
     */
    private String refundReason;

    /**
     * 退款时间
     */
    private Instant refundedTime;

    /**
     * 退款类型（FULL/PARTIAL）
     */
    private String refundType;

}

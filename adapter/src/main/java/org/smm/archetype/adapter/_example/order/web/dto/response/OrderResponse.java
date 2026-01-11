package org.smm.archetype.adapter._example.order.web.dto.response;

import lombok.Getter;
import lombok.Setter;
import org.smm.archetype.app._example.order.dto.OrderDTO;
import org.smm.archetype.domain._example.order.model.OrderStatus;
import org.smm.archetype.domain._example.order.model.PaymentMethod;

import java.time.Instant;
import java.util.List;

/**
 * 订单响应
 * @author Leonardo
 * @since 2026/1/11
 */
@Getter
@Setter
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

    /**
     * 从DTO转换
     */
    public static OrderResponse fromDTO(OrderDTO dto) {
        OrderResponse response = new OrderResponse();
        response.setId(dto.getId());
        response.setOrderNo(dto.getOrderNo());
        response.setCustomerId(dto.getCustomerId());
        response.setCustomerName(dto.getCustomerName());
        response.setStatus(dto.getStatus());
        response.setPaymentMethod(dto.getPaymentMethod());
        response.setTotalAmount(MoneyResponse.fromDTO(dto.getTotalAmount()));
        response.setItems(dto.getItems().stream()
                                  .map(OrderItemResponse::fromDTO)
                                  .toList());
        response.setShippingAddress(AddressResponse.fromDTO(dto.getShippingAddress()));
        response.setContactInfo(ContactInfoResponse.fromDTO(dto.getContactInfo()));
        response.setRemark(dto.getRemark());
        response.setCreateTime(dto.getCreateTime());
        response.setPaymentTime(dto.getPaymentTime());
        response.setShippedTime(dto.getShippedTime());
        response.setCompletedTime(dto.getCompletedTime());
        response.setCancelledTime(dto.getCancelledTime());
        response.setCancelReason(dto.getCancelReason());
        return response;
    }

}

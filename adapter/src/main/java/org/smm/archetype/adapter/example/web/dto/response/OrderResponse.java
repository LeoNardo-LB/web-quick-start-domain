package org.smm.archetype.adapter.example.web.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.smm.archetype.app.example.dto.OrderDTO;
import org.smm.archetype.domain.example.model.OrderStatus;
import org.smm.archetype.domain.example.model.PaymentMethod;

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

    /**
     * 从DTO转换
     */
    public static OrderResponse fromDTO(OrderDTO dto) {
        OrderResponseBuilder builder = OrderResponse.builder();
        builder.setId(dto.getId());
        builder.setOrderNo(dto.getOrderNo());
        builder.setCustomerId(dto.getCustomerId());
        builder.setCustomerName(dto.getCustomerName());
        builder.setStatus(dto.getStatus());
        builder.setPaymentMethod(dto.getPaymentMethod());
        builder.setTotalAmount(MoneyResponse.fromDTO(dto.getTotalAmount()));
        builder.setItems(dto.getItems().stream()
                                  .map(OrderItemResponse::fromDTO)
                                  .toList());
        builder.setShippingAddress(AddressResponse.fromDTO(dto.getShippingAddress()));
        builder.setContactInfo(ContactInfoResponse.fromDTO(dto.getContactInfo()));
        builder.setRemark(dto.getRemark());
        builder.setCreateTime(dto.getCreateTime());
        builder.setPaymentTime(dto.getPaymentTime());
        builder.setShippedTime(dto.getShippedTime());
        builder.setCompletedTime(dto.getCompletedTime());
        builder.setCancelledTime(dto.getCancelledTime());
        builder.setCancelReason(dto.getCancelReason());
        return builder.build();
    }

}

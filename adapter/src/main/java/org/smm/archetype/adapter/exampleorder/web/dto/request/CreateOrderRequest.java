package org.smm.archetype.adapter.exampleorder.web.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import org.smm.archetype.domain.exampleorder.model.valueobject.Money;

import java.util.List;

/**
 * 创建订单请求对象，包含订单基本信息和订单项。
 */
@Getter
@Setter
public class CreateOrderRequest {

    /**
     * 客户ID
     */
    @NotBlank(message = "客户ID不能为空")
    private String customerId;

    /**
     * 客户名称
     */
    @NotBlank(message = "客户姓名不能为空")
    private String customerName;

    /**
     * 订单项列表
     */
    @NotEmpty(message = "订单项信息不能为空")
    private List<OrderItemRequest> items;

    /**
     * 总金额
     */
    private Money totalAmount;

    /**
     * 收货地址
     */
    private AddressRequest shippingAddress;

    /**
     * 联系信息
     */
    private ContactInfoRequest contactInfo;

    /**
     * 备注
     */
    private String remark;

    /**
     * 订单项请求
     */
    @Getter
    @Setter
    public static class OrderItemRequest {

        /**
         * 商品ID
         */
        @NotBlank(message = "商品ID不能为空")
        private String productId;

        /**
         * 商品名称
         */
        @NotBlank(message = "商品名称不能为空")
        private String productName;

        /**
         * SKU编码
         */
        @NotBlank(message = "SKU编码不能为空")
        private String skuCode;

        /**
         * 单价
         */
        private Money unitPrice;

        /**
         * 数量
         */
        private Integer quantity;

    }

    /**
     * 地址请求
     */
    @Getter
    @Setter
    public static class AddressRequest {

        /**
         * 省
         */
        @NotBlank(message = "省份不能为空")
        private String province;

        /**
         * 市
         */
        @NotBlank(message = "城市不能为空")
        private String city;

        /**
         * 区
         */
        private String district;

        /**
         * 详细地址
         */
        @NotBlank(message = "详细地址不能为空")
        private String detailAddress;

        /**
         * 邮政编码
         */
        private String postalCode;

    }

    /**
     * 联系信息请求
     */
    @Getter
    @Setter
    public static class ContactInfoRequest {

        /**
         * 联系人姓名
         */
        @NotBlank(message = "联系人姓名不能为空")
        private String contactName;

        /**
         * 联系电话
         */
        @NotBlank(message = "联系电话不能为空")
        private String contactPhone;

        /**
         * 联系邮箱
         */
        private String contactEmail;

    }

}

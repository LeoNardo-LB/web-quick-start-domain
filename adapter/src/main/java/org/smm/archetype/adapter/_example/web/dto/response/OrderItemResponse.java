package org.smm.archetype.adapter._example.web.dto.response;

import lombok.Getter;
import lombok.Setter;
import org.smm.archetype.app._example.dto.OrderItemDTO;

import java.math.BigDecimal;

/**
 * 订单项响应
 * @author Leonardo
 * @since 2026/1/11
 */
@Getter
@Setter
public class OrderItemResponse {

    /**
     * 商品ID
     */
    private String productId;

    /**
     * 商品名称
     */
    private String productName;

    /**
     * SKU编码
     */
    private String skuCode;

    /**
     * 单价
     */
    private BigDecimal unitPrice;

    /**
     * 数量
     */
    private Integer quantity;

    /**
     * 小计
     */
    private BigDecimal subtotal;

    /**
     * 币种
     */
    private String currency;

    /**
     * 从DTO转换
     */
    public static OrderItemResponse fromDTO(OrderItemDTO dto) {
        OrderItemResponse response = new OrderItemResponse();
        response.setProductId(dto.getProductId());
        response.setProductName(dto.getProductName());
        response.setSkuCode(dto.getSkuCode());
        response.setUnitPrice(dto.getUnitPrice());
        response.setQuantity(dto.getQuantity());
        response.setSubtotal(dto.getSubtotal());
        response.setCurrency(dto.getCurrency());
        return response;
    }

}

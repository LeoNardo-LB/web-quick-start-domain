package org.smm.archetype.adapter.exampleorder.web.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * 订单项响应
 */
@Getter
@Builder(setterPrefix = "set")
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

}

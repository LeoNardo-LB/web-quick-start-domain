package org.smm.archetype.app.exampleorder.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * 订单项数据传输对象。
 */
@Getter
@Builder(setterPrefix = "set")
public class OrderItemDTO {

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
     * 小计金额
     */
    private BigDecimal subtotal;

    /**
     * 货币类型
     */
    private String currency;

}

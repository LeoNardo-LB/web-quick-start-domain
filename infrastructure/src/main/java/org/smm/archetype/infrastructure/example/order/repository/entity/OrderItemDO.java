package org.smm.archetype.infrastructure.example.order.repository.entity;

import com.mybatisflex.annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.smm.archetype.infrastructure._shared.dal.BaseDO;

import java.math.BigDecimal;

/**
 * 订单项数据对象
 * @author Leonardo
 * @since 2025/12/30
 */
@Data
@Builder(setterPrefix = "set")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Table(value = "order_item")
public class OrderItemDO extends BaseDO {

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 产品ID
     */
    private String productId;

    /**
     * 产品名称
     */
    private String productName;

    /**
     * 单价
     */
    private BigDecimal unitPrice;

    /**
     * 币种
     */
    private String currency;

    /**
     * 数量
     */
    private Integer quantity;

    /**
     * 小计
     */
    private BigDecimal subtotal;

}

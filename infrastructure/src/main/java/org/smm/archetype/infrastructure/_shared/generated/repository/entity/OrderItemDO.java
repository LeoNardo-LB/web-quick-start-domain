package org.smm.archetype.infrastructure._shared.generated.repository.entity;

import com.mybatisflex.annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.smm.archetype.infrastructure._shared.dal.BaseDO;
import org.smm.archetype.infrastructure._shared.dal.BaseDOFillListener;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * 订单项表-实体 实体类。
 * @author Administrator
 * @since 2026-01-11
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Table(value = "order_item", onInsert = BaseDOFillListener.class, onUpdate = BaseDOFillListener.class)
public class OrderItemDO extends BaseDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 订单ID
     */
    private Long orderId;

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
     * 货币类型
     */
    private String currency;

    /**
     * 数量
     */
    private Integer quantity;

    /**
     * 小计金额
     */
    private BigDecimal subtotal;

    /**
     * 删除时间
     */
    private Instant deleteTime;

    /**
     * 删除人ID
     */
    private String deleteUser;

}

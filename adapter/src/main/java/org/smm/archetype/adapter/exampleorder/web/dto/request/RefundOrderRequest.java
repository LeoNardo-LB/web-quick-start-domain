package org.smm.archetype.adapter.exampleorder.web.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 退款订单请求
 */
@Setter
@Getter
public class RefundOrderRequest {

    /**
     * 退款金额
     */
    @NotNull(message = "退款金额不能为空")
    @DecimalMin(value = "0.01", message = "退款金额必须大于0")
    private BigDecimal refundAmount;

    /**
     * 币种
     */
    @NotBlank(message = "币种不能为空")
    private String currency;

    /**
     * 退款类型（FULL/PARTIAL）
     */
    @NotBlank(message = "退款类型不能为空")
    private String refundType;

    /**
     * 退款原因
     */
    @NotBlank(message = "退款原因不能为空")
    private String refundReason;

}

package org.smm.archetype.adapter.exampleorder.web.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * 金额响应
 */
@Getter
@Builder(setterPrefix = "set")
public class MoneyResponse {

    /**
     * 金额
     */
    private BigDecimal amount;

    /**
     * 币种
     */
    private String currency;

}

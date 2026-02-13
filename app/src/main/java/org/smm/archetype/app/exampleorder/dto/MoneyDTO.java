package org.smm.archetype.app.exampleorder.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * 金额数据传输对象。
 */
@Getter
@Builder(setterPrefix = "set")
public class MoneyDTO {

    /**
     * 金额
     */
    private BigDecimal amount;

    /**
     * 货币类型
     */
    private String currency;

}

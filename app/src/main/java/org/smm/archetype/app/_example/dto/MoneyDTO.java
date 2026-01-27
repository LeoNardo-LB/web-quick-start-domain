package org.smm.archetype.app._example.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 金额DTO
 * @author Leonardo
 * @since 2026/1/11
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

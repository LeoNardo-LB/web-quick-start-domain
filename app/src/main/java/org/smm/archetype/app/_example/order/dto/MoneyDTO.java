package org.smm.archetype.app._example.order.dto;

import java.math.BigDecimal;

/**
 * 金额DTO
 * @author Leonardo
 * @since 2026/1/11
 */
public class MoneyDTO {

    /**
     * 金额
     */
    private BigDecimal amount;

    /**
     * 货币类型
     */
    private String currency;

    public MoneyDTO() {
    }

    public MoneyDTO(BigDecimal amount, String currency) {
        this.amount = amount;
        this.currency = currency;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

}

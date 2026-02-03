package org.smm.archetype.adapter.example.web.dto.response;

import lombok.Getter;
import lombok.Setter;
import org.smm.archetype.app.example.dto.MoneyDTO;

import java.math.BigDecimal;

/**
 * 金额响应


 */
@Getter
@Setter
public class MoneyResponse {

    /**
     * 金额
     */
    private BigDecimal amount;

    /**
     * 币种
     */
    private String currency;

    /**
     * 从DTO转换
     */
    public static MoneyResponse fromDTO(MoneyDTO dto) {
        if (dto == null) {
            return null;
        }
        MoneyResponse response = new MoneyResponse();
        response.setAmount(dto.getAmount());
        response.setCurrency(dto.getCurrency());
        return response;
    }

}

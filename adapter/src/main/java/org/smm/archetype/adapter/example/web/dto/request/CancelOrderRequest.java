package org.smm.archetype.adapter.example.web.dto.request;

import lombok.Getter;
import lombok.Setter;

/**
 * 取消订单请求


 */
@Getter
@Setter
public class CancelOrderRequest {

    /**
     * 取消原因
     */
    private String reason;

}

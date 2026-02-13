package org.smm.archetype.adapter.exampleorder.web.dto.request;

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

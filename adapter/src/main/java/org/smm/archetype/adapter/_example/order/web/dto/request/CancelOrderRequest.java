package org.smm.archetype.adapter._example.order.web.dto.request;

import lombok.Getter;
import lombok.Setter;

/**
 * 取消订单请求
 * @author Leonardo
 * @since 2026/1/11
 */
@Getter
@Setter
public class CancelOrderRequest {

    /**
     * 取消原因
     */
    private String reason;

}

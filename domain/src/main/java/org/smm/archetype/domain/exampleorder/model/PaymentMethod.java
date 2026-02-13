package org.smm.archetype.domain.exampleorder.model;

import lombok.RequiredArgsConstructor;

/**
 * 支付方式枚举


 */
@RequiredArgsConstructor
public enum PaymentMethod {
    /**
     * 支付宝
     */
    ALIPAY("支付宝"),

    /**
     * 微信支付
     */
    WECHAT("微信"),

    /**
     * Stripe支付
     */
    STRIPE("Stripe");

    private final String description;

    /**
     * 获取支付方式描述
     */
    public String getDescription() {
        return description;
    }
}

package org.smm.archetype.domain.exampleorder.model;

import lombok.RequiredArgsConstructor;

/**
 * 支付状态枚举，支持支付和退款状态流转。
 */
@RequiredArgsConstructor
public enum PaymentStatus {
    /**
     * 待支付 - 支付记录创建，等待支付
     */
    PENDING("待支付"),

    /**
     * 成功 - 支付成功
     */
    SUCCESS("成功"),

    /**
     * 失败 - 支付失败
     */
    FAILED("失败"),

    /**
     * 已退款 - 支付已退款
     */
    REFUNDED("已退款");

    private final String description;

    /**
     * 获取状态描述
     */
    public String getDescription() {
        return description;
    }

    /**
     * 检查是否为终态
     */
    public boolean isTerminalState() {
        return this == SUCCESS || this == FAILED || this == REFUNDED;
    }

    /**
     * 检查是否为成功状态
     */
    public boolean isSuccess() {
        return this == SUCCESS;
    }

    /**
     * 检查是否可以退款
     */
    public boolean canRefund() {
        return this == SUCCESS;
    }
}

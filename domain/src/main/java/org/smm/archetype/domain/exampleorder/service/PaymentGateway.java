package org.smm.archetype.domain.exampleorder.service;

import org.smm.archetype.domain.exampleorder.model.PaymentMethod;

import java.math.BigDecimal;

/**
 * 支付网关端口接口，定义支付操作的抽象。
 */
public interface PaymentGateway {

    /**
     * 支付订单
     * @param orderId        订单ID
     * @param orderNo        订单编号
     * @param amount         支付金额
     * @param currency       货币类型
     * @param paymentMethod  支付方式
     * @param paymentAccount 支付账户（如支付宝账号）
     * @return 支付交易ID
     * @throws PaymentException 支付失败
     */
    String pay(
            Long orderId,
            String orderNo,
            BigDecimal amount,
            String currency,
            PaymentMethod paymentMethod,
            String paymentAccount
    ) throws PaymentException;

    /**
     * 查询支付状态
     * @param transactionId 第三方交易ID
     * @return 支付状态
     */
    PaymentStatus queryPaymentStatus(String transactionId);

    /**
     * 退款
     * @param transactionId 第三方交易ID
     * @param refundAmount  退款金额
     * @param refundReason  退款原因
     * @return 退款交易ID
     * @throws PaymentException 退款失败
     */
    String refund(
            String transactionId,
            BigDecimal refundAmount,
            String refundReason
    ) throws PaymentException;

    /**
     * 支付状态
     */
    enum PaymentStatus {
        /**
         * 待支付
         */
        PENDING,
        /**
         * 支付成功
         */
        SUCCESS,
        /**
         * 支付失败
         */
        FAILED,
        /**
         * 已退款
         */
        REFUNDED
    }

    /**
     * 支付异常
     */
    class PaymentException extends Exception {

        public PaymentException(String message) {
            super(message);
        }

        public PaymentException(String message, Throwable cause) {
            super(message, cause);
        }

    }

}

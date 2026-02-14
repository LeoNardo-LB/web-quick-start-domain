package org.smm.archetype.infrastructure.exampleorder.adapter;

import lombok.extern.slf4j.Slf4j;
import org.smm.archetype.domain.exampleorder.model.PaymentMethod;
import org.smm.archetype.domain.exampleorder.service.PaymentGateway;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Stripe支付网关适配器
 *
职责：
 * <ul>
 *   <li>实现PaymentGateway端口接口</li>
 *   <li>调用Stripe API进行支付</li>
 *   <li>处理Stripe响应和异常</li>
 * </ul>
 *
说明：
 * <ul>
 *   <li>这是一个Mock实现，仅用于演示</li>
 *   <li>实际生产环境需要集成Stripe SDK</li>
 *   <li>通过配置文件控制是否启用（payment.stripe.enabled）</li>
 *   <li>通过OrderConfigure配置类注册为Bean（遵循规范：不使用@Component注解）</li>
 * </ul>


 */
@Slf4j
public class StripePaymentAdapter implements PaymentGateway {

    @Override
    public String pay(
            Long orderId,
            String orderNo,
            BigDecimal amount,
            String currency,
            PaymentMethod paymentMethod,
            String paymentAccount
    ) throws PaymentException {
        log.info("Stripe支付开始: orderId={}, orderNo={}, amount={}, currency={}",
                orderId, orderNo, amount, currency);

        try {
            // TODO: 实际生产环境需要调用Stripe API
            // 这里仅模拟支付成功
            String transactionId = "stripe_" + UUID.randomUUID().toString().replace("-", "");

            log.info("Stripe支付成功: orderId={}, transactionId={}", orderId, transactionId);
            return transactionId;

        } catch (Exception e) {
            log.error("Stripe支付失败: orderId={}", orderId, e);
            throw new PaymentException("Stripe支付失败: " + e.getMessage(), e);
        }
    }

    @Override
    public PaymentStatus queryPaymentStatus(String transactionId) {
        log.info("查询Stripe支付状态: transactionId={}", transactionId);

        // TODO: 实际生产环境需要调用Stripe API查询
        // 这里仅模拟返回成功状态
        return PaymentStatus.SUCCESS;
    }

    @Override
    public String refund(
            String transactionId,
            BigDecimal refundAmount,
            String refundReason
    ) throws PaymentException {
        log.info("Stripe退款开始: transactionId={}, refundAmount={}, reason={}",
                transactionId, refundAmount, refundReason);

        try {
            // TODO: 实际生产环境需要调用Stripe API
            String refundTransactionId = "stripe_refund_" + UUID.randomUUID().toString().replace("-", "");

            log.info("Stripe退款成功: originalTransactionId={}, refundTransactionId={}",
                    transactionId, refundTransactionId);
            return refundTransactionId;

        } catch (Exception e) {
            log.error("Stripe退款失败: transactionId={}", transactionId, e);
            throw new PaymentException("Stripe退款失败: " + e.getMessage(), e);
        }
    }

}

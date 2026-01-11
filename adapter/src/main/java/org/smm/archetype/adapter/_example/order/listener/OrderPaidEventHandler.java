package org.smm.archetype.adapter._example.order.listener;

import lombok.extern.slf4j.Slf4j;
import org.smm.archetype.app._shared.event.EventHandler;
import org.smm.archetype.domain._example.order.model.event.OrderPaidEvent;
import org.smm.archetype.domain._shared.base.DomainEvent;

/**
 * 订单支付事件处理器
 *
 * <p>职责：
 * <ul>
 *   <li>监听订单支付事件</li>
 *   <li>发送支付通知（Mock调用）</li>
 * </ul>
 * @author Leonardo
 * @since 2026/1/11
 */
@Slf4j
public class OrderPaidEventHandler implements EventHandler<OrderPaidEvent> {

    @Override
    public void handle(OrderPaidEvent event) {
        log.info("处理订单支付事件: orderId={}, orderNo={}",
                event.getAggregateId(), event.getOrderNo());

        try {
            // Mock调用：发送支付通知
            log.info("【Mock】发送支付通知: orderId={}, orderNo={}, paymentMethod={}, amount={}",
                    event.getAggregateId(),
                    event.getOrderNo(),
                    event.getPaymentMethod(),
                    event.getPaymentAmount());

            // 在实际项目中，这里会调用真实的通知服务
            // 例如：notificationService.sendPaymentNotification(event);

            log.info("订单支付事件处理完成: orderId={}", event.getAggregateId());

        } catch (Exception e) {
            log.error("处理订单支付事件失败: orderId={}, error={}",
                    event.getAggregateId(), e.getMessage(), e);
            // 不抛出异常，避免影响其他事件处理器
        }
    }

    @Override
    public boolean canHandle(DomainEvent event) {
        return event instanceof OrderPaidEvent;
    }

}

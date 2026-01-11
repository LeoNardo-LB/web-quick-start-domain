package org.smm.archetype.adapter._example.order.listener;

import lombok.extern.slf4j.Slf4j;
import org.smm.archetype.app._shared.event.EventHandler;
import org.smm.archetype.domain._example.order.model.event.OrderCancelledEvent;
import org.smm.archetype.domain._shared.base.DomainEvent;

/**
 * 订单取消事件处理器
 *
 * <p>职责：
 * <ul>
 *   <li>监听订单取消事件</li>
 *   <li>释放库存（已在应用层处理）</li>
 *   <li>发送取消通知（Mock调用）</li>
 * </ul>
 * @author Leonardo
 * @since 2026/1/11
 */
@Slf4j
public class OrderCancelledEventHandler implements EventHandler<OrderCancelledEvent> {

    @Override
    public void handle(OrderCancelledEvent event) {
        log.info("处理订单取消事件: orderId={}, orderNo={}, reason={}",
                event.getAggregateId(), event.getOrderNo(), event.getReason());

        try {
            // 真实调用：释放库存（已在应用层的cancelOrder中完成）
            log.info("释放库存: orderId={}, orderNo={}",
                    event.getAggregateId(), event.getOrderNo());

            // Mock调用：发送取消通知
            log.info("【Mock】发送取消通知: orderId={}, orderNo={}, reason={}",
                    event.getAggregateId(),
                    event.getOrderNo(),
                    event.getReason());

            // 在实际项目中，这里会调用真实的通知服务
            // 例如：notificationService.sendCancellationNotification(event);

            log.info("订单取消事件处理完成: orderId={}", event.getAggregateId());

        } catch (Exception e) {
            log.error("处理订单取消事件失败: orderId={}, error={}",
                    event.getAggregateId(), e.getMessage(), e);
            // 不抛出异常，避免影响其他事件处理器
        }
    }

    @Override
    public boolean canHandle(DomainEvent event) {
        return event instanceof OrderCancelledEvent;
    }

}

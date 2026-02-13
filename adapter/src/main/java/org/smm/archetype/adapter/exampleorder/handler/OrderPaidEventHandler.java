package org.smm.archetype.adapter.exampleorder.handler;

import lombok.extern.slf4j.Slf4j;
import org.smm.archetype.adapter.event.EventHandler;
import org.smm.archetype.domain.shared.event.Event;
import org.smm.archetype.domain.shared.event.Type;
import org.smm.archetype.domain.exampleorder.model.event.OrderPaidEventDTO;

/**
 * 订单支付事件处理器，监听订单支付事件并发送通知。
 */
@Slf4j
public class OrderPaidEventHandler implements EventHandler<OrderPaidEventDTO> {

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Event<OrderPaidEventDTO> canHandle(Event<Object> event) {
        // 通过 Type 枚举区分事件类型，通过 payload instanceof 进行类型检查
        if (event.getType() == Type.ORDER_PAID
                    && event.getPayload() instanceof OrderPaidEventDTO) {
            return (Event) event;
        }
        return null;
    }

    @Override
    public void handle(OrderPaidEventDTO payload) {
        log.info("处理订单支付事件: orderId={}, orderNo={}",
                payload.getAggregateId(), payload.getOrderNo());

        try {
            // Mock调用：发送支付通知
            log.info("【Mock】发送支付通知: orderId={}, orderNo={}, paymentMethod={}, amount={}",
                    payload.getAggregateId(),
                    payload.getOrderNo(),
                    payload.getPaymentMethod(),
                    payload.getPaymentAmount());

            // 在实际项目中，这里会调用真实的通知服务
            // 例如：notificationService.sendPaymentNotification(event);

            log.info("订单支付事件处理完成: orderId={}", payload.getAggregateId());

        } catch (Exception e) {
            log.error("处理订单支付事件失败: orderId={}, error={}",
                    payload.getAggregateId(), e.getMessage(), e);
            throw e; // 抛出异常让 EventDispatcher 处理重试
        }
    }

}

package org.smm.archetype.adapter.exampleorder.handler;

import lombok.extern.slf4j.Slf4j;
import org.smm.archetype.adapter.event.EventHandler;
import org.smm.archetype.domain.shared.event.Event;
import org.smm.archetype.domain.shared.event.Type;
import org.smm.archetype.domain.exampleorder.model.event.OrderCancelledEventDTO;

/**
 * 订单取消事件处理器，监听订单取消事件并释放库存。
 */
@Slf4j
public class OrderCancelledEventHandler implements EventHandler<OrderCancelledEventDTO> {

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Event<OrderCancelledEventDTO> canHandle(Event<Object> event) {
        // 通过 Type 枚举区分事件类型，通过 payload instanceof 进行类型检查
        if (event.getType() == Type.ORDER_CANCELLED && event.getPayload() instanceof OrderCancelledEventDTO) {
            return (Event) event;
        }
        return null;
    }

    @Override
    public void handle(OrderCancelledEventDTO payload) {
        log.info("处理订单取消事件: orderId={}, orderNo={}, reason={}",
                payload.getAggregateId(), payload.getOrderNo(), payload.getReason());

        try {
            // 真实调用：释放库存（已在应用层的cancelOrder中完成）
            log.info("释放库存: orderId={}, orderNo={}",
                    payload.getAggregateId(), payload.getOrderNo());

            // Mock调用：发送取消通知
            log.info("【Mock】发送取消通知: orderId={}, orderNo={}, reason={}",
                    payload.getAggregateId(),
                    payload.getOrderNo(),
                    payload.getReason());

            // 在实际项目中，这里会调用真实的通知服务
            // 例如：notificationService.sendCancellationNotification(event);

            log.info("订单取消事件处理完成: orderId={}", payload.getAggregateId());

        } catch (Exception e) {
            log.error("处理订单取消事件失败: orderId={}, error={}",
                    payload.getAggregateId(), e.getMessage(), e);
            throw e; // 抛出异常让 EventDispatcher 处理重试
        }
    }

}

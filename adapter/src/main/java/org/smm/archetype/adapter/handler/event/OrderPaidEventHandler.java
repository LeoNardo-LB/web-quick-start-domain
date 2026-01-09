package org.smm.archetype.adapter.handler.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smm.archetype.domain._shared.base.DomainEvent;
import org.smm.archetype.domain._shared.event.EventType;
import org.smm.archetype.domain.example.order.event.OrderPaidEvent;
import org.springframework.stereotype.Component;

/**
 * 订单支付事件处理器
 *
 * <p>示例：处理订单支付事件
 * @author Leonardo
 * @since 2025/12/30
 */
@Component
public class OrderPaidEventHandler implements EventHandler<OrderPaidEvent> {

    private static final Logger log = LoggerFactory.getLogger(OrderPaidEventHandler.class);

    @Override
    public void handle(OrderPaidEvent event) {
        log.info("Processing OrderPaidEvent: orderId={}, paymentMethod={}, amount={}",
                event.getOrderId(),
                event.getPaymentMethod(),
                event.getTotalAmount());

        // 示例业务逻辑：
        // 1. 通知财务系统
        // 2. 触发库存扣减
        // 3. 发送支付成功通知
        // 4. 更新客户积分

        log.info("OrderPaidEvent processed successfully");
    }

    @Override
    public boolean canHandle(DomainEvent event) {
        return event instanceof OrderPaidEvent;
    }

    @Override
    public EventType getEventType() {
        return EventType.ORDER_PAID;
    }

}

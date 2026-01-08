package org.smm.archetype.infrastructure.example.order.handler;

import lombok.extern.slf4j.Slf4j;
import org.smm.archetype.domain._shared.base.DomainEvent;
import org.smm.archetype.domain._shared.event.EventHandler;
import org.smm.archetype.domain.example.order.event.OrderCancelledEvent;
import org.springframework.stereotype.Component;

/**
 * 订单取消事件处理器
 *
 * <p>示例：处理订单取消事件
 * @author Leonardo
 * @since 2025/12/30
 */
@Slf4j
@Component
public class OrderCancelledEventHandler implements EventHandler<OrderCancelledEvent> {

    @Override
    public void handle(OrderCancelledEvent event) {
        log.info("Processing OrderCancelledEvent: orderId={}, reason={}",
                event.getOrderId(),
                event.getReason());

        // 示例业务逻辑：
        // 1. 恢复库存
        // 2. 处理退款
        // 3. 发送取消通知
        // 4. 更新统计数据

        log.info("OrderCancelledEvent processed successfully");
    }

    @Override
    public boolean canHandle(DomainEvent event) {
        return event instanceof OrderCancelledEvent;
    }

}

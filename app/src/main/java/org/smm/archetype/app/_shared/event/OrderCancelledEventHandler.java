package org.smm.archetype.app._shared.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smm.archetype.domain._example.order.event.OrderCancelledEvent;
import org.smm.archetype.domain._shared.base.DomainEvent;

/**
 * 订单取消事件处理器
 *
 * <p>示例：处理订单取消事件
 * @author Leonardo
 * @since 2025/12/30
 */
public class OrderCancelledEventHandler implements EventHandler<OrderCancelledEvent> {

    private static final Logger log = LoggerFactory.getLogger(OrderCancelledEventHandler.class);

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

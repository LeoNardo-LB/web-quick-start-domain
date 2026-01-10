package org.smm.archetype.app._shared.event;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smm.archetype.domain._example.order.event.OrderCreatedEvent;
import org.smm.archetype.domain._shared.base.DomainEvent;

/**
 * 订单创建事件处理器
 *
 * <p>示例：处理订单创建事件
 * @author Leonardo
 * @since 2025/12/30
 */
@RequiredArgsConstructor
public class OrderCreatedEventHandler implements EventHandler<OrderCreatedEvent> {

    private static final Logger log = LoggerFactory.getLogger(OrderCreatedEventHandler.class);

    @Override
    public void handle(OrderCreatedEvent event) {
        log.info("Processing OrderCreatedEvent: orderId={}, customerId={}, totalAmount={}",
                event.getOrderId(),
                event.getCustomerId(),
                event.getTotalAmount());

        // 示例业务逻辑：
        // 1. 发送欢迎邮件
        // 2. 发送短信通知
        // 3. 更新统计信息
        // 4. 触发其他业务流程

        // 这里只是示例，实际业务根据需求实现
        try {
            // 模拟业务处理
            Thread.sleep(100);
            log.info("OrderCreatedEvent processed successfully");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Failed to process OrderCreatedEvent", e);
            throw new RuntimeException("Failed to process OrderCreatedEvent", e);
        }
    }

    @Override
    public boolean canHandle(DomainEvent event) {
        return event instanceof OrderCreatedEvent;
    }

}

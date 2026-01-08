package org.smm.archetype.infrastructure.example.order.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smm.archetype.domain._shared.base.DomainEvent;
import org.smm.archetype.domain._shared.event.EventHandler;
import org.smm.archetype.domain.example.order.event.OrderCreatedEvent;
import org.springframework.stereotype.Component;

/**
 * 订单创建事件处理器
 *
 * <p>示例：处理订单创建事件
 * @author Leonardo
 * @since 2025/12/30
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCreatedEventHandler implements EventHandler<OrderCreatedEvent> {

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

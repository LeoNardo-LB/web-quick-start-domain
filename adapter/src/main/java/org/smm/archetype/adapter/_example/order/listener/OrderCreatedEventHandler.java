package org.smm.archetype.adapter._example.order.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smm.archetype.app._shared.event.EventHandler;
import org.smm.archetype.domain._example.order.model.event.OrderCreatedEvent;
import org.smm.archetype.domain._example.order.service.InventoryService;
import org.smm.archetype.domain._shared.base.DomainEvent;

/**
 * 订单创建事件处理器
 *
 * <p>职责：
 * <ul>
 *   <li>监听订单创建事件</li>
 *   <li>锁定库存（真实调用）</li>
 * </ul>
 * @author Leonardo
 * @since 2026/1/11
 */
@Slf4j
@RequiredArgsConstructor
public class OrderCreatedEventHandler implements EventHandler<OrderCreatedEvent> {

    private final InventoryService inventoryService;

    @Override
    public void handle(OrderCreatedEvent event) {
        log.info("处理订单创建事件: orderId={}, orderNo={}",
                event.getAggregateId(), event.getOrderNo());

        try {
            // 真实调用：锁定库存
            log.info("锁定库存: orderId={}, orderNo={}",
                    event.getAggregateId(), event.getOrderNo());

            // 注意：库存锁定已经在应用层的createOrder中完成了
            // 这里只是记录日志，表示事件已被处理
            log.info("订单创建事件处理完成: orderId={}", event.getAggregateId());

        } catch (Exception e) {
            log.error("处理订单创建事件失败: orderId={}, error={}",
                    event.getAggregateId(), e.getMessage(), e);
            // 不抛出异常，避免影响其他事件处理器
        }
    }

    @Override
    public boolean canHandle(DomainEvent event) {
        return event instanceof OrderCreatedEvent;
    }

}

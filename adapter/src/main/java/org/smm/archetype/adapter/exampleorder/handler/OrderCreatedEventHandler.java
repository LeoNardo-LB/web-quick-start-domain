package org.smm.archetype.adapter.exampleorder.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smm.archetype.adapter.event.EventHandler;
import org.smm.archetype.domain.shared.event.Event;
import org.smm.archetype.domain.shared.event.Type;
import org.smm.archetype.domain.exampleorder.model.event.OrderCreatedEventDTO;
import org.smm.archetype.domain.exampleorder.service.InventoryService;

/**
 * 订单创建事件处理器，监听订单创建事件并锁定库存。
 */
@Slf4j
@RequiredArgsConstructor
public class OrderCreatedEventHandler implements EventHandler<OrderCreatedEventDTO> {

    private final InventoryService inventoryService;

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Event<OrderCreatedEventDTO> canHandle(Event<Object> event) {
        // 通过 Type 枚举区分事件类型，通过 payload instanceof 进行类型检查
        if (event.getType() == Type.ORDER_CREATED
                    && event.getPayload() instanceof OrderCreatedEventDTO) {
            return (Event) event;
        }
        return null;
    }

    @Override
    public void handle(OrderCreatedEventDTO payload) {
        log.info("处理订单创建事件: orderId={}, orderNo={}",
                payload.getAggregateId(), payload.getOrderNo());

        try {
            // 真实调用：锁定库存
            log.info("锁定库存: orderId={}, orderNo={}",
                    payload.getAggregateId(), payload.getOrderNo());

            // 注意：库存锁定已经在应用层的createOrder中完成了
            // 这里只是记录日志，表示事件已被处理
            log.info("订单创建事件处理完成: orderId={}", payload.getAggregateId());

        } catch (Exception e) {
            log.error("处理订单创建事件失败: orderId={}, error={}",
                    payload.getAggregateId(), e.getMessage(), e);
            throw e; // 抛出异常让 EventDispatcher 处理重试
        }
    }

}

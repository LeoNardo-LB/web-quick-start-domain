package org.smm.archetype.adapter._example.order.config;

import org.smm.archetype.adapter._example.order.listener.OrderCancelledEventHandler;
import org.smm.archetype.adapter._example.order.listener.OrderCreatedEventHandler;
import org.smm.archetype.adapter._example.order.listener.OrderPaidEventHandler;
import org.smm.archetype.app._shared.event.EventHandler;
import org.smm.archetype.domain._example.order.service.InventoryService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 订单接口层配置
 * @author Leonardo
 * @since 2026/1/11
 */
@Configuration
public class OrderAdapterConfigure {

    /**
     * 订单创建事件处理器Bean
     */
    @Bean
    public EventHandler<?> orderCreatedEventHandler(InventoryService inventoryService) {
        return new OrderCreatedEventHandler(inventoryService);
    }

    /**
     * 订单支付事件处理器Bean
     */
    @Bean
    public EventHandler<?> orderPaidEventHandler() {
        return new OrderPaidEventHandler();
    }

    /**
     * 订单取消事件处理器Bean
     */
    @Bean
    public EventHandler<?> orderCancelledEventHandler() {
        return new OrderCancelledEventHandler();
    }

}

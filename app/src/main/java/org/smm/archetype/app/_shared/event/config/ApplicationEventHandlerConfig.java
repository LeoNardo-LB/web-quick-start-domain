package org.smm.archetype.app._shared.event.config;

import org.smm.archetype.app._shared.event.OrderCancelledEventHandler;
import org.smm.archetype.app._shared.event.OrderCreatedEventHandler;
import org.smm.archetype.app._shared.event.OrderPaidEventHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Application层事件处理器配置
 *
 * <p>负责创建领域事件处理器的Bean。
 * @author Leonardo
 * @since 2026-01-10
 */
@Configuration
public class ApplicationEventHandlerConfig {

    /**
     * 订单已创建事件处理器
     * @return 订单已创建事件处理器
     */
    @Bean
    public OrderCreatedEventHandler orderCreatedEventHandler() {
        return new OrderCreatedEventHandler();
    }

    /**
     * 订单已支付事件处理器
     * @return 订单已支付事件处理器
     */
    @Bean
    public OrderPaidEventHandler orderPaidEventHandler() {
        return new OrderPaidEventHandler();
    }

    /**
     * 订单已取消事件处理器
     * @return 订单已取消事件处理器
     */
    @Bean
    public OrderCancelledEventHandler orderCancelledEventHandler() {
        return new OrderCancelledEventHandler();
    }

}

package org.smm.archetype.app._example.order.config;

import org.smm.archetype.app._example.order.OrderApplicationService;
import org.smm.archetype.domain._example.order.repository.OrderAggrRepository;
import org.smm.archetype.domain._example.order.service.OrderDomainService;
import org.smm.archetype.domain._shared.event.EventPublisher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;

/**
 * 订单应用层配置
 * @author Leonardo
 * @since 2026/1/11
 */
@Configuration
public class OrderAppConfigure {

    /**
     * 订单应用服务Bean
     */
    @org.springframework.context.annotation.Bean
    public OrderApplicationService orderApplicationService(
            OrderAggrRepository orderRepository,
            OrderDomainService orderDomainService,
            @Qualifier("springEventPublisher") EventPublisher eventPublisher) {
        return new OrderApplicationService(orderRepository, orderDomainService, eventPublisher);
    }

}

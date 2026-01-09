package org.smm.archetype.app._example.order.config;

import org.smm.archetype.app._example.order.service.OrderApplicationService;
import org.smm.archetype.domain._example.order.repository.OrderRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Application层订单示例模块配置
 *
 * <p>负责创建订单应用服务的Bean。
 * @author Leonardo
 * @since 2026-01-10
 */
@Configuration
public class ApplicationExampleOrderConfig {

    /**
     * 订单应用服务
     *
     * <p>实现订单相关的用例编排，如创建订单、支付订单、取消订单等。
     * @param orderRepository 订单仓储
     * @return 订单应用服务
     */
    @Bean
    public OrderApplicationService orderApplicationService(final OrderRepository orderRepository) {
        return new OrderApplicationService(orderRepository);
    }

}

package org.smm.archetype.config;

import org.smm.archetype.domain._example.order.repository.OrderRepository;
import org.smm.archetype.infrastructure._example.order.repository.converter.OrderConverter;
import org.smm.archetype.infrastructure._example.order.repository.converter.OrderItemConverter;
import org.smm.archetype.infrastructure._example.order.repository.impl.OrderRepositoryImpl;
import org.smm.archetype.infrastructure._example.order.repository.mapper.OrderItemMapper;
import org.smm.archetype.infrastructure._example.order.repository.mapper.OrderMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 订单示例模块配置
 *
 * <p>负责创建订单领域仓储实现的Bean。
 * @author Leonardo
 * @since 2026-01-10
 */
@Configuration
public class ExampleOrderConfigure {

    /**
     * 订单仓储实现
     * @param orderMapper        订单Mapper
     * @param orderItemMapper    订单项Mapper
     * @param orderConverter     订单转换器
     * @param orderItemConverter 订单项转换器
     * @return 订单仓储实现
     */
    @Bean
    public OrderRepository orderRepository(
            final OrderMapper orderMapper,
            final OrderItemMapper orderItemMapper,
            final OrderConverter orderConverter,
            final OrderItemConverter orderItemConverter) {
        return new OrderRepositoryImpl(orderMapper, orderItemMapper, orderConverter, orderItemConverter);
    }

}

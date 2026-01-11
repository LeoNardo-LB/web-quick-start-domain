package org.smm.archetype.infrastructure._example.order.config;

import org.smm.archetype.domain._example.order.repository.OrderAggrRepository;
import org.smm.archetype.domain._example.order.service.InventoryService;
import org.smm.archetype.domain._example.order.service.OrderDomainService;
import org.smm.archetype.domain._example.order.service.PaymentGateway;
import org.smm.archetype.infrastructure._example.order.adapter.MockInventoryServiceAdapter;
import org.smm.archetype.infrastructure._example.order.persistence.OrderAggrRepositoryImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Configuration;

/**
 * 订单基础设施层配置
 *
 * <p>职责：
 * <ul>
 *   <li>注册订单仓储Bean</li>
 *   <li>注册外部服务适配器Bean（如果需要）</li>
 *   <li>配置基础设施层依赖</li>
 * </ul>
 *
 * <p>设计原则：
 * <ul>
 *   <li>使用@Configuration + @Bean模式</li>
 *   <li>使用@ConditionalOnMissingBean支持自定义实现</li>
 *   <li>配置类放在Bean所在的模块</li>
 * </ul>
 * @author Leonardo
 * @since 2026/1/11
 */
@Configuration
public class OrderInfraConfigure {

    /**
     * 注册订单仓储Bean
     * <p>如果用户未提供自定义实现，使用默认的OrderAggrRepositoryImpl
     * @return OrderAggrRepository
     */
    @org.springframework.context.annotation.Bean
    @ConditionalOnMissingBean(OrderAggrRepository.class)
    public OrderAggrRepository orderAggrRepository(OrderAggrRepositoryImpl impl) {
        return impl;
    }

    /**
     * 注册订单领域服务Bean
     * <p>领域服务没有实现类，直接使用new创建
     * @return OrderDomainService
     */
    @org.springframework.context.annotation.Bean
    @ConditionalOnMissingBean(OrderDomainService.class)
    public OrderDomainService orderDomainService(InventoryService inventoryService) {
        return new OrderDomainService(inventoryService);
    }

    /**
     * 注册库存服务Bean
     * <p>创建并返回MockInventoryServiceAdapter实例
     * @return InventoryService
     */
    @org.springframework.context.annotation.Bean
    public InventoryService inventoryService() {
        return new MockInventoryServiceAdapter();
    }

    /**
     * 注册支付网关Bean
     * <p>仅当存在PaymentGateway实现时才注册
     * <p>支持多种支付方式：Stripe、支付宝等
     * @return PaymentGateway
     */
    @org.springframework.context.annotation.Bean
    @ConditionalOnMissingBean(PaymentGateway.class)
    public PaymentGateway paymentGateway() {
        // 默认情况下不提供支付网关实现
        // 用户可以通过配置启用特定的支付适配器（如StripePaymentAdapter）
        return null;
    }

}

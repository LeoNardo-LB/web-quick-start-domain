package org.smm.archetype.config.exampleorder;

import org.smm.archetype.adapter.event.EventHandler;
import org.smm.archetype.adapter.exampleorder.handler.OrderCancelledEventHandler;
import org.smm.archetype.adapter.exampleorder.handler.OrderCreatedEventHandler;
import org.smm.archetype.adapter.exampleorder.handler.OrderPaidEventHandler;
import org.smm.archetype.app.exampleorder.OrderAppService;
import org.smm.archetype.app.exampleorder.converter.OrderDtoConverter;
import org.smm.archetype.domain.exampleorder.repository.OrderAggrRepository;
import org.smm.archetype.domain.exampleorder.service.InventoryService;
import org.smm.archetype.domain.exampleorder.service.OrderDomainService;
import org.smm.archetype.domain.exampleorder.service.PaymentGateway;
import org.smm.archetype.domain.shared.event.DomainEventPublisher;
import org.smm.archetype.infrastructure.exampleorder.adapter.MockInventoryServiceAdapter;
import org.smm.archetype.infrastructure.exampleorder.adapter.StripePaymentAdapter;
import org.smm.archetype.infrastructure.exampleorder.persistence.OrderRepositoryImpl;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 订单聚合根配置类，注册订单相关的所有Bean。
 */
@Configuration
public class OrderConfigure {

    // ==================== 应用服务 ====================

    /**
     * 订单应用服务Bean
     * 职责：用例编排、事务管理、DTO转换
     * @param orderRepository      订单仓储
     * @param orderDomainService   订单领域服务
     * @param domainEventPublisher 事件发布器
     * @param dtoConverter         DTO转换器（MapStruct）
     * @return OrderAppService
     */
    @Bean
    public OrderAppService orderAppService(
            OrderAggrRepository orderRepository,
            OrderDomainService orderDomainService,
            @Qualifier("springEventPublisher") DomainEventPublisher domainEventPublisher,
            OrderDtoConverter dtoConverter) {
        return new OrderAppService(orderRepository, orderDomainService, domainEventPublisher, dtoConverter);
    }

    // ==================== 仓储实现 ====================

    /**
     * 订单聚合根仓储Bean
     * 如果用户未提供自定义实现，使用默认的OrderRepositoryImpl
     * @return OrderAggrRepository
     */
    @Bean
    public OrderAggrRepository orderAggrRepository() {
        OrderRepositoryImpl repository = new OrderRepositoryImpl();
        // 手动调用初始化方法，因为 @Bean 方法直接 new 不会触发 @PostConstruct
        repository.initTestData();
        return repository;
    }

    // ==================== 领域服务 ====================

    /**
     * 订单领域服务Bean
     * 职责：封装跨聚合根的业务规则（库存验证、金额计算等）
     * @param inventoryService 库存服务
     * @return OrderDomainService
     */
    @Bean
    @ConditionalOnMissingBean(OrderDomainService.class)
    public OrderDomainService orderDomainService(InventoryService inventoryService) {
        return new OrderDomainService(inventoryService);
    }

    // ==================== 中间件服务 ====================

    /**
     * 库存服务Bean
     * 职责：验证库存、锁定库存、释放库存
     * 默认实现：MockInventoryServiceAdapter（模拟实现）
     * @return InventoryService
     */
    @Bean
    public InventoryService inventoryService() {
        return new MockInventoryServiceAdapter();
    }

    /**
     * 支付网关Bean（Stripe实现）
     * 职责：处理支付请求、查询支付状态
     * 通过配置文件控制是否启用（payment.stripe.enabled）
     * @return Stripe支付网关实现
     */
    @Bean
    @ConditionalOnProperty(prefix = "payment.stripe", name = "enabled", havingValue = "true")
    public PaymentGateway stripePaymentGateway() {
        return new StripePaymentAdapter();
    }

    /**
     * 支付网关Bean（默认实现）
     * 职责：处理支付请求、查询支付状态
     * 支持多种支付方式：Stripe、支付宝、微信支付等
     * 默认情况下返回null，用户可以通过配置启用特定的支付适配器
     * @return PaymentGateway（可能为null）
     */
    @Bean
    @ConditionalOnMissingBean(PaymentGateway.class)
    public PaymentGateway paymentGateway() {
        // 默认情况下不提供支付网关实现
        // 用户可以通过配置启用特定的支付适配器（如StripePaymentAdapter）
        return null;
    }

    // ==================== 事件处理器 ====================

    /**
     * 订单创建事件处理器Bean
     * 职责：处理订单创建事件（如发送通知、记录日志）
     * @param inventoryService 库存服务
     * @return EventDispatcher
     */
    @Bean
    public EventHandler<?> orderCreatedEventHandler(InventoryService inventoryService) {
        return new OrderCreatedEventHandler(inventoryService);
    }

    /**
     * 订单支付事件处理器Bean
     * 职责：处理订单支付事件（如更新库存、发货）
     * @return EventDispatcher
     */
    @Bean
    public EventHandler<?> orderPaidEventHandler() {
        return new OrderPaidEventHandler();
    }

    /**
     * 订单取消事件处理器Bean
     * 职责：处理订单取消事件（如释放库存、退款）
     * @return EventDispatcher
     */
    @Bean
    public EventHandler<?> orderCancelledEventHandler() {
        return new OrderCancelledEventHandler();
    }

}

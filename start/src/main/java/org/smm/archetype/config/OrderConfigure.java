package org.smm.archetype.config;

import org.smm.archetype.adapter._example.order.listener.OrderCancelledEventHandler;
import org.smm.archetype.adapter._example.order.listener.OrderCreatedEventHandler;
import org.smm.archetype.adapter._example.order.listener.OrderPaidEventHandler;
import org.smm.archetype.app._example.order.OrderAppService;
import org.smm.archetype.app._shared.event.EventHandler;
import org.smm.archetype.domain._example.order.repository.OrderAggrRepository;
import org.smm.archetype.domain._example.order.service.InventoryService;
import org.smm.archetype.domain._example.order.service.OrderDomainService;
import org.smm.archetype.domain._example.order.service.PaymentGateway;
import org.smm.archetype.domain._shared.event.EventPublisher;
import org.smm.archetype.infrastructure._example.order.adapter.MockInventoryServiceAdapter;
import org.smm.archetype.infrastructure._example.order.persistence.OrderAggrRepositoryImpl;
import org.smm.archetype.infrastructure._example.order.persistence.converter.OrderAggrConverter;
import org.smm.archetype.infrastructure._example.order.persistence.converter.OrderItemConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 订单聚合根配置
 *
 * <p>聚合根：OrderAggr
 * <p>职责：
 * <ul>
 *   <li>注册订单相关的所有Bean：应用服务、仓储实现、领域服务、事件处理器、中间件服务</li>
 *   <li>集中管理订单聚合根的依赖注入</li>
 *   <li>提供条件装配支持（@ConditionalOnMissingBean）</li>
 * </ul>
 *
 * <p>设计原则：
 * <ul>
 *   <li>按聚合根命名配置类（OrderAggr → OrderConfigure）</li>
 *   <li>使用 @Configuration + @Bean 模式</li>
 *   <li>配置类位于 start/src/main/java/org/smm/archetype/config/</li>
 *   <li>使用构造器注入（而非 @Autowired 字段注入）</li>
 * </ul>
 * @author Leonardo
 * @since 2026/1/11
 */
@Configuration
public class OrderConfigure {

    // ==================== 应用服务 ====================

    /**
     * 订单应用服务Bean
     * <p>职责：用例编排、事务管理、DTO转换
     * @param orderRepository    订单仓储
     * @param orderDomainService 订单领域服务
     * @param eventPublisher     事件发布器
     * @return OrderAppService
     */
    @Bean
    public OrderAppService orderAppService(
            OrderAggrRepository orderRepository,
            OrderDomainService orderDomainService,
            @Qualifier("springEventPublisher") EventPublisher eventPublisher) {
        return new OrderAppService(orderRepository, orderDomainService, eventPublisher);
    }

    // ==================== 仓储实现 ====================

    /**
     * 订单聚合根仓储Bean
     * <p>如果用户未提供自定义实现，使用默认的OrderAggrRepositoryImpl
     * @param impl OrderAggrRepositoryImpl实例（由Spring自动创建）
     * @return OrderAggrRepository
     */
    @Bean
    @ConditionalOnMissingBean(OrderAggrRepository.class)
    public OrderAggrRepository orderAggrRepository(OrderAggrRepositoryImpl impl) {
        return impl;
    }

    // ==================== 领域服务 ====================

    /**
     * 订单领域服务Bean
     * <p>职责：封装跨聚合根的业务规则（库存验证、金额计算等）
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
     * <p>职责：验证库存、锁定库存、释放库存
     * <p>默认实现：MockInventoryServiceAdapter（模拟实现）
     * @return InventoryService
     */
    @Bean
    public InventoryService inventoryService() {
        return new MockInventoryServiceAdapter();
    }

    /**
     * 支付网关Bean
     * <p>职责：处理支付请求、查询支付状态
     * <p>支持多种支付方式：Stripe、支付宝、微信支付等
     * <p>默认情况下返回null，用户可以通过配置启用特定的支付适配器
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
     * <p>职责：处理订单创建事件（如发送通知、记录日志）
     * @param inventoryService 库存服务
     * @return EventHandler
     */
    @Bean
    public EventHandler<?> orderCreatedEventHandler(InventoryService inventoryService) {
        return new OrderCreatedEventHandler(inventoryService);
    }

    /**
     * 订单支付事件处理器Bean
     * <p>职责：处理订单支付事件（如更新库存、发货）
     * @return EventHandler
     */
    @Bean
    public EventHandler<?> orderPaidEventHandler() {
        return new OrderPaidEventHandler();
    }

    /**
     * 订单取消事件处理器Bean
     * <p>职责：处理订单取消事件（如释放库存、退款）
     * @return EventHandler
     */
    @Bean
    public EventHandler<?> orderCancelledEventHandler() {
        return new OrderCancelledEventHandler();
    }

    // ==================== 转换器 ====================

    /**
     * 订单聚合根转换器Bean
     * <p>职责：OrderAggr与OrderAggrDO之间的转换
     * @return OrderAggrConverter
     */
    @Bean
    public OrderAggrConverter orderAggrConverter() {
        return new OrderAggrConverter();
    }

    /**
     * 订单项转换器Bean
     * <p>职责：OrderItem与OrderItemDO之间的转换
     * @return OrderItemConverter
     */
    @Bean
    public OrderItemConverter orderItemConverter() {
        return new OrderItemConverter();
    }

}

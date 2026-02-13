package org.smm.archetype.app.exampleorder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smm.archetype.app.exampleorder.command.CancelOrderCommand;
import org.smm.archetype.app.exampleorder.command.CreateOrderCommand;
import org.smm.archetype.app.exampleorder.command.PayOrderCommand;
import org.smm.archetype.app.exampleorder.command.RefundOrderCommand;
import org.smm.archetype.app.exampleorder.command.ShipOrderCommand;
import org.smm.archetype.app.exampleorder.converter.OrderDtoConverter;
import org.smm.archetype.app.exampleorder.dto.OrderDTO;
import org.smm.archetype.app.exampleorder.query.GetOrderByIdQuery;
import org.smm.archetype.app.exampleorder.query.GetOrdersByCustomerQuery;
import org.smm.archetype.app.exampleorder.query.OrderListQuery;
import org.smm.archetype.domain.shared.base.PageResult;
import org.smm.archetype.domain.shared.event.DomainEventPublisher;
import org.smm.archetype.domain.exampleorder.model.OrderAggr;
import org.smm.archetype.domain.exampleorder.model.OrderItem;
import org.smm.archetype.domain.exampleorder.model.RefundType;
import org.smm.archetype.domain.exampleorder.model.valueobject.Money;
import org.smm.archetype.domain.exampleorder.repository.OrderAggrRepository;
import org.smm.archetype.domain.exampleorder.service.OrderDomainService;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 订单应用服务，编排订单相关用例和事务管理。
 * 
 * <p>使用 MapStruct 转换器进行 Domain→DTO 的转换。</p>
 */
@Slf4j
@RequiredArgsConstructor
public class OrderAppService {

    private final OrderAggrRepository orderRepository;
    private final OrderDomainService   orderDomainService;
    private final DomainEventPublisher domainEventPublisher;
    private final OrderDtoConverter    dtoConverter;

    /**
     * 创建订单用例
     * @param command 创建订单命令
     * @return 订单DTO
     */
    @Transactional(rollbackFor = Exception.class)
    public OrderDTO createOrder(CreateOrderCommand command) {
        log.info("创建订单: customerId={}, itemsCount={}",
                command.getCustomerId(), command.getItems().size());

        // 1. 验证订单项
        List<OrderItem> orderItems = orderDomainService.createOrderItems(command.getItems());
        orderDomainService.validateOrderItems(orderItems);

        // 2. 验证库存
        try {
            orderDomainService.validateInventory(command.getItems());
        } catch (Exception e) {
            log.error("验证库存失败: {}", e.getMessage());
            throw new RuntimeException("验证库存失败: " + e.getMessage());
        }

        // 3. 创建订单
        OrderAggr order = OrderAggr.create(
                OrderAggr.generateOrderNo(),
                command.getCustomerId(),
                command.getCustomerName(),
                new ArrayList<>(orderItems),
                command.getTotalAmount(),
                command.getShippingAddress(),
                command.getContactInfo(),
                command.getRemark()
        );

        // 4. 保存订单
        OrderAggr savedOrder = orderRepository.save(order);

        // 5. 锁定库存
        try {
            orderDomainService.lockInventory(savedOrder.getId(), savedOrder.getOrderNo(), command.getItems());
        } catch (Exception e) {
            log.error("锁定库存失败: {}", e.getMessage());
            throw new RuntimeException("锁定库存失败: " + e.getMessage());
        }

        log.info("订单创建成功: orderId={}, orderNo={}", savedOrder.getId(), savedOrder.getOrderNo());

        return dtoConverter.toDTO(savedOrder);
    }

    /**
     * 支付订单用例
     * @param command 支付订单命令
     * @return 订单DTO
     */
    @Transactional(rollbackFor = Exception.class)
    public OrderDTO payOrder(PayOrderCommand command) {
        log.info("支付订单: orderId={}, paymentMethod={}, amount={}",
                command.getOrderId(), command.getPaymentMethod(), command.getPaymentAmount());

        // 1. 查询订单
        OrderAggr order = findOrderById(command.getOrderId());

        // 2. 验证支付金额
        orderDomainService.validatePaymentAmount(order.getTotalAmount(), command.getPaymentAmount());

        // 3. 支付订单
        order.pay(command.getPaymentMethod(), command.getPaymentAmount());

        // 4. 保存订单
        OrderAggr savedOrder = orderRepository.save(order);

        log.info("订单支付成功: orderId={}", savedOrder.getId());

        return dtoConverter.toDTO(savedOrder);
    }

    /**
     * 取消订单用例
     * @param command 取消订单命令
     * @return 订单DTO
     */
    @Transactional(rollbackFor = Exception.class)
    public OrderDTO cancelOrder(CancelOrderCommand command) {
        log.info("取消订单: orderId={}, reason={}", command.getOrderId(), command.getReason());

        // 1. 查询订单
        OrderAggr order = findOrderById(command.getOrderId());

        // 2. 取消订单
        order.cancel(command.getReason());

        // 3. 释放库存
        try {
            orderDomainService.releaseInventory(order.getId(), order.getOrderNo());
        } catch (Exception e) {
            log.error("释放库存失败: {}", e.getMessage());
            // 释放库存失败不影响订单取消流程
        }

        // 4. 保存订单
        OrderAggr savedOrder = orderRepository.save(order);

        log.info("订单取消成功: orderId={}", savedOrder.getId());

        return dtoConverter.toDTO(savedOrder);
    }

    /**
     * 退款订单用例
     * @param command 退款订单命令
     * @return 订单DTO
     */
    @Transactional(rollbackFor = Exception.class)
    public OrderDTO refundOrder(RefundOrderCommand command) {
        log.info("退款订单: orderId={}, refundAmount={}, refundType={}",
                command.getOrderId(), command.getRefundAmount(), command.getRefundType());

        // 1. 查询订单
        OrderAggr order = findOrderById(command.getOrderId());

        // 2. 构建退款金额对象
        Money refundAmount = Money.of(new java.math.BigDecimal(command.getRefundAmount()), command.getCurrency());

        // 3. 解析退款类型
        RefundType refundType = RefundType.valueOf(command.getRefundType());

        // 4. 执行退款
        order.refund(refundAmount, refundType, command.getRefundReason());

        // 5. 保存订单
        OrderAggr savedOrder = orderRepository.save(order);

        // 6. 释放库存（可选，根据业务需求）
        try {
            orderDomainService.releaseInventory(order.getId(), order.getOrderNo());
        } catch (Exception e) {
            log.error("释放库存失败: {}", e.getMessage());
            // 释放库存失败不影响退款流程
        }

        log.info("订单退款成功: orderId={}, refundAmount={}", savedOrder.getId(), refundAmount);

        return dtoConverter.toDTO(savedOrder);
    }

    /**
     * 发货订单用例
     * @param command 发货订单命令
     * @return 订单DTO
     */
    @Transactional(rollbackFor = Exception.class)
    public OrderDTO shipOrder(ShipOrderCommand command) {
        log.info("发货订单: orderId={}", command.getOrderId());

        // 1. 查询订单
        OrderAggr order = findOrderById(command.getOrderId());

        // 2. 发货订单
        order.ship();

        // 3. 保存订单
        OrderAggr savedOrder = orderRepository.save(order);

        log.info("订单发货成功: orderId={}", savedOrder.getId());

        return dtoConverter.toDTO(savedOrder);
    }

    /**
     * 完成订单用例
     * @param orderId 订单ID
     * @return 订单DTO
     */
    @Transactional(rollbackFor = Exception.class)
    public OrderDTO completeOrder(Long orderId) {
        log.info("完成订单: orderId={}", orderId);

        // 1. 查询订单
        OrderAggr order = findOrderById(orderId);

        // 2. 完成订单
        order.complete();

        // 3. 保存订单
        OrderAggr savedOrder = orderRepository.save(order);

        log.info("订单完成成功: orderId={}", savedOrder.getId());

        return dtoConverter.toDTO(savedOrder);
    }

    /**
     * 根据ID查询订单
     * @param query 查询对象
     * @return 订单DTO
     */
    @Transactional(readOnly = true)
    public OrderDTO getOrderById(GetOrderByIdQuery query) {
        OrderAggr order = findOrderById(query.getOrderId());
        return dtoConverter.toDTO(order);
    }

    /**
     * 查询客户订单列表
     * @param query 查询对象
     * @return 订单DTO列表
     */
    @Transactional(readOnly = true)
    public List<OrderDTO> getOrdersByCustomer(GetOrdersByCustomerQuery query) {
        List<OrderAggr> orders = orderRepository.findByCustomerId(query.getCustomerId());
        return dtoConverter.toDTOList(orders);
    }

    /**
     * 查询订单列表（分页）
     * @param query 查询对象
     * @return 分页结果
     */
    @Transactional(readOnly = true)
    public org.smm.archetype.app.shared.result.PageResult<List<OrderDTO>> getOrderList(OrderListQuery query) {
        // 调用Repository分页查询
        PageResult<OrderAggr> page = orderRepository.findOrders(
                query.getCustomerId(),
                query.getPageNumber(),
                query.getPageSize()
        );

        // 使用转换器转换为DTO
        List<OrderDTO> dtos = dtoConverter.toDTOList(page.getRecords());

        // 构建返回结果
        return org.smm.archetype.app.shared.result.PageResult.<List<OrderDTO>>PRBuilder()
                       .setData(dtos)
                       .setPageNumber(page.getPageNumber())
                       .setPageSize(page.getPageSize())
                       .setTotalRaw(page.getTotalRaw())
                       .build();
    }

    /**
     * 检查订单号是否存在
     * @param orderNo 订单号
     * @return 是否存在
     */
    @Transactional(readOnly = true)
    public boolean existsByOrderNo(String orderNo) {
        return orderRepository.existsByOrderNo(orderNo);
    }

    /**
     * 检查订单是否可以支付
     * <p>业务规则：只有 CREATED 状态的订单才能支付</p>
     * @param orderId 订单ID
     * @return 是否可以支付
     */
    @Transactional(readOnly = true)
    public boolean canPayOrder(Long orderId) {
        log.info("检查订单是否可支付: orderId={}", orderId);
        
        OrderAggr order = findOrderById(orderId);
        boolean canPay = order.canPay();
        
        log.info("订单可支付检查结果: orderId={}, canPay={}", orderId, canPay);
        return canPay;
    }

    // ========== 私有方法 ==========

    /**
     * 根据ID查询订单，不存在则抛出异常
     */
    private OrderAggr findOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                       .orElseThrow(() -> new RuntimeException("订单不存在: " + orderId));
    }

}

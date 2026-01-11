package org.smm.archetype.app._example.order;

import lombok.extern.slf4j.Slf4j;
import org.smm.archetype.app._example.order.command.CancelOrderCommand;
import org.smm.archetype.app._example.order.command.CreateOrderCommand;
import org.smm.archetype.app._example.order.command.PayOrderCommand;
import org.smm.archetype.app._example.order.command.ShipOrderCommand;
import org.smm.archetype.app._example.order.dto.AddressDTO;
import org.smm.archetype.app._example.order.dto.ContactInfoDTO;
import org.smm.archetype.app._example.order.dto.MoneyDTO;
import org.smm.archetype.app._example.order.dto.OrderDTO;
import org.smm.archetype.app._example.order.dto.OrderItemDTO;
import org.smm.archetype.app._example.order.query.GetOrderByIdQuery;
import org.smm.archetype.app._example.order.query.GetOrdersByCustomerQuery;
import org.smm.archetype.app._example.order.query.OrderListQuery;
import org.smm.archetype.app._shared.result.PageResult;
import org.smm.archetype.domain._example.order.model.OrderAggr;
import org.smm.archetype.domain._example.order.model.OrderItem;
import org.smm.archetype.domain._example.order.model.valueobject.Address;
import org.smm.archetype.domain._example.order.model.valueobject.ContactInfo;
import org.smm.archetype.domain._example.order.model.valueobject.Money;
import org.smm.archetype.domain._example.order.repository.OrderAggrRepository;
import org.smm.archetype.domain._example.order.service.OrderDomainService;
import org.smm.archetype.domain._shared.event.EventPublisher;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 订单应用服务
 *
 * <p>职责：
 * <ul>
 *   <li>编排订单相关用例</li>
 *   <li>管理事务边界</li>
 *   <li>DTO转换</li>
 *   <li>发布领域事件</li>
 * </ul>
 * @author Leonardo
 * @since 2026/1/11
 */
@Slf4j
public class OrderAppService {

    private final OrderAggrRepository orderRepository;
    private final OrderDomainService  orderDomainService;
    private final EventPublisher      eventPublisher;

    public OrderAppService(OrderAggrRepository orderRepository,
                           OrderDomainService orderDomainService,
                           EventPublisher eventPublisher) {
        this.orderRepository = orderRepository;
        this.orderDomainService = orderDomainService;
        this.eventPublisher = eventPublisher;
    }

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

        // 6. 发布领域事件
        publishDomainEvents(savedOrder);

        log.info("订单创建成功: orderId={}, orderNo={}", savedOrder.getId(), savedOrder.getOrderNo());

        return toDTO(savedOrder);
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

        // 5. 发布领域事件
        publishDomainEvents(savedOrder);

        log.info("订单支付成功: orderId={}", savedOrder.getId());

        return toDTO(savedOrder);
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

        // 5. 发布领域事件
        publishDomainEvents(savedOrder);

        log.info("订单取消成功: orderId={}", savedOrder.getId());

        return toDTO(savedOrder);
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

        // 4. 发布领域事件
        publishDomainEvents(savedOrder);

        log.info("订单发货成功: orderId={}", savedOrder.getId());

        return toDTO(savedOrder);
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

        // 4. 发布领域事件
        publishDomainEvents(savedOrder);

        log.info("订单完成成功: orderId={}", savedOrder.getId());

        return toDTO(savedOrder);
    }

    /**
     * 根据ID查询订单
     * @param query 查询对象
     * @return 订单DTO
     */
    @Transactional(readOnly = true)
    public OrderDTO getOrderById(GetOrderByIdQuery query) {
        OrderAggr order = findOrderById(query.getOrderId());
        return toDTO(order);
    }

    /**
     * 查询客户订单列表
     * @param query 查询对象
     * @return 订单DTO列表
     */
    @Transactional(readOnly = true)
    public List<OrderDTO> getOrdersByCustomer(GetOrdersByCustomerQuery query) {
        List<OrderAggr> orders = orderRepository.findByCustomerId(query.getCustomerId());
        return orders.stream()
                       .map(this::toDTO)
                       .collect(Collectors.toList());
    }

    /**
     * 查询订单列表（分页）
     * @param query 查询对象
     * @return 分页结果
     */
    @Transactional(readOnly = true)
    public PageResult<OrderDTO> getOrderList(OrderListQuery query) {
        // 调用Repository分页查询
        org.smm.archetype.domain._shared.base.PageModel<OrderAggr> page = orderRepository.findOrders(
                query.getCustomerId(),
                query.getPageNumber(),
                query.getPageSize()
        );

        // 转换为DTO
        List<OrderDTO> dtos = page.getRecords().stream()
                                      .map(this::toDTO)
                                      .collect(Collectors.toList());

        // 构建返回结果
        return PageResult.<OrderDTO>builder()
                       .pageNumber(page.getPageNumber())
                       .pageSize(page.getPageSize())
                       .records(dtos)
                       .totalRaw(page.getTotalRaw())
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

    // ========== 私有方法 ==========

    /**
     * 根据ID查询订单，不存在则抛出异常
     */
    private OrderAggr findOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                       .orElseThrow(() -> new RuntimeException("订单不存在: " + orderId));
    }

    /**
     * 发布领域事件
     */
    private void publishDomainEvents(OrderAggr order) {
        if (order.hasUncommittedEvents()) {
            eventPublisher.publish(order.getUncommittedEvents());
            order.markEventsAsCommitted();
            log.debug("发布领域事件: orderId={}, eventsCount={}",
                    order.getId(), order.getUncommittedEvents().size());
        }
    }

    /**
     * 转换为DTO
     */
    private OrderDTO toDTO(OrderAggr order) {
        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setOrderNo(order.getOrderNo());
        dto.setCustomerId(order.getCustomerId());
        dto.setCustomerName(order.getCustomerName());
        dto.setStatus(order.getStatus());
        dto.setPaymentMethod(order.getPaymentMethod());
        dto.setTotalAmount(toMoneyDTO(order.getTotalAmount()));
        dto.setItems(toOrderItemDTOs(order.getItems()));
        dto.setShippingAddress(toAddressDTO(order.getShippingAddress()));
        dto.setContactInfo(toContactInfoDTO(order.getContactInfo()));
        dto.setRemark(order.getRemark());
        dto.setCreateTime(order.getCreateTime());
        dto.setPaymentTime(order.getPaymentTime());
        dto.setShippedTime(order.getShippedTime());
        dto.setCompletedTime(order.getCompletedTime());
        dto.setCancelledTime(order.getCancelledTime());
        dto.setCancelReason(order.getCancelReason());
        return dto;
    }

    private MoneyDTO toMoneyDTO(Money money) {
        if (money == null) {
            return null;
        }
        return new MoneyDTO(money.getAmount(), money.getCurrency());
    }

    private AddressDTO toAddressDTO(Address address) {
        if (address == null) {
            return null;
        }
        AddressDTO dto = new AddressDTO();
        dto.setProvince(address.getProvince());
        dto.setCity(address.getCity());
        dto.setDistrict(address.getDistrict());
        dto.setDetailAddress(address.getDetailAddress());
        dto.setPostalCode(address.getPostalCode());
        return dto;
    }

    private ContactInfoDTO toContactInfoDTO(ContactInfo contactInfo) {
        if (contactInfo == null) {
            return null;
        }
        ContactInfoDTO dto = new ContactInfoDTO();
        dto.setContactName(contactInfo.getContactName());
        dto.setContactPhone(contactInfo.getContactPhone());
        dto.setContactEmail(contactInfo.getContactEmail());
        return dto;
    }

    private List<OrderItemDTO> toOrderItemDTOs(List<OrderItem> items) {
        if (items == null) {
            return new ArrayList<>();
        }
        return items.stream()
                       .map(this::toOrderItemDTO)
                       .collect(Collectors.toList());
    }

    private OrderItemDTO toOrderItemDTO(OrderItem item) {
        OrderItemDTO dto = new OrderItemDTO();
        dto.setProductId(item.getProductId());
        dto.setProductName(item.getProductName());
        dto.setSkuCode(item.getSkuCode());
        dto.setUnitPrice(item.getUnitPrice() != null ? item.getUnitPrice().getAmount() : null);
        dto.setQuantity(item.getQuantity());
        dto.setSubtotal(item.getSubtotal() != null ? item.getSubtotal().getAmount() : null);
        dto.setCurrency(item.getCurrency());
        return dto;
    }

}

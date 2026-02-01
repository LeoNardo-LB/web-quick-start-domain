package org.smm.archetype.app._example;

import lombok.extern.slf4j.Slf4j;
import org.smm.archetype.app._example.command.CancelOrderCommand;
import org.smm.archetype.app._example.command.CreateOrderCommand;
import org.smm.archetype.app._example.command.PayOrderCommand;
import org.smm.archetype.app._example.command.ShipOrderCommand;
import org.smm.archetype.app._example.dto.AddressDTO;
import org.smm.archetype.app._example.dto.AddressDTO.AddressDTOBuilder;
import org.smm.archetype.app._example.dto.ContactInfoDTO;
import org.smm.archetype.app._example.dto.ContactInfoDTO.ContactInfoDTOBuilder;
import org.smm.archetype.app._example.dto.MoneyDTO;
import org.smm.archetype.app._example.dto.MoneyDTO.MoneyDTOBuilder;
import org.smm.archetype.app._example.dto.OrderDTO;
import org.smm.archetype.app._example.dto.OrderDTO.OrderDTOBuilder;
import org.smm.archetype.app._example.dto.OrderItemDTO;
import org.smm.archetype.app._example.dto.OrderItemDTO.OrderItemDTOBuilder;
import org.smm.archetype.app._example.query.GetOrderByIdQuery;
import org.smm.archetype.app._example.query.GetOrdersByCustomerQuery;
import org.smm.archetype.app._example.query.OrderListQuery;
import org.smm.archetype.domain.bizshared.base.PageResult;
import org.smm.archetype.domain.bizshared.event.DomainEventPublisher;
import org.smm.archetype.domain.example.model.OrderAggr;
import org.smm.archetype.domain.example.model.OrderItem;
import org.smm.archetype.domain.example.model.valueobject.Address;
import org.smm.archetype.domain.example.model.valueobject.ContactInfo;
import org.smm.archetype.domain.example.model.valueobject.Money;
import org.smm.archetype.domain.example.repository.OrderAggrRepository;
import org.smm.archetype.domain.example.service.OrderDomainService;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 订单应用服务，编排订单相关用例和事务管理。
 */
@Slf4j
public class OrderAppService {

    private final OrderAggrRepository orderRepository;
    private final OrderDomainService   orderDomainService;
    private final DomainEventPublisher domainEventPublisher;

    public OrderAppService(OrderAggrRepository orderRepository,
                           OrderDomainService orderDomainService,
                           DomainEventPublisher domainEventPublisher) {
        this.orderRepository = orderRepository;
        this.orderDomainService = orderDomainService;
        this.domainEventPublisher = domainEventPublisher;
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
    public org.smm.archetype.app.bizshared.result.PageResult<List<OrderDTO>> getOrderList(OrderListQuery query) {
        // 调用Repository分页查询
        PageResult<OrderAggr> page = orderRepository.findOrders(
                query.getCustomerId(),
                query.getPageNumber(),
                query.getPageSize()
        );

        // 转换为DTO
        List<OrderDTO> dtos = page.getRecords().stream()
                                      .map(this::toDTO)
                                      .collect(Collectors.toList());

        // 构建返回结果
        return org.smm.archetype.app.bizshared.result.PageResult.<List<OrderDTO>>PRBuilder()
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

    // ========== 私有方法 ==========

    /**
     * 根据ID查询订单，不存在则抛出异常
     */
    private OrderAggr findOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                       .orElseThrow(() -> new RuntimeException("订单不存在: " + orderId));
    }

    /**
     * 转换为DTO
     */
    private OrderDTO toDTO(OrderAggr order) {
        OrderDTOBuilder builder = OrderDTO.builder();
        builder.setId(order.getId());
        builder.setOrderNo(order.getOrderNo());
        builder.setCustomerId(order.getCustomerId());
        builder.setCustomerName(order.getCustomerName());
        builder.setStatus(order.getStatus());
        builder.setPaymentMethod(order.getPaymentMethod());
        builder.setTotalAmount(toMoneyDTO(order.getTotalAmount()));
        builder.setItems(toOrderItemDTOs(order.getItems()));
        builder.setShippingAddress(toAddressDTO(order.getShippingAddress()));
        builder.setContactInfo(toContactInfoDTO(order.getContactInfo()));
        builder.setRemark(order.getRemark());
        builder.setCreateTime(order.getCreateTime());
        builder.setPaymentTime(order.getPaymentTime());
        builder.setShippedTime(order.getShippedTime());
        builder.setCompletedTime(order.getCompletedTime());
        builder.setCancelledTime(order.getCancelledTime());
        builder.setCancelReason(order.getCancelReason());
        return builder.build();
    }

    private MoneyDTO toMoneyDTO(Money money) {
        if (money == null) {
            return null;
        }
        MoneyDTOBuilder builder = MoneyDTO.builder();
        builder.setAmount(money.getAmount());
        builder.setCurrency(money.getCurrency());
        return builder.build();
    }

    private AddressDTO toAddressDTO(Address address) {
        if (address == null) {
            return null;
        }
        AddressDTOBuilder builder = AddressDTO.builder();
        builder.setProvince(address.getProvince());
        builder.setCity(address.getCity());
        builder.setDistrict(address.getDistrict());
        builder.setDetailAddress(address.getDetailAddress());
        builder.setPostalCode(address.getPostalCode());
        return builder.build();
    }

    private ContactInfoDTO toContactInfoDTO(ContactInfo contactInfo) {
        if (contactInfo == null) {
            return null;
        }
        ContactInfoDTOBuilder builder = ContactInfoDTO.builder();
        builder.setContactName(contactInfo.getContactName());
        builder.setContactPhone(contactInfo.getContactPhone());
        builder.setContactEmail(contactInfo.getContactEmail());
        return builder.build();
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
        OrderItemDTOBuilder builder = OrderItemDTO.builder();
        builder.setProductId(item.getProductId());
        builder.setProductName(item.getProductName());
        builder.setSkuCode(item.getSkuCode());
        builder.setUnitPrice(item.getUnitPrice() != null ? item.getUnitPrice().getAmount() : null);
        builder.setQuantity(item.getQuantity());
        builder.setSubtotal(item.getSubtotal() != null ? item.getSubtotal().getAmount() : null);
        builder.setCurrency(item.getCurrency());
        return builder.build();
    }

}

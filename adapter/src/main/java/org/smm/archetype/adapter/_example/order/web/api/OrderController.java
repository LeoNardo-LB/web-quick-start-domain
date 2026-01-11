package org.smm.archetype.adapter._example.order.web.api;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.smm.archetype.adapter._example.order.web.dto.request.CancelOrderRequest;
import org.smm.archetype.adapter._example.order.web.dto.request.CreateOrderRequest;
import org.smm.archetype.adapter._example.order.web.dto.request.PayOrderRequest;
import org.smm.archetype.adapter._example.order.web.dto.response.OrderResponse;
import org.smm.archetype.adapter._shared.result.BaseResult;
import org.smm.archetype.app._example.order.OrderAppService;
import org.smm.archetype.app._example.order.command.CancelOrderCommand;
import org.smm.archetype.app._example.order.command.CreateOrderCommand;
import org.smm.archetype.app._example.order.command.PayOrderCommand;
import org.smm.archetype.app._example.order.command.ShipOrderCommand;
import org.smm.archetype.app._example.order.dto.OrderDTO;
import org.smm.archetype.app._example.order.query.GetOrderByIdQuery;
import org.smm.archetype.app._example.order.query.GetOrdersByCustomerQuery;
import org.smm.archetype.domain._example.order.model.valueobject.Address;
import org.smm.archetype.domain._example.order.model.valueobject.ContactInfo;
import org.smm.archetype.domain._example.order.model.valueobject.OrderItemInfo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 订单控制器
 *
 * <p>提供订单相关的HTTP接口
 * @author Leonardo
 * @since 2026/1/11
 */
@Slf4j
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderAppService orderApplicationService;

    public OrderController(OrderAppService orderApplicationService) {
        this.orderApplicationService = orderApplicationService;
    }

    /**
     * 创建订单
     * @param request 创建订单请求
     * @return 订单响应
     */
    @PostMapping
    public BaseResult<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        log.info("创建订单: customerId={}, itemsCount={}",
                request.getCustomerId(), request.getItems().size());

        try {
            // 转换请求为命令
            List<OrderItemInfo> items = request.getItems().stream()
                                                .map(item -> OrderItemInfo.builder()
                                                                     .productId(item.getProductId())
                                                                     .productName(item.getProductName())
                                                                     .skuCode(item.getSkuCode())
                                                                     .unitPrice(item.getUnitPrice())
                                                                     .quantity(item.getQuantity())
                                                                     .build())
                                                .toList();

            Address address = Address.builder()
                                      .province(request.getShippingAddress().getProvince())
                                      .city(request.getShippingAddress().getCity())
                                      .district(request.getShippingAddress().getDistrict())
                                      .detailAddress(request.getShippingAddress().getDetailAddress())
                                      .postalCode(request.getShippingAddress().getPostalCode())
                                      .build();

            ContactInfo contactInfo = ContactInfo.builder()
                                              .contactName(request.getContactInfo().getContactName())
                                              .contactPhone(request.getContactInfo().getContactPhone())
                                              .contactEmail(request.getContactInfo().getContactEmail())
                                              .build();

            CreateOrderCommand command = new CreateOrderCommand(
                    request.getCustomerId(),
                    request.getCustomerName(),
                    items,
                    request.getTotalAmount(),
                    address,
                    contactInfo,
                    request.getRemark()
            );

            // 调用应用服务
            OrderDTO orderDTO = orderApplicationService.createOrder(command);

            // 转换为响应
            OrderResponse response = OrderResponse.fromDTO(orderDTO);

            return BaseResult.success(response);

        } catch (Exception e) {
            log.error("创建订单失败: {}", e.getMessage(), e);
            return BaseResult.error(500, "创建订单失败: " + e.getMessage());
        }
    }

    /**
     * 支付订单
     * @param orderId 订单ID
     * @param request 支付订单请求
     * @return 订单响应
     */
    @PostMapping("/{orderId}/pay")
    public BaseResult<OrderResponse> payOrder(
            @PathVariable Long orderId,
            @RequestBody PayOrderRequest request) {
        log.info("支付订单: orderId={}, paymentMethod={}", orderId, request.getPaymentMethod());

        try {
            PayOrderCommand command = new PayOrderCommand(
                    orderId,
                    request.getPaymentMethod(),
                    request.getPaymentAmount()
            );

            OrderDTO orderDTO = orderApplicationService.payOrder(command);
            OrderResponse response = OrderResponse.fromDTO(orderDTO);

            return BaseResult.success(response);

        } catch (Exception e) {
            log.error("支付订单失败: {}", e.getMessage(), e);
            return BaseResult.error(500, "支付订单失败: " + e.getMessage());
        }
    }

    /**
     * 取消订单
     * @param orderId 订单ID
     * @param request 取消订单请求
     * @return 订单响应
     */
    @PostMapping("/{orderId}/cancel")
    public BaseResult<OrderResponse> cancelOrder(
            @PathVariable Long orderId,
            @RequestBody CancelOrderRequest request) {
        log.info("取消订单: orderId={}, reason={}", orderId, request.getReason());

        try {
            CancelOrderCommand command = new CancelOrderCommand(orderId, request.getReason());
            OrderDTO orderDTO = orderApplicationService.cancelOrder(command);
            OrderResponse response = OrderResponse.fromDTO(orderDTO);

            return BaseResult.success(response);

        } catch (Exception e) {
            log.error("取消订单失败: {}", e.getMessage(), e);
            return BaseResult.error(500, "取消订单失败: " + e.getMessage());
        }
    }

    /**
     * 发货订单
     * @param orderId 订单ID
     * @return 订单响应
     */
    @PostMapping("/{orderId}/ship")
    public BaseResult<OrderResponse> shipOrder(@PathVariable Long orderId) {
        log.info("发货订单: orderId={}", orderId);

        try {
            ShipOrderCommand command = new ShipOrderCommand(orderId);
            OrderDTO orderDTO = orderApplicationService.shipOrder(command);
            OrderResponse response = OrderResponse.fromDTO(orderDTO);

            return BaseResult.success(response);

        } catch (Exception e) {
            log.error("发货订单失败: {}", e.getMessage(), e);
            return BaseResult.error(500, "发货订单失败: " + e.getMessage());
        }
    }

    /**
     * 完成订单
     * @param orderId 订单ID
     * @return 订单响应
     */
    @PostMapping("/{orderId}/complete")
    public BaseResult<OrderResponse> completeOrder(@PathVariable Long orderId) {
        log.info("完成订单: orderId={}", orderId);

        try {
            OrderDTO orderDTO = orderApplicationService.completeOrder(orderId);
            OrderResponse response = OrderResponse.fromDTO(orderDTO);

            return BaseResult.success(response);

        } catch (Exception e) {
            log.error("完成订单失败: {}", e.getMessage(), e);
            return BaseResult.error(500, "完成订单失败: " + e.getMessage());
        }
    }

    /**
     * 查询订单详情
     * @param orderId 订单ID
     * @return 订单响应
     */
    @GetMapping("/{orderId}")
    public BaseResult<OrderResponse> getOrderById(@PathVariable Long orderId) {
        log.info("查询订单详情: orderId={}", orderId);

        try {
            GetOrderByIdQuery query = new GetOrderByIdQuery(orderId);
            OrderDTO orderDTO = orderApplicationService.getOrderById(query);
            OrderResponse response = OrderResponse.fromDTO(orderDTO);

            return BaseResult.success(response);

        } catch (Exception e) {
            log.error("查询订单详情失败: {}", e.getMessage(), e);
            return BaseResult.error(500, "查询订单详情失败: " + e.getMessage());
        }
    }

    /**
     * 查询客户订单列表
     * @param customerId 客户ID
     * @return 订单响应列表
     */
    @GetMapping("/customer/{customerId}")
    public BaseResult<List<OrderResponse>> getOrdersByCustomer(@PathVariable String customerId) {
        log.info("查询客户订单列表: customerId={}", customerId);

        try {
            GetOrdersByCustomerQuery query = new GetOrdersByCustomerQuery(customerId);
            List<OrderDTO> orderDTOs = orderApplicationService.getOrdersByCustomer(query);

            List<OrderResponse> responses = orderDTOs.stream()
                                                    .map(OrderResponse::fromDTO)
                                                    .toList();

            return BaseResult.success(responses);

        } catch (Exception e) {
            log.error("查询客户订单列表失败: {}", e.getMessage(), e);
            return BaseResult.error(500, "查询客户订单列表失败: " + e.getMessage());
        }
    }

}

package org.smm.archetype.adapter.access.web.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smm.archetype.adapter._shared.result.BaseResult;
import org.smm.archetype.adapter.access.web.converter.OrderDTOConverter;
import org.smm.archetype.adapter.access.web.dto.CreateOrderRequest;
import org.smm.archetype.adapter.access.web.dto.OrderDTO;
import org.smm.archetype.adapter.access.web.dto.PayOrderRequest;
import org.smm.archetype.app._example.order.command.CancelOrderCommand;
import org.smm.archetype.app._example.order.command.PayOrderCommand;
import org.smm.archetype.app._example.order.query.GetOrderQuery;
import org.smm.archetype.app._example.order.query.SearchOrdersQuery;
import org.smm.archetype.app._example.order.service.OrderApplicationService;
import org.smm.archetype.domain._example.order.model.Order;
import org.smm.archetype.domain._example.order.model.OrderStatus;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

/**
 * 订单控制器
 *
 * <p>提供订单管理的REST API
 * @author Leonardo
 * @since 2025/12/30
 */
@Slf4j
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderApplicationService orderApplicationService;
    private final OrderDTOConverter orderDTOConverter;

    /**
     * 创建订单
     *
     * POST /api/orders
     */
    @PostMapping
    public ResponseEntity<BaseResult<Long>> createOrder(
            @Valid @RequestBody CreateOrderRequest request
    ) {
        log.info("Creating order for customer: {}", request.getCustomerId());

        // 1. Request -> Command
        var command = orderDTOConverter.toCommand(request);

        // 2. 调用应用服务
        Long orderId = orderApplicationService.createOrder(command);

        // 3. 返回结果
        return ResponseEntity
                       .created(URI.create("/api/orders/" + orderId))
                       .body(BaseResult.success(orderId));
    }

    /**
     * 获取订单详情
     *
     * GET /api/orders/{orderId}
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<BaseResult<OrderDTO>> getOrder(
            @PathVariable Long orderId
    ) {
        log.info("Getting order: {}", orderId);

        // 1. 构造查询对象
        GetOrderQuery query = new GetOrderQuery(orderId);

        // 2. 调用应用服务
        Order order = orderApplicationService.getOrder(query);

        // 3. 转换为DTO
        OrderDTO dto = orderDTOConverter.toDTO(order);

        return ResponseEntity.ok(BaseResult.success(dto));
    }

    /**
     * 搜索订单
     *
     * GET /api/orders?customerId={customerId}&status={status}
     */
    @GetMapping
    public ResponseEntity<BaseResult<List<OrderDTO>>> searchOrders(
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) String status
    ) {
        log.info("Searching orders: customerId={}, status={}", customerId, status);

        // 1. 构造查询对象
        SearchOrdersQuery query = SearchOrdersQuery.builder()
                                          .customerId(customerId)
                                          .status(status != null ? OrderStatus.valueOf(status) : null)
                                          .build();

        // 2. 调用应用服务
        List<Order> orders = orderApplicationService.searchOrders(query);

        // 3. 转换为DTO列表
        List<OrderDTO> dtos = orderDTOConverter.toDTOList(orders);

        return ResponseEntity.ok(BaseResult.success(dtos));
    }

    /**
     * 支付订单
     *
     * POST /api/orders/{orderId}/pay
     */
    @PostMapping("/{orderId}/pay")
    public ResponseEntity<BaseResult<Void>> payOrder(
            @PathVariable Long orderId,
            @Valid @RequestBody PayOrderRequest request
    ) {
        log.info("Paying order: {} with method: {}", orderId, request.getPaymentMethod());

        // 1. Request -> Command
        PayOrderCommand command = new PayOrderCommand(orderId, request.getPaymentMethod());

        // 2. 调用应用服务
        orderApplicationService.payOrder(command);

        return ResponseEntity.ok(BaseResult.success());
    }

    /**
     * 取消订单
     *
     * POST /api/orders/{orderId}/cancel
     */
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<BaseResult<Void>> cancelOrder(
            @PathVariable Long orderId,
            @RequestParam(required = false, defaultValue = "用户取消") String reason
    ) {
        log.info("Cancelling order: {}, reason: {}", orderId, reason);

        // 1. 构造Command
        var command = CancelOrderCommand.builder()
                              .orderId(orderId)
                              .reason(reason)
                              .build();

        // 2. 调用应用服务
        orderApplicationService.cancelOrder(command);

        return ResponseEntity.ok(BaseResult.success());
    }

    /**
     * 发货
     *
     * POST /api/orders/{orderId}/ship
     */
    @PostMapping("/{orderId}/ship")
    public ResponseEntity<BaseResult<Void>> shipOrder(
            @PathVariable Long orderId,
            @RequestParam String trackingNumber
    ) {
        log.info("Shipping order: {}, tracking: {}", orderId, trackingNumber);

        orderApplicationService.shipOrder(orderId, trackingNumber);

        return ResponseEntity.ok(BaseResult.success());
    }

    /**
     * 完成订单
     *
     * POST /api/orders/{orderId}/complete
     */
    @PostMapping("/{orderId}/complete")
    public ResponseEntity<BaseResult<Void>> completeOrder(
            @PathVariable Long orderId
    ) {
        log.info("Completing order: {}", orderId);

        orderApplicationService.completeOrder(orderId);

        return ResponseEntity.ok(BaseResult.success());
    }

    /**
     * 异常处理示例
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<BaseResult<Void>> handleIllegalArgumentException(
            IllegalArgumentException e
    ) {
        log.error("Illegal argument: {}", e.getMessage());
        return ResponseEntity
                       .status(HttpStatus.BAD_REQUEST)
                       .body(BaseResult.error(400, e.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<BaseResult<Void>> handleIllegalStateException(
            IllegalStateException e
    ) {
        log.error("Illegal state: {}", e.getMessage());
        return ResponseEntity
                       .status(HttpStatus.CONFLICT)
                       .body(BaseResult.error(409, e.getMessage()));
    }

}

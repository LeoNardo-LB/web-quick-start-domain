package org.smm.archetype.adapter.exampleorder.web.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smm.archetype.adapter.exampleorder.converter.OrderRequestConverter;
import org.smm.archetype.adapter.exampleorder.converter.OrderResponseConverter;
import org.smm.archetype.adapter.exampleorder.web.dto.request.CancelOrderRequest;
import org.smm.archetype.adapter.exampleorder.web.dto.request.CreateOrderRequest;
import org.smm.archetype.adapter.exampleorder.web.dto.request.PayOrderRequest;
import org.smm.archetype.adapter.exampleorder.web.dto.request.RefundOrderRequest;
import org.smm.archetype.adapter.exampleorder.web.dto.response.OrderResponse;
import org.smm.archetype.app.shared.result.BaseResult;
import org.smm.archetype.app.shared.result.PageResult;
import org.smm.archetype.app.exampleorder.OrderAppService;
import org.smm.archetype.app.exampleorder.command.PayOrderCommand;
import org.smm.archetype.app.exampleorder.command.ShipOrderCommand;
import org.smm.archetype.app.exampleorder.dto.OrderDTO;
import org.smm.archetype.app.exampleorder.query.GetOrderByIdQuery;
import org.smm.archetype.app.exampleorder.query.GetOrdersByCustomerQuery;
import org.smm.archetype.app.exampleorder.query.OrderListQuery;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

/**
 * 订单控制器，提供订单相关的HTTP接口。
 * 
 * <p>使用 MapStruct 转换器进行 Request→Command 和 DTO→Response 的转换。</p>
 */
@Slf4j
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderAppService orderApplicationService;
    private final OrderRequestConverter requestConverter;
    private final OrderResponseConverter responseConverter;

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
            // 使用转换器将请求转换为命令
            var command = requestConverter.toCommand(request);

            // 调用应用服务
            OrderDTO orderDTO = orderApplicationService.createOrder(command);

            // 使用转换器将 DTO 转换为响应
            OrderResponse response = responseConverter.toResponse(orderDTO);

            return BaseResult.success(response);

        } catch (Exception e) {
            log.error("创建订单失败: {}", e.getMessage(), e);
            return BaseResult.error("500", "创建订单失败: " + e.getMessage());
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
            // 使用转换器将请求转换为命令，并设置订单ID
            var command = requestConverter.toCommand(request);
            command.setOrderId(orderId);

            OrderDTO orderDTO = orderApplicationService.payOrder(command);
            OrderResponse response = responseConverter.toResponse(orderDTO);

            return BaseResult.success(response);

        } catch (Exception e) {
            log.error("支付订单失败: {}", e.getMessage(), e);
            return BaseResult.error("500", "支付订单失败: " + e.getMessage());
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
            // 使用转换器将请求转换为命令，并设置订单ID
            var command = requestConverter.toCommand(request);
            command.setOrderId(orderId);
            
            OrderDTO orderDTO = orderApplicationService.cancelOrder(command);
            OrderResponse response = responseConverter.toResponse(orderDTO);

            return BaseResult.success(response);

        } catch (Exception e) {
            log.error("取消订单失败: {}", e.getMessage(), e);
            return BaseResult.error("500", "取消订单失败: " + e.getMessage());
        }
    }

    /**
     * 退款订单
     * @param orderId 订单ID
     * @param request 退款订单请求
     * @return 订单响应
     */
    @PostMapping("/{orderId}/refund")
    public BaseResult<OrderResponse> refundOrder(
            @PathVariable Long orderId,
            @Valid @RequestBody RefundOrderRequest request) {
        log.info("退款订单: orderId={}, refundAmount={}, refundType={}",
                orderId, request.getRefundAmount(), request.getRefundType());

        try {
            // 使用转换器将请求转换为命令，并设置订单ID
            var command = requestConverter.toCommand(request);
            command.setOrderId(orderId);

            OrderDTO orderDTO = orderApplicationService.refundOrder(command);
            OrderResponse response = responseConverter.toResponse(orderDTO);

            return BaseResult.success(response);

        } catch (Exception e) {
            log.error("退款订单失败: {}", e.getMessage(), e);
            return BaseResult.error("500", "退款订单失败: " + e.getMessage());
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
            OrderResponse response = responseConverter.toResponse(orderDTO);

            return BaseResult.success(response);

        } catch (Exception e) {
            log.error("发货订单失败: {}", e.getMessage(), e);
            return BaseResult.error("500", "发货订单失败: " + e.getMessage());
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
            OrderResponse response = responseConverter.toResponse(orderDTO);

            return BaseResult.success(response);

        } catch (Exception e) {
            log.error("完成订单失败: {}", e.getMessage(), e);
            return BaseResult.error("500", "完成订单失败: " + e.getMessage());
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
            GetOrderByIdQuery query = GetOrderByIdQuery.builder().setOrderId(orderId).build();
            OrderDTO orderDTO = orderApplicationService.getOrderById(query);
            OrderResponse response = responseConverter.toResponse(orderDTO);

            return BaseResult.success(response);

        } catch (Exception e) {
            log.error("查询订单详情失败: {}", e.getMessage(), e);
            return BaseResult.error("500", "查询订单详情失败: " + e.getMessage());
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
            GetOrdersByCustomerQuery query = GetOrdersByCustomerQuery.builder().setCustomerId(customerId).build();
            List<OrderDTO> orderDTOs = orderApplicationService.getOrdersByCustomer(query);

            // 使用转换器批量转换
            List<OrderResponse> responses = responseConverter.toOrderResponseList(orderDTOs);

            return BaseResult.success(responses);

        } catch (Exception e) {
            log.error("查询客户订单列表失败: {}", e.getMessage(), e);
            return BaseResult.error("500", "查询客户订单列表失败: " + e.getMessage());
        }
    }

    /**
     * 分页查询订单列表
     * @param customerId 客户ID（可选）
     * @param pageNumber 页码（从1开始，默认1）
     * @param pageSize   每页大小（默认10）
     * @return 分页结果
     */
    @GetMapping
    public PageResult<List<OrderResponse>> getOrderList(
            @RequestParam(required = false) String customerId,
            @RequestParam(defaultValue = "1") Long pageNumber,
            @RequestParam(defaultValue = "10") Long pageSize) {
        log.info("分页查询订单列表: customerId={}, pageNumber={}, pageSize={}", customerId, pageNumber, pageSize);

        try {
            // 构建查询对象
            OrderListQuery query = OrderListQuery.OLQBuilder()
                                           .setCustomerId(customerId)
                                           .setPageNumber(pageNumber)
                                           .setPageSize(pageSize)
                                           .build();

            // 调用应用服务
            PageResult<List<OrderDTO>> page = orderApplicationService.getOrderList(query);

            // 使用转换器批量转换
            List<OrderResponse> responses = responseConverter.toOrderResponseList(page.getData());

            // 构建分页结果
            return PageResult.<List<OrderResponse>>PRBuilder()
                           .setPageNumber(page.getPageNumber())
                           .setPageSize(page.getPageSize())
                           .setData(responses)
                           .setTotalRaw(page.getTotalRaw())
                           .build();

        } catch (Exception e) {
            log.error("分页查询订单列表失败: {}", e.getMessage(), e);
            return PageResult.<List<OrderResponse>>PRBuilder()
                           .setCode("500")
                           .setData(null)
                           .setMessage("分页查询订单列表失败: " + e.getMessage())
                           .setTime(Instant.now())
                           .build();
        }
    }

}

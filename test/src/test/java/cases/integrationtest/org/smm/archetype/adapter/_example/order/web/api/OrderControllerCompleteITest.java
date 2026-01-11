package cases.integrationtest.org.smm.archetype.adapter._example.order.web.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.smm.archetype.adapter._example.order.web.dto.request.CancelOrderRequest;
import org.smm.archetype.adapter._example.order.web.dto.request.CreateOrderRequest;
import org.smm.archetype.adapter._example.order.web.dto.request.PayOrderRequest;
import org.smm.archetype.domain._example.order.model.PaymentMethod;
import org.smm.archetype.domain._example.order.model.valueobject.Money;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import support.ITestBase;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * OrderController完整集成测试
 *
 * <p>测试OrderController中除GET /api/orders/customer/{customerId}外的所有接口
 * @author Leonardo
 * @since 2026-01-11
 */
@DisplayName("OrderController完整集成测试")
class OrderControllerCompleteITest extends ITestBase {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected String getDataSetFile() {
        return "controller/order-data.xml";
    }

    // ==================== 查询订单详情测试 ====================

    @Test
    @DisplayName("GET /api/orders/{orderId} - 查询订单详情 - 成功")
    void getOrderById_ExistingOrder_ReturnsOrderDetails() throws Exception {
        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.get("/api/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.orderNo").value("ORD202401110001"))
                .andExpect(jsonPath("$.data.customerId").value("CUST001"))
                .andExpect(jsonPath("$.data.customerName").value("张三"))
                .andExpect(jsonPath("$.data.status").value("CREATED"))
                .andExpect(jsonPath("$.data.totalAmount.amount").value(100.0))
                .andExpect(jsonPath("$.data.totalAmount.currency").value("CNY"))
                .andExpect(jsonPath("$.data.items").isArray())
                .andExpect(jsonPath("$.data.items", hasSize(1)))
                .andExpect(jsonPath("$.data.items[0].productId").value("PROD001"))
                .andExpect(jsonPath("$.data.items[0].productName").value("iPhone 15"))
                .andExpect(jsonPath("$.data.shippingAddress.province").value("北京市"))
                .andExpect(jsonPath("$.data.contactInfo.contactName").value("张三"));
    }

    @Test
    @DisplayName("GET /api/orders/{orderId} - 查询订单详情 - 订单不存在返回错误")
    void getOrderById_NonExistentOrder_ReturnsNotFoundError() throws Exception {
        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.get("/api/orders/9999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value(containsString("订单不存在")));
    }

    @Test
    @DisplayName("GET /api/orders/{orderId} - 查询订单详情 - 已支付订单包含支付信息")
    void getOrderById_PaidOrder_ContainsPaymentInfo() throws Exception {
        // Act & Assert
        // Note: paymentTime字段缺失（待修复：OrderAggrDO和schema.sql需要添加该字段）
        mockMvc.perform(MockMvcRequestBuilders.get("/api/orders/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value("PAID"))
                .andExpect(jsonPath("$.data.paymentMethod").value("ALIPAY"));
    }

    // ==================== 支付订单测试 ====================

    @Test
    @DisplayName("POST /api/orders/{orderId}/pay - 支付订单 - 成功")
    void payOrder_CreatedOrder_ReturnsPaidOrder() throws Exception {
        // Arrange
        PayOrderRequest request = new PayOrderRequest();
        request.setPaymentMethod(PaymentMethod.ALIPAY);
        request.setPaymentAmount(Money.of(BigDecimal.valueOf(100.00), "CNY"));

        String requestJson = objectMapper.writeValueAsString(request);

        // Act & Assert
        // Note: paymentTime字段缺失（待修复：OrderAggrDO和schema.sql需要添加该字段）
        mockMvc.perform(MockMvcRequestBuilders.post("/api/orders/1/pay")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.status").value("PAID"))
                .andExpect(jsonPath("$.data.paymentMethod").value("ALIPAY"));
    }

    @Test
    @DisplayName("POST /api/orders/{orderId}/pay - 支付订单 - 订单不存在返回错误")
    void payOrder_NonExistentOrder_ReturnsNotFoundError() throws Exception {
        // Arrange
        PayOrderRequest request = new PayOrderRequest();
        request.setPaymentMethod(PaymentMethod.ALIPAY);
        request.setPaymentAmount(Money.of(BigDecimal.valueOf(100.00), "CNY"));

        String requestJson = objectMapper.writeValueAsString(request);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.post("/api/orders/9999/pay")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value(containsString("订单不存在")));
    }

    @Test
    @DisplayName("POST /api/orders/{orderId}/pay - 支付订单 - 已支付订单重复支付返回错误")
    void payOrder_AlreadyPaidOrder_ReturnsBusinessError() throws Exception {
        // Arrange
        PayOrderRequest request = new PayOrderRequest();
        request.setPaymentMethod(PaymentMethod.WECHAT);
        request.setPaymentAmount(Money.of(BigDecimal.valueOf(200.00), "CNY"));

        String requestJson = objectMapper.writeValueAsString(request);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.post("/api/orders/2/pay")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value(containsString("状态不允许")));
    }

    // ==================== 取消订单测试 ====================

    @Test
    @DisplayName("POST /api/orders/{orderId}/cancel - 取消订单 - 成功")
    void cancelOrder_CreatedOrder_ReturnsCancelledOrder() throws Exception {
        // Arrange
        CancelOrderRequest request = new CancelOrderRequest();
        request.setReason("用户主动取消");

        String requestJson = objectMapper.writeValueAsString(request);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.post("/api/orders/1/cancel")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.status").value("CANCELLED"));
    }

    @Test
    @DisplayName("POST /api/orders/{orderId}/cancel - 取消订单 - 订单不存在返回错误")
    void cancelOrder_NonExistentOrder_ReturnsNotFoundError() throws Exception {
        // Arrange
        CancelOrderRequest request = new CancelOrderRequest();
        request.setReason("测试取消");

        String requestJson = objectMapper.writeValueAsString(request);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.post("/api/orders/9999/cancel")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value(containsString("订单不存在")));
    }

    @Test
    @DisplayName("POST /api/orders/{orderId}/cancel - 取消订单 - 已发货订单取消返回错误")
    void cancelOrder_ShippedOrder_ReturnsBusinessError() throws Exception {
        // Arrange - 先将订单2改为已发货状态
        mockMvc.perform(MockMvcRequestBuilders.post("/api/orders/2/ship"));

        CancelOrderRequest request = new CancelOrderRequest();
        request.setReason("不想买了");

        String requestJson = objectMapper.writeValueAsString(request);

        // Act & Assert - 已发货订单不能取消
        mockMvc.perform(MockMvcRequestBuilders.post("/api/orders/2/cancel")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value(containsString("状态不允许")));
    }

    // ==================== 发货订单测试 ====================

    @Test
    @DisplayName("POST /api/orders/{orderId}/ship - 发货订单 - 成功")
    void shipOrder_PaidOrder_ReturnsShippedOrder() throws Exception {
        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.post("/api/orders/2/ship"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(2))
                .andExpect(jsonPath("$.data.status").value("SHIPPED"));
    }

    @Test
    @DisplayName("POST /api/orders/{orderId}/ship - 发货订单 - 订单不存在返回错误")
    void shipOrder_NonExistentOrder_ReturnsNotFoundError() throws Exception {
        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.post("/api/orders/9999/ship"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value(containsString("订单不存在")));
    }

    @Test
    @DisplayName("POST /api/orders/{orderId}/ship - 发货订单 - 未支付订单发货返回错误")
    void shipOrder_UnpaidOrder_ReturnsBusinessError() throws Exception {
        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.post("/api/orders/1/ship"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value(containsString("状态不允许")));
    }

    // ==================== 完成订单测试 ====================

    @Test
    @DisplayName("POST /api/orders/{orderId}/complete - 完成订单 - 成功")
    void completeOrder_ShippedOrder_ReturnsCompletedOrder() throws Exception {
        // 先发货
        mockMvc.perform(MockMvcRequestBuilders.post("/api/orders/2/ship"));

        // Act & Assert - 完成订单
        mockMvc.perform(MockMvcRequestBuilders.post("/api/orders/2/complete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(2))
                .andExpect(jsonPath("$.data.status").value("COMPLETED"));
    }

    @Test
    @DisplayName("POST /api/orders/{orderId}/complete - 完成订单 - 订单不存在返回错误")
    void completeOrder_NonExistentOrder_ReturnsNotFoundError() throws Exception {
        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.post("/api/orders/9999/complete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value(containsString("订单不存在")));
    }

    @Test
    @DisplayName("POST /api/orders/{orderId}/complete - 完成订单 - 未发货订单完成返回错误")
    void completeOrder_UnshippedOrder_ReturnsBusinessError() throws Exception {
        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.post("/api/orders/1/complete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value(containsString("状态不允许")));
    }

    // ==================== 创建订单测试 ====================

    @Test
    @DisplayName("POST /api/orders - 创建订单 - 成功")
    void createOrder_ValidRequest_ReturnsCreatedOrder() throws Exception {
        // Arrange - 使用 MockInventoryService 中存在的商品
        CreateOrderRequest.OrderItemRequest itemRequest = new CreateOrderRequest.OrderItemRequest();
        itemRequest.setProductId("PROD003");
        itemRequest.setProductName("AirPods Pro");
        itemRequest.setSkuCode("APPRO-WHITE-2G");
        itemRequest.setUnitPrice(Money.of(BigDecimal.valueOf(199.00), "CNY"));
        itemRequest.setQuantity(1);

        CreateOrderRequest request = new CreateOrderRequest();
        request.setCustomerId("CUST004");
        request.setCustomerName("王五");
        request.setItems(List.of(itemRequest));
        request.setTotalAmount(Money.of(BigDecimal.valueOf(199.00), "CNY"));

        CreateOrderRequest.AddressRequest addressRequest = new CreateOrderRequest.AddressRequest();
        addressRequest.setProvince("深圳市");
        addressRequest.setCity("深圳市");
        addressRequest.setDistrict("南山区");
        addressRequest.setDetailAddress("科技园");
        addressRequest.setPostalCode("518057");
        request.setShippingAddress(addressRequest);

        CreateOrderRequest.ContactInfoRequest contactRequest = new CreateOrderRequest.ContactInfoRequest();
        contactRequest.setContactName("王五");
        contactRequest.setContactPhone("13700137000");
        contactRequest.setContactEmail("wangwu@example.com");
        request.setContactInfo(contactRequest);
        request.setRemark("新订单测试");

        String requestJson = objectMapper.writeValueAsString(request);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.post("/api/orders")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.customerId").value("CUST004"))
                .andExpect(jsonPath("$.data.customerName").value("王五"))
                .andExpect(jsonPath("$.data.status").value("CREATED"))
                .andExpect(jsonPath("$.data.totalAmount.amount").value(199.0))
                .andExpect(jsonPath("$.data.items").isArray())
                .andExpect(jsonPath("$.data.items", hasSize(1)))
                .andExpect(jsonPath("$.data.items[0].productName").value("AirPods Pro"));
    }

}

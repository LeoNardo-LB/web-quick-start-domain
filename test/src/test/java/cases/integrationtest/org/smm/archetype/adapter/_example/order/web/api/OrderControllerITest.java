package cases.integrationtest.org.smm.archetype.adapter._example.order.web.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import support.ITestBase;

import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * OrderController集成测试
 *
 * <p>测试范围：
 * <ul>
 *   <li>HTTP请求/响应验证</li>
 *   <li>Controller → Application → Domain → Repository 完整流程</li>
 * </ul>
 */
@DisplayName("OrderController集成测试")
class OrderControllerITest extends ITestBase {

    @Override
    protected String getDataSetFile() {
        return "controller/order-data.xml";
    }

    @Test
    @DisplayName("GET /api/orders/customer/{customerId} - 查询客户订单列表 - 成功")
    void getOrdersByCustomer_ExistingCustomer_ReturnsOrderList() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/orders/customer/CUST001"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[*].customerId", everyItem(is("CUST001"))))
                .andExpect(jsonPath("$.data[*].customerName", everyItem(is("张三"))));
    }

    @Test
    @DisplayName("GET /api/orders/customer/{customerId} - 查询客户订单列表 - 客户存在但无订单")
    void getOrdersByCustomer_CustomerWithNoOrders_ReturnsEmptyList() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/orders/customer/CUST999"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data", hasSize(0)));
    }

    @Test
    @DisplayName("GET /api/orders/customer/{customerId} - 查询客户订单列表 - 返回不同客户的订单")
    void getOrdersByCustomer_DifferentCustomer_ReturnsOnlyTheirOrders() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/orders/customer/CUST002"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].customerId").value("CUST002"))
                .andExpect(jsonPath("$.data[0].customerName").value("李四"));
    }

    @Test
    @DisplayName("GET /api/orders/customer/{customerId} - HTTP响应头验证")
    void getOrdersByCustomer_VerifyResponseHeaders() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/orders/customer/CUST001"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(org.springframework.http.MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").exists())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @DisplayName("GET /api/orders/customer/{customerId} - 订单列表包含订单项信息")
    void getOrdersByCustomer_VerifyOrderItemsIncluded() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/orders/customer/CUST001"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].items").isArray())
                .andExpect(jsonPath("$.data[0].items[0].productId").exists())
                .andExpect(jsonPath("$.data[0].items[0].productName").exists())
                .andExpect(jsonPath("$.data[0].items[0].skuCode").exists())
                .andExpect(jsonPath("$.data[0].items[0].unitPrice").exists())
                .andExpect(jsonPath("$.data[0].items[0].quantity").exists());
    }

    @Test
    @DisplayName("GET /api/orders/customer/{customerId} - 订单列表包含收货地址信息")
    void getOrdersByCustomer_VerifyShippingAddressIncluded() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/orders/customer/CUST001"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].shippingAddress").exists())
                .andExpect(jsonPath("$.data[0].shippingAddress.province").exists())
                .andExpect(jsonPath("$.data[0].shippingAddress.city").exists())
                .andExpect(jsonPath("$.data[0].shippingAddress.district").exists())
                .andExpect(jsonPath("$.data[0].shippingAddress.detailAddress").exists())
                .andExpect(jsonPath("$.data[0].shippingAddress.postalCode").exists());
    }

    @Test
    @DisplayName("GET /api/orders/customer/{customerId} - 订单列表包含联系信息")
    void getOrdersByCustomer_VerifyContactInfoIncluded() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/orders/customer/CUST001"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].contactInfo").exists())
                .andExpect(jsonPath("$.data[0].contactInfo.contactName").value("张三"))
                .andExpect(jsonPath("$.data[0].contactInfo.contactPhone").value("13800138000"))
                .andExpect(jsonPath("$.data[0].contactInfo.contactEmail").value("zhangsan@example.com"));
    }

}

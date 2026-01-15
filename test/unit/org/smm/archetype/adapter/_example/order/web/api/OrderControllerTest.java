package org.smm.archetype.adapter._example.order.web.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.smm.archetype.app._example.order.OrderAppService;
import org.smm.archetype.domain._example.order.model.aggregateroot.OrderAggr;
import org.smm.archetype.domain._example.order.model.valueobject.Money;
import org.smm.archetype.domain._shared.base.Response;
import org.smm.archetype.domain._shared.client.OrderId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 订单Controller单元测试示例
 *
 * <p>演示如何测试Controller层：
 * <ul>
 *   <li>使用@WebMvcTest只加载Controller</li>
 *   <li>使用MockMvc模拟HTTP请求</li>
 *   <li>使用@MockBean Mock依赖Service</li>
 *   <li>使用jsonPath验证JSON响应</li>
 * </ul>
 *
 * @author Leonardo
 * @since 2026-01-16
 */
@WebMvcTest(OrderController.class)
@DisplayName("订单API单元测试示例")
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderAppService orderAppService;

    @Test
    @DisplayName("查询客户订单列表 - 成功")
    void testGetOrdersByCustomer_success() throws Exception {
        // Given: 准备Mock数据
        String customerId = "CUST001";
        OrderAggr order1 = OrderAggr.create(customerId, new Money(100.00, "CNY"));
        OrderAggr order2 = OrderAggr.create(customerId, new Money(200.00, "CNY"));

        when(orderAppService.getOrdersByCustomer(eq(customerId)))
            .thenReturn(List.of(order1, order2));

        // When & Then: 执行请求并验证
        mockMvc.perform(get("/api/orders")
                .param("customerId", customerId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.message").value("success"))
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data.length()").value(2))
            .andExpect(jsonPath("$.data[0].customerId").value(customerId));

        // 验证Service方法被调用
        verify(orderAppService).getOrdersByCustomer(eq(customerId));
    }

    @Test
    @DisplayName("查询订单详情 - 成功")
    void testGetOrderById_success() throws Exception {
        // Given
        OrderId orderId = OrderId.generate();
        OrderAggr order = OrderAggr.create("CUST001", new Money(100.00, "CNY"));

        when(orderAppService.getOrderById(eq(orderId)))
            .thenReturn(order);

        // When & Then
        mockMvc.perform(get("/api/orders/{id}", orderId.getValue())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.id").value(orderId.getValue()));

        verify(orderAppService).getOrderById(eq(orderId));
    }
}

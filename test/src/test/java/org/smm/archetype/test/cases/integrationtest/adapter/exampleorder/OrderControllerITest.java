package org.smm.archetype.test.cases.integrationtest.adapter.exampleorder;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.smm.archetype.test.support.IntegrationTestBase;
import org.springframework.http.MediaType;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * OrderController.refundOrder() 端点的集成测试
 *
 * <p>测试 REST API 退款端点，包括：
 * <ul>
 *   <li>退款成功返回 200</li>
 *   <li>参数校验失败返回 400</li>
 *   <li>业务异常返回 400</li>
 *   <li>订单不存在返回 404 或 500</li>
 * </ul>
 *
 * <p>注意：此测试类需要数据库支持才能完整运行。
 * 如果数据库未配置，测试可能无法通过。</p>
 */
@DisplayName("OrderController.refundOrder() 集成测试")
public class OrderControllerITest extends IntegrationTestBase {

    private static final String ORDERS_BASE_URL = "/api/orders";
    private static final Long NON_EXISTENT_ORDER_ID = 999999L;

    // ==================== 退款成功测试 ====================

    @Nested
    @DisplayName("退款成功测试")
    class RefundSuccessTests {

        @Test
        @DisplayName("退款成功 - 返回 HTTP 200")
        void testRefundOrder_Success_Returns200() {
            // Given - 退款请求
            String requestBody = """
                {
                    "refundAmount": 100.00,
                    "currency": "CNY",
                    "refundType": "FULL",
                    "refundReason": "用户申请退款"
                }
                """;

            // When & Then - 发送退款请求
            // 注意：此测试假设订单ID=1存在且状态为PAID
            // 实际运行需要先创建并支付订单
            webTestClient.post()
                    .uri(ORDERS_BASE_URL + "/1/refund")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.code").isEqualTo("200")
                    .jsonPath("$.data").exists()
                    .jsonPath("$.data.status").isEqualTo("REFUNDED")
                    .jsonPath("$.data.refundType").isEqualTo("FULL");
        }

    }

    // ==================== 参数校验失败测试 ====================

    @Nested
    @DisplayName("参数校验失败测试")
    class ValidationFailureTests {

        @Test
        @DisplayName("缺少退款金额 - 返回 HTTP 400")
        void testRefundOrder_MissingRefundAmount_Returns400() {
            // Given - 缺少退款金额
            String requestBody = """
                {
                    "currency": "CNY",
                    "refundType": "FULL",
                    "refundReason": "用户申请退款"
                }
                """;

            // When & Then
            webTestClient.post()
                    .uri(ORDERS_BASE_URL + "/1/refund")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .exchange()
                    .expectStatus().isBadRequest();
        }

        @Test
        @DisplayName("退款金额为0 - 返回 HTTP 400")
        void testRefundOrder_ZeroRefundAmount_Returns400() {
            // Given - 退款金额为0
            String requestBody = """
                {
                    "refundAmount": 0,
                    "currency": "CNY",
                    "refundType": "FULL",
                    "refundReason": "用户申请退款"
                }
                """;

            // When & Then
            webTestClient.post()
                    .uri(ORDERS_BASE_URL + "/1/refund")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .exchange()
                    .expectStatus().isBadRequest();
        }

        @Test
        @DisplayName("退款金额为负数 - 返回 HTTP 400")
        void testRefundOrder_NegativeRefundAmount_Returns400() {
            // Given - 退款金额为负数
            String requestBody = """
                {
                    "refundAmount": -10.00,
                    "currency": "CNY",
                    "refundType": "FULL",
                    "refundReason": "用户申请退款"
                }
                """;

            // When & Then
            webTestClient.post()
                    .uri(ORDERS_BASE_URL + "/1/refund")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .exchange()
                    .expectStatus().isBadRequest();
        }

        @Test
        @DisplayName("缺少币种 - 返回 HTTP 400")
        void testRefundOrder_MissingCurrency_Returns400() {
            // Given - 缺少币种
            String requestBody = """
                {
                    "refundAmount": 100.00,
                    "refundType": "FULL",
                    "refundReason": "用户申请退款"
                }
                """;

            // When & Then
            webTestClient.post()
                    .uri(ORDERS_BASE_URL + "/1/refund")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .exchange()
                    .expectStatus().isBadRequest();
        }

        @Test
        @DisplayName("缺少退款类型 - 返回 HTTP 400")
        void testRefundOrder_MissingRefundType_Returns400() {
            // Given - 缺少退款类型
            String requestBody = """
                {
                    "refundAmount": 100.00,
                    "currency": "CNY",
                    "refundReason": "用户申请退款"
                }
                """;

            // When & Then
            webTestClient.post()
                    .uri(ORDERS_BASE_URL + "/1/refund")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .exchange()
                    .expectStatus().isBadRequest();
        }

        @Test
        @DisplayName("缺少退款原因 - 返回 HTTP 400")
        void testRefundOrder_MissingRefundReason_Returns400() {
            // Given - 缺少退款原因
            String requestBody = """
                {
                    "refundAmount": 100.00,
                    "currency": "CNY",
                    "refundType": "FULL"
                }
                """;

            // When & Then
            webTestClient.post()
                    .uri(ORDERS_BASE_URL + "/1/refund")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .exchange()
                    .expectStatus().isBadRequest();
        }

    }

    // ==================== 业务异常测试 ====================

    @Nested
    @DisplayName("业务异常测试")
    class BusinessErrorTests {

        @Test
        @DisplayName("退款金额超限 - 返回 HTTP 500 或 400")
        void testRefundOrder_ExceededAmount_ReturnsError() {
            // Given - 退款金额超过订单金额
            String requestBody = """
                {
                    "refundAmount": 999999.00,
                    "currency": "CNY",
                    "refundType": "FULL",
                    "refundReason": "测试超限退款"
                }
                """;

            // When & Then
            webTestClient.post()
                    .uri(ORDERS_BASE_URL + "/1/refund")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .exchange()
                    .expectStatus().is5xxServerError()
                    .expectBody()
                    .jsonPath("$.code").isEqualTo("500");
        }

    }

    // ==================== 订单不存在测试 ====================

    @Nested
    @DisplayName("订单不存在测试")
    class OrderNotFoundTests {

        @Test
        @DisplayName("订单不存在 - 返回 HTTP 500")
        void testRefundOrder_OrderNotFound_ReturnsError() {
            // Given - 不存在的订单ID
            String requestBody = """
                {
                    "refundAmount": 100.00,
                    "currency": "CNY",
                    "refundType": "FULL",
                    "refundReason": "用户申请退款"
                }
                """;

            // When & Then
            webTestClient.post()
                    .uri(ORDERS_BASE_URL + "/" + NON_EXISTENT_ORDER_ID + "/refund")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .exchange()
                    .expectStatus().is5xxServerError()
                    .expectBody()
                    .jsonPath("$.code").isEqualTo("500")
                    .jsonPath("$.message").value(message -> 
                            assertThat((String) message).contains("订单不存在"));
        }

    }

    // ==================== 部分退款测试 ====================

    @Nested
    @DisplayName("部分退款测试")
    class PartialRefundTests {

        @Test
        @DisplayName("部分退款成功 - 返回 HTTP 200")
        void testRefundOrder_PartialRefund_Success() {
            // Given - 部分退款请求
            String requestBody = """
                {
                    "refundAmount": 30.00,
                    "currency": "CNY",
                    "refundType": "PARTIAL",
                    "refundReason": "部分退款"
                }
                """;

            // When & Then - 注意：此测试假设订单ID=2存在且状态为PAID
            webTestClient.post()
                    .uri(ORDERS_BASE_URL + "/2/refund")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.code").isEqualTo("200")
                    .jsonPath("$.data.status").isEqualTo("PARTIALLY_REFUNDED")
                    .jsonPath("$.data.refundType").isEqualTo("PARTIAL");
        }

    }

}

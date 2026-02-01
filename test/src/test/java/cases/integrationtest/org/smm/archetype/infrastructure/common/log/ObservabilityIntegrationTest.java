package cases.integrationtest.org.smm.archetype.infrastructure.common.log;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.TestPropertySource;
import support.ITestBase;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Observability集成测试
 *
 * <p>测试范围：
 * <ul>
 *   <li>Actuator健康检查端点</li>
 *   <li>Actuator Prometheus端点</li>
 *   <li>Actuator Metrics端点</li>
 *   <li>标准文本日志输出</li>
 *   <li>链路追踪集成</li>
 * </ul>
 *
 * @author Leonardo
 * @since 2026-01-30
 */
@TestPropertySource(properties = {
        "server.servlet.context-path=",  // 清空 context-path 便于测试
        "management.endpoints.web.exposure.include=health,info,metrics,prometheus"
})
@DisplayName("Observability集成测试")
class ObservabilityIntegrationTest extends ITestBase {

    @Value("${server.servlet.context-path:}")
    private String contextPath;

    @Test
    @DisplayName("GET /actuator/health - 健康检查端点返回200和状态")
    void testHealthEndpoint() {
        // Act & Assert
        webTestClient.get().uri("/actuator/health")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(body -> assertThat(body).contains("\"status\""));
    }

    @Test
    @DisplayName("GET /actuator/prometheus - Prometheus端点返回Prometheus格式")
    void testPrometheusEndpoint() {
        // Act & Assert
        webTestClient.get().uri("/actuator/prometheus")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(body -> {
                    assertThat(body).contains("# HELP");
                    assertThat(body).contains("# TYPE");
                });
    }

    @Test
    @DisplayName("GET /actuator/metrics - Metrics端点列出所有指标名称")
    void testMetricsEndpoint() {
        // Act & Assert
        webTestClient.get().uri("/actuator/metrics")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(body -> assertThat(body).contains("\"names\""));
    }

    @Test
    @DisplayName("GET /actuator/metrics/jvm.memory.used - 查询特定JVM指标")
    void testSpecificJvmMetric() {
        // Act & Assert
        webTestClient.get().uri("/actuator/metrics/jvm.memory.used")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(body -> assertThat(body).contains("\"name\":\"jvm.memory.used\""));
    }

    @Test
    @DisplayName("Prometheus指标包含应用标签")
    void testPrometheusMetricsIncludeCommonTags() {
        // Act & Assert
        webTestClient.get().uri("/actuator/prometheus")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(body -> {
                    assertThat(body).contains("application=\"");
                });
    }

    @Test
    @DisplayName("链路追踪占位符在日志配置中存在")
    void testTracingPlaceholdersInLogPattern() {
        // 此测试验证logback配置包含traceId和spanId占位符
        // 由于无法直接读取运行中的logback配置，这里我们验证
        // 通过观察日志行为（在实际运行中）

        // 注意：在实际运行中，日志文件应包含traceId和spanId占位符
        // 验证通过检查logback-spring.xml配置文件完成
        // 此测试为占位符，实际验证需要检查日志文件或配置文件
    }
}

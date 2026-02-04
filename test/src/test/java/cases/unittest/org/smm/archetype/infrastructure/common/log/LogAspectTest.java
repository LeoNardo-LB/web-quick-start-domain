package cases.unittest.org.smm.archetype.infrastructure.common.log;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.smm.archetype.infrastructure.bizshared.log.LogAspect;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * LogAspect单元测试，验证AOP切面日志记录和指标采集。
 *
 * <p>⚠️ 注意：由于 Spring Boot 4.0.2 和 spring-boot-starter-aop 4.0.0-M2 存在兼容性问题，
 * LogAspect 的 AOP 功能当前不可用。这些测试保留用于 AOP 功能恢复后的验证。
 */
@DisplayName("LogAspect单元测试")
@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class LogAspectTest {

    private LogAspect logAspect;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        // Create LogAspect instance with SimpleMeterRegistry (for testing)
        MeterRegistry meterRegistry = new SimpleMeterRegistry();
        logAspect = new LogAspect(meterRegistry);
    }

    // ==================== Constructor Tests ====================

    @Test
    @DisplayName("构造函数 - 有参构造 - 对象创建成功")
    void constructor_WithMeterRegistry_InstanceCreated() {
        // Act
        MeterRegistry meterRegistry = new SimpleMeterRegistry();
        LogAspect aspect = new LogAspect(meterRegistry);

        // Assert
        assertThat(aspect).isNotNull();
    }

    // ==================== AOP Functionality Tests (Disabled due to compatibility issue) ====================

    @Test
    @Disabled("AOP功能当前不可用：Spring Boot 4.0.2 和 spring-boot-starter-aop 4.0.0-M2 兼容性问题")
    @DisplayName("init方法 - 注册Metrics - Timer和Counter创建成功")
    void init_RegisterMetrics_TimerAndCounterCreated() {
        // 此测试在 AOP 功能恢复后需要恢复
        // 当前由于 AOP 不可用，initIfNecessary() 方法不会被调用
    }

    @Test
    @Disabled("AOP功能当前不可用：Spring Boot 4.0.2 和 spring-boot-starter-aop 4.0.0-M2 兼容性问题")
    @DisplayName("init方法 - 多次调用 - 每次使用相同Metrics")
    void init_MultipleCalls_UsesSameMetrics() {
        // 此测试在 AOP 功能恢复后需要恢复
        // 当前由于 AOP 不可用，initIfNecessary() 方法不会被调用
    }

    // ==================== Pointcut Method Tests ====================

    @Test
    @DisplayName("logCut - Pointcut方法 - 无异常抛出")
    void logCut_PointcutMethod_NoException() {
        // Act & Assert - just verify method doesn't throw
        logAspect.logCut();
    }

    @Test
    @DisplayName("clientCut - Pointcut方法 - 无异常抛出")
    void clientCut_PointcutMethod_NoException() {
        // Act & Assert - just verify method doesn't throw
        logAspect.clientCut();
    }

    @Test
    @DisplayName("combinedCut - Pointcut方法 - 无异常抛出")
    void combinedCut_PointcutMethod_NoException() {
        // Act & Assert - just verify method doesn't throw
        logAspect.combinedCut();
    }

}

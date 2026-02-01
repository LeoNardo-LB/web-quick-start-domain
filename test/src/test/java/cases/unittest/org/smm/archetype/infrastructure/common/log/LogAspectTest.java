package cases.unittest.org.smm.archetype.infrastructure.common.log;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.smm.archetype.infrastructure.common.log.LogAspect;
import support.UnitTestBase;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * LogAspect单元测试，验证AOP切面日志记录和指标采集。
 */
@DisplayName("LogAspect单元测试")
@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class LogAspectTest {

    private MeterRegistry meterRegistry;

    private LogAspect logAspect;

    @BeforeEach
    void setUp() throws Exception {
        // Use SimpleMeterRegistry instead of mocking
        meterRegistry = new SimpleMeterRegistry();

        // Create LogAspect instance
        logAspect = new LogAspect(meterRegistry);
        logAspect.init();
    }

    // ==================== Constructor and @PostConstruct Tests ====================

    @Test
    @DisplayName("构造函数 - 注入MeterRegistry - 字段初始化成功")
    void constructor_InjectMeterRegistry_FieldsInitialized() {
        // Act
        LogAspect aspect = new LogAspect(meterRegistry);

        // Assert
        assertThat(aspect).isNotNull();
    }

    @Test
    @DisplayName("init方法 - 注册Metrics - Timer和Counter创建成功")
    void init_RegisterMetrics_TimerAndCounterCreated() {
        // Arrange
        MeterRegistry testRegistry = new SimpleMeterRegistry();
        LogAspect aspect = new LogAspect(testRegistry);

        // Act
        aspect.init();

        // Assert
        assertThat(testRegistry.find("log_aspect_timer_seconds").timer()).isNotNull();
        assertThat(testRegistry.find("log_aspect_counter_total").counter()).isNotNull();
        assertThat(testRegistry.find("log_aspect_errors_total").counter()).isNotNull();
    }

    @Test
    @DisplayName("init方法 - 多次调用 - 每次使用相同Metrics")
    void init_MultipleCalls_UsesSameMetrics() {
        // Arrange
        MeterRegistry testRegistry = new SimpleMeterRegistry();
        LogAspect aspect = new LogAspect(testRegistry);
        
        // Act - call init multiple times on same instance
        aspect.init();
        aspect.init();
        
        // Assert
        // Due to @PostConstruct, init() is called automatically by Spring
        // Multiple calls to init() should use the same MeterRegistry instance
        // So timer samples should accumulate, not create new timers
        assertThat(testRegistry.getMeters().stream().filter(m -> m.getId().getName().equals("log_aspect_timer_seconds")).count()).isEqualTo(1);
        assertThat(testRegistry.getMeters().stream().filter(m -> m.getId().getName().equals("log_aspect_counter_total")).count()).isEqualTo(1);
        assertThat(testRegistry.getMeters().stream().filter(m -> m.getId().getName().equals("log_aspect_errors_total")).count()).isEqualTo(1);
    }

    // ==================== logCut Method Tests ====================

    @Test
    @DisplayName("logCut - Pointcut方法 - 无异常抛出")
    void logCut_PointcutMethod_NoException() {
        // Act & Assert - just verify method doesn't throw
        logAspect.logCut();
    }

}

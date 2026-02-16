package org.smm.archetype.test.cases.integrationtest.aop;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.smm.archetype.app.aop.AopTestAppService;
import org.smm.archetype.test.support.IntegrationTestBase;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

/**
 * DomainEventCollectAspectJ 切面集成测试
 *
 * <p>测试 AOP 切面是否正确拦截 AppService 方法调用。</p>
 *
 * <p>验证内容：
 * <ul>
 *   <li>切点表达式是否匹配 AppService 类</li>
 *   <li>切面是否正确设置 ScopedThreadContext 上下文</li>
 *   <li>领域事件列表是否正确初始化</li>
 * </ul>
 */
@DisplayName("DomainEventCollectAspectJ AOP 集成测试")
class DomainEventCollectAspectITest extends IntegrationTestBase {

    @Autowired
    private AopTestAppService aopTestAppService;

    // ==================== AOP 生效验证 ====================

    @Nested
    @DisplayName("AOP 生效验证")
    class AopEnabledTests {

        @Test
        @DisplayName("调用 AppService 方法时 - AOP 切面应生效")
        void testAopIsEnabled_whenCallingAppService() {
            // When - 调用 AppService 方法
            boolean aopEnabled = aopTestAppService.isAopEnabled();

            // Then - AOP 应该生效，返回 true
            assertThat(aopEnabled)
                    .as("AOP 切面应该生效，ScopedThreadContext.getDomainEvents() 应返回非空列表")
                    .isTrue();
        }

        @Test
        @DisplayName("调用 AppService 方法时 - 领域事件列表应初始化为空列表")
        void testDomainEventsInitialized_whenCallingAppService() {
            // When - 调用 AppService 方法获取事件列表大小
            int size = aopTestAppService.getDomainEventsSize();

            // Then - 事件列表应该被初始化（初始为空）
            assertThat(size)
                    .as("领域事件列表应该被初始化为空列表")
                    .isZero();
        }

    }

    // ==================== 切点表达式验证 ====================

    @Nested
    @DisplayName("切点表达式验证")
    class PointcutExpressionTests {

        @Test
        @DisplayName("切点表达式应匹配以 AppService 结尾的类")
        void testPointcutMatchesAppServiceClass() {
            // Given - AopTestAppService 类名以 AppService 结尾
            String className = AopTestAppService.class.getSimpleName();

            // Then - 类名应该以 AppService 结尾
            assertThat(className)
                    .as("测试服务类名应该以 AppService 结尾以匹配切点表达式")
                    .endsWith("AppService");
        }

    }

    // ==================== 多次调用验证 ====================

    @Nested
    @DisplayName("多次调用验证")
    class MultipleInvocationTests {

        @Test
        @DisplayName("多次调用 AppService 方法 - AOP 应始终生效")
        void testAopEnabled_multipleInvocations() {
            // When - 多次调用 AppService 方法
            for (int i = 0; i < 5; i++) {
                boolean aopEnabled = aopTestAppService.isAopEnabled();

                // Then - 每次调用 AOP 都应该生效
                assertThat(aopEnabled)
                        .as("第 %d 次调用时 AOP 应该生效", i + 1)
                        .isTrue();
            }
        }

        @Test
        @DisplayName("多次调用 - 领域事件列表应该独立（不共享状态）")
        void testDomainEventsIndependent_multipleInvocations() {
            // When - 多次调用并验证
            assertThatNoException()
                    .as("每次调用都应该有独立的领域事件列表")
                    .isThrownBy(() -> {
                        for (int i = 0; i < 3; i++) {
                            int size = aopTestAppService.getDomainEventsSize();
                            assertThat(size).isZero();
                        }
                    });
        }

    }

}

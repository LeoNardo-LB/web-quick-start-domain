package org.smm.archetype.test.support;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * 应用启动测试
 * <p>
 * 验证 Spring 上下文能正常启动，是测试流程的守门员
 * <p>
 * 测试类型：集成测试（启动 Spring 上下文）
 */
class ApplicationStartupTests extends IntegrationTestBase {

    private final ApplicationContext applicationContext;

    @Autowired
    ApplicationStartupTests(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Test
    void testApplicationContextStartup() {
        // Then: 验证 Spring 上下文正常启动
        assertNotNull(applicationContext, "Spring 上下文应该正常启动");
    }

}

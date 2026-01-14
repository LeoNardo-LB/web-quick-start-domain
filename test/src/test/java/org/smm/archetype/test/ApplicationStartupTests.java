package org.smm.archetype.test;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.smm.archetype.domain._shared.client.EsClient;
import org.smm.archetype.domain.common.search.SearchService;
import support.ITestBase;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 应用启动测试
 *
 * <p>验证Spring容器能够成功启动并装配所有Bean
 *
 * @author Leonardo
 * @since 2026-01-14
 */
@DisplayName("应用启动测试")
@SpringBootTest(classes = TestBootstrap.class)
class ApplicationStartupTests extends ITestBase {

    @Autowired(required = false)
    private EsClient esClient;

    @Autowired(required = false)
    private SearchService searchService;

    @Test
    @DisplayName("验证EsClient Bean成功装配")
    void verifyEsClientBeanLoaded() {
        // Assert
        assertThat(esClient).isNotNull();
        System.out.println("✅ EsClient Bean successfully loaded: " + esClient.getClass().getSimpleName());
    }

    @Test
    @DisplayName("验证SearchService Bean成功装配")
    void verifySearchServiceBeanLoaded() {
        // Assert
        assertThat(searchService).isNotNull();
        System.out.println("✅ SearchService Bean successfully loaded: " + searchService.getClass().getSimpleName());
    }

    @Test
    @DisplayName("验证应用上下文成功启动")
    void verifyApplicationContextStartup() {
        // If we reach here, the context started successfully
        System.out.println("✅ Application context started successfully");
    }
}
